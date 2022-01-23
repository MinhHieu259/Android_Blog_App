package com.example.blogapp.Adapters;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.blogapp.CommentActivity;
import com.example.blogapp.Constaint;
import com.example.blogapp.Fragments.HomeFragment;
import com.example.blogapp.Models.Comment;
import com.example.blogapp.Models.Post;
import com.example.blogapp.R;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentsHolder>{
    private Context context;
    private ArrayList<Comment> list;
    private SharedPreferences sharedPreferences;
    private ProgressDialog dialog;
    public CommentAdapter(Context context, ArrayList<Comment> list) {
        this.context = context;
        this.list = list;
        dialog = new ProgressDialog(context);
        sharedPreferences = context.getSharedPreferences("user", Context.MODE_PRIVATE);
    }

    @NonNull
    @Override
    public CommentsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_comment, parent, false);
       return new CommentsHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentsHolder holder, @SuppressLint("RecyclerView") int position) {
        Comment comment = list.get(position);
        Picasso.get().load(comment.getUser().getPhoto()).into(holder.imgProfile);
        holder.txtName.setText(comment.getUser().getUserName());
        holder.txtDate.setText(comment.getDate());
        holder.txtComment.setText(comment.getComment());

        if (sharedPreferences.getInt("id", 0) != comment.getUser().getId()){
            holder.btnDeleteComment.setVisibility(View.GONE);
        }else {
            holder.btnDeleteComment.setVisibility(View.VISIBLE);
            holder.btnDeleteComment.setOnClickListener(v->{
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("Chắn chắn xóa ?");
                builder.setPositiveButton("Xóa", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        deleteComment(comment.getId(), position);
                    }
                });
                builder.setNegativeButton("Trở về", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                builder.show();
            });

        }
    }
    public void deleteComment(int commentId, int position){
        dialog.setMessage("Đang xử lý...");
        dialog.show();
        StringRequest request = new StringRequest( Request.Method.POST, Constaint.DELETE_COMMENT, response -> {
            try {
                JSONObject object = new JSONObject(response);
                if (object.getBoolean("success")){
                    list.remove(position);
                    Post post = HomeFragment.arrayList.get(CommentActivity.postPosition);
                    post.setComment(post.getComment()-1);
                    HomeFragment.arrayList.set(CommentActivity.postPosition, post);
                    HomeFragment.recyclerView.getAdapter().notifyDataSetChanged();
                    notifyDataSetChanged();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            dialog.dismiss();
        }, error -> {
            error.printStackTrace();
            dialog.dismiss();
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                String token = sharedPreferences.getString("token", "");
                HashMap<String,String> map = new HashMap<>();
                map.put("Authorization", "Bearer "+token);
                return map;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String,String> map = new HashMap<>();
                map.put("id", commentId+"");
                return map;
            }
        };
        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add(request);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class CommentsHolder extends RecyclerView.ViewHolder{
        private CircleImageView imgProfile;
        TextView txtName,txtDate, txtComment;
        private ImageButton btnDeleteComment;

        public CommentsHolder(@NonNull View itemView) {
            super(itemView);
            imgProfile = itemView.findViewById(R.id.imgCommentProfile);
            txtName = itemView.findViewById(R.id.txtCommentName);
            txtComment = itemView.findViewById(R.id.txtCommentText);
            txtDate = itemView.findViewById(R.id.txtCommentDate);
            btnDeleteComment = itemView.findViewById(R.id.btnDeleteComment);

        }
    }
}

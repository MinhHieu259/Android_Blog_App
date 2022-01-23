package com.example.blogapp.Adapters;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
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
import com.example.blogapp.EditPostActivity;
import com.example.blogapp.HomeActivity;
import com.example.blogapp.Models.Post;
import com.example.blogapp.R;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.PostsHolder> {
    private Context context;
    private ArrayList<Post> list;
    private ArrayList<Post> listAll;
    private SharedPreferences sharedPreferences;

    public PostsAdapter(Context context, ArrayList<Post> list) {
        this.context = context;
        this.list = list;
        this.listAll = new ArrayList<>(list);
        sharedPreferences = context.getApplicationContext().getSharedPreferences("user", Context.MODE_PRIVATE);

    }

    @NonNull
    @Override
    public PostsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_post, parent, false);
        return new PostsHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostsHolder holder, @SuppressLint("RecyclerView") int position) {
        Post post = list.get(position);
        Picasso.get().load(Constaint.URL+"storage/profiles/"+post.getUser().getPhoto()).into(holder.imgProfile);
        Picasso.get().load(Constaint.URL+"storage/posts/"+post.getPhoto()).into(holder.imgPost);
        holder.txtname.setText(post.getUser().getUserName());
        holder.txtComment.setText("Xem tất cả: "+post.getComment()+" bình luận");
        holder.txtLike.setText(post.getLike()+" Likes");
        holder.txtDate.setText(post.getDate());
        holder.txtDesc.setText(post.getDesc());
        holder.btnlike.setImageResource(
                post.isSelfLike() ? R.drawable.ic_baseline_favorite_red :R.drawable.ic_baseline_favorite_outline
        );
        holder.btnlike.setOnClickListener(v->{
            holder.btnlike.setImageResource(
                    post.isSelfLike() ? R.drawable.ic_baseline_favorite_outline :R.drawable.ic_baseline_favorite_red
            );
            StringRequest request = new StringRequest(Request.Method.POST, Constaint.LIKE_POST, response -> {
                Post mpost = list.get(position);
                try {
                    JSONObject object = new JSONObject(response);
                    if (object.getBoolean("success")){
                       mpost.setSelfLike(!post.isSelfLike());
                       mpost.setLike(mpost.isSelfLike()?mpost.getLike()+1:mpost.getLike()-1);
                       list.set(position, mpost);
                       notifyItemChanged(position);
                       notifyDataSetChanged();
                    }else {
                        holder.btnlike.setImageResource(
                                post.isSelfLike() ? R.drawable.ic_baseline_favorite_outline :R.drawable.ic_baseline_favorite_red
                        );
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }, error -> {
                error.printStackTrace();
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
                    map.put("id", post.getId()+"");
                    return map;
                }


            };
            RequestQueue queue = Volley.newRequestQueue(context);
            queue.add(request);
        });
        holder.txtComment.setOnClickListener(v->{
            Intent intent = new Intent((HomeActivity)context, CommentActivity.class);
            intent.putExtra("postId", post.getId());
            intent.putExtra("postPosition", position);
            context.startActivity(intent);
        });
        holder.btnComment.setOnClickListener(v->{
            Intent intent = new Intent((HomeActivity)context, CommentActivity.class);
            intent.putExtra("postId", post.getId());
            intent.putExtra("postPosition", position);
            context.startActivity(intent);
        });
        if (post.getUser().getId() == sharedPreferences.getInt("id", 0))
        {
        holder.btnPostOption.setVisibility(View.VISIBLE);
        }else{
            holder.btnPostOption.setVisibility(View.GONE);
        }
        holder.btnPostOption.setOnClickListener(v->{
            PopupMenu popupMenu = new PopupMenu(context, holder.btnPostOption);
            popupMenu.inflate(R.menu.menu_post_option);
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    switch (menuItem.getItemId()){
                        case R.id.item_edit:{
                            Intent intent = new Intent(((HomeActivity)context), EditPostActivity.class);
                            intent.putExtra("id", post.getId());
                            intent.putExtra("position", position);
                            intent.putExtra("desc", post.getDesc());
                            context.startActivity(intent);
                            return true;

                        }
                        case R.id.item_delete:{
                            deletePost(post.getId(), position);
                        }
                    }
                    return false;
                }
            });
            popupMenu.show();
        });
    }

    private void deletePost(int postId, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Xác nhận");
        builder.setMessage("Xóa bài viết ?");
        builder.setPositiveButton("Xóa", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                StringRequest request = new StringRequest(Request.Method.POST, Constaint.DELETE_POST, response -> {
                    try {
                        JSONObject object = new JSONObject(response);
                        if (object.getBoolean("success")){
                            list.remove(position);
                            notifyItemRemoved(position);
                            notifyDataSetChanged();
                            listAll.clear();
                            listAll.addAll(list);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }, error -> {

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
                        map.put("id", postId+"");
                        return map;
                    }
                };
                RequestQueue queue = Volley.newRequestQueue(context);
                queue.add(request);
            }
        });
        builder.setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.show();
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            ArrayList<Post> filterList = new ArrayList<>();
            if (charSequence.toString().isEmpty()){
                filterList.addAll(listAll);
            }else {
                for (Post post : listAll){
                    if (post.getDesc().toLowerCase().contains(charSequence.toString().toLowerCase())
                    || post.getUser().getUserName().toLowerCase().contains(charSequence.toString().toLowerCase())){
                        filterList.add(post);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filterList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            list.clear();
            list.addAll((Collection<? extends Post>) filterResults.values);
            notifyDataSetChanged();
        }
    };

    public Filter getFilter() {
        return filter;
    }

    class PostsHolder extends RecyclerView.ViewHolder{
        private TextView txtname, txtDate, txtDesc, txtLike, txtComment;
        private CircleImageView imgProfile;
        private ImageView imgPost;
        ImageButton btnPostOption, btnlike, btnComment;
        public PostsHolder(@NonNull View itemView) {
            super(itemView);
            txtname = itemView.findViewById(R.id.txtPostname);
            txtDate = itemView.findViewById(R.id.txtPostDate);
            txtDesc = itemView.findViewById(R.id.txtPostDesc);
            txtLike = itemView.findViewById(R.id.txtPostLike);
            txtComment = itemView.findViewById(R.id.txtPostComment);
            imgPost = itemView.findViewById(R.id.imgPostPhoto);
            imgProfile = itemView.findViewById(R.id.imgPostProfile);
            btnlike = itemView.findViewById(R.id.btnPostLike);
            btnComment = itemView.findViewById(R.id.btnPostComment);
            btnPostOption = itemView.findViewById(R.id.btnPostOption);
            btnPostOption.setVisibility(View.GONE);
        }
    }
}

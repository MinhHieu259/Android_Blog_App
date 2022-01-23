package com.example.blogapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.blogapp.Adapters.CommentAdapter;
import com.example.blogapp.Fragments.HomeFragment;
import com.example.blogapp.Models.Comment;
import com.example.blogapp.Models.Post;
import com.example.blogapp.Models.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CommentActivity extends AppCompatActivity {
    private RecyclerView recyclerViewcm;
    private ArrayList<Comment> list;
    private CommentAdapter adapter;
    private EditText txtAddComment;
    private int postId = 0;
    public static int postPosition = 0;
    private SharedPreferences sharedPreferences;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);
        init();
    }
    private void init(){
        dialog= new ProgressDialog(this);
        dialog.setCancelable(false);
        txtAddComment = findViewById(R.id.txtAddComment);
        postPosition = getIntent().getIntExtra("postPosition", -1);
        sharedPreferences = getApplication().getSharedPreferences("user", Context.MODE_PRIVATE);
        recyclerViewcm = findViewById(R.id.recyclerComments);
        recyclerViewcm.setHasFixedSize(true);
        recyclerViewcm.setLayoutManager(new LinearLayoutManager(this));
        postId = getIntent().getIntExtra("postId", 0);
        getComment();
    }

    private void getComment() {
        list = new ArrayList<>();
        StringRequest request = new StringRequest(Request.Method.POST, Constaint.COMMENTS, response -> {
            try {
                JSONObject object = new JSONObject(response);
                if (object.getBoolean("success")){
                    JSONArray comments = new JSONArray(object.getString("comments"));
                    for (int i =0; i< comments.length(); i++){
                        JSONObject comment= comments.getJSONObject(i);
                        JSONObject user = comment.getJSONObject("user");

                        User mUser = new User();
                        mUser.setId(user.getInt("id"));
                        mUser.setPhoto(Constaint.URL+"storage/profiles/"+user.getString("photo"));
                        mUser.setUserName(user.getString("name")+" "+user.getString("lastname"));

                        Comment mComment = new Comment();
                        mComment.setId(comment.getInt("id"));
                        mComment.setUser(mUser);
                        mComment.setDate(comment.getString("created_at"));
                        mComment.setComment(comment.getString("comment"));
                        list.add(mComment);

                    }
                }
                adapter = new CommentAdapter(this, list);
                recyclerViewcm.setAdapter(adapter);
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
                map.put("id", postId+"");
                return map;
            }
        };
        RequestQueue queue = Volley.newRequestQueue(CommentActivity.this);
        queue.add(request);
    }

    public void goBack(View view) {
        super.onBackPressed();
    }

    public void addComment(View view) {
        String commentText = txtAddComment.getText().toString();
        dialog.setMessage("Đang xử lý...");
        dialog.show();
        if (commentText.length()>0){
            StringRequest request = new StringRequest(Request.Method.POST, Constaint.CREATE_COMMENT, response -> {
                JSONObject object = null;
                try {
                    object = new JSONObject(response);
                    if (object.getBoolean("success")){
                        JSONObject comment = object.getJSONObject("comment");
                        JSONObject user = object.getJSONObject("user");

                        Comment c = new Comment();
                        User u = new User();
                        u.setId(user.getInt("id"));
                        u.setUserName(user.getString("name")+" "+ user.getString("lastname"));
                        u.setPhoto(Constaint.URL+"storage/profiles/"+user.getString("photo"));
                        c.setUser(u);
                        c.setId(comment.getInt("id"));
                        c.setDate(comment.getString("created_at"));
                        c.setComment(comment.getString("comment"));

                        // Cập nhật comment count
                        Post post = HomeFragment.arrayList.get(postPosition);
                        post.setComment(post.getComment()+1);
                        HomeFragment.arrayList.set(postPosition, post);
                        HomeFragment.recyclerView.getAdapter().notifyDataSetChanged();

                        list.add(c);
                        recyclerViewcm.getAdapter().notifyDataSetChanged();
                        txtAddComment.setText("");


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
                protected Map<String,String> getParams() throws AuthFailureError {
                    HashMap<String,String> map = new HashMap<>();
                    map.put("id", postId+"");
                    map.put("comment", commentText);
                    return map;
                }
            };
            RequestQueue queue = Volley.newRequestQueue(CommentActivity.this);
            queue.add(request);
        }
    }
}
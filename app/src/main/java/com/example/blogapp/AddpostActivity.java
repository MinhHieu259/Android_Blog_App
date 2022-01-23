package com.example.blogapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.blogapp.Fragments.HomeFragment;
import com.example.blogapp.Models.Post;
import com.example.blogapp.Models.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AddpostActivity extends AppCompatActivity {
private Button btnPost;
private ImageView imgPost;
private EditText txtTextPost;
private Bitmap bitmap = null;
private static final int GALLERY_CHANGE_POST = 3;
private ProgressDialog dialog;
private SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addpost);
        init();
    }
    public void init(){
        sharedPreferences = getApplicationContext().getSharedPreferences("user", Context.MODE_PRIVATE);
        btnPost = findViewById(R.id.btn_Addpost);
        imgPost = findViewById(R.id.imgAddpost);
        txtTextPost = findViewById(R.id.txtDescAddpost);
        imgPost.setImageURI(getIntent().getData());
        dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        try {
            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), getIntent().getData());
        } catch (IOException e) {
            e.printStackTrace();
        }
        btnPost.setOnClickListener(v->{
            if (!txtTextPost.getText().toString().isEmpty()){
                post();
            }else {
                Toast.makeText(this, "Không được để trống thông tin", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void post(){
        dialog.setMessage("Đang xử lý");
        dialog.show();
        StringRequest request = new StringRequest(Request.Method.POST, Constaint.ADD_POST, response -> {
            try {
                JSONObject object = new JSONObject(response);
                if (object.getBoolean("success")){
                    JSONObject postobject = object.getJSONObject("post");
                    JSONObject userobject = postobject.getJSONObject("user");

                    User user = new User();
                    user.setId(userobject.getInt("id"));
                    user.setUserName(userobject.getString("name")+ " "+ userobject.getString("lastname"));
                    user.setPhoto(userobject.getString("photo"));

                    Post post = new Post();
                    post.setUser(user);
                    post.setId(postobject.getInt("id"));
                    post.setSelfLike(false);
                    post.setPhoto(postobject.getString("photo"));
                    post.setDesc(postobject.getString("desc"));
                    post.setComment(0);
                    post.setLike(0);
                    post.setDate(postobject.getString("created_at"));

                    HomeFragment.arrayList.add(0, post);
                    HomeFragment.recyclerView.getAdapter().notifyItemInserted(0);
                    HomeFragment.recyclerView.getAdapter().notifyDataSetChanged();
                   // Toast.makeText(this, "Đăng thành thông", Toast.LENGTH_SHORT).show();
                    finish();
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
                map.put("desc", txtTextPost.getText().toString().trim());
                map.put("photo", bitmapToString(bitmap));
                return map;
            }
        };
        RequestQueue queue = Volley.newRequestQueue(AddpostActivity.this);
        queue.add(request);
    }
    private String bitmapToString(Bitmap bitmap) {
        if (bitmap != null){
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            byte [] array = byteArrayOutputStream.toByteArray();
            return android.util.Base64.encodeToString(array, Base64.DEFAULT);

        }
        return "";
    }
    public void chagePhoto(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, GALLERY_CHANGE_POST);
    }

    public void cancelPost(View view) {
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_CHANGE_POST && resultCode == RESULT_OK){
            Uri imgUrl = data.getData();
            imgPost.setImageURI(imgUrl);
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imgUrl);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
package com.example.blogapp.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.blogapp.Adapters.AccountPostAdapter;
import com.example.blogapp.Constaint;
import com.example.blogapp.Models.Post;
import com.example.blogapp.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class AccountFragment extends Fragment {
    private View view;
    private MaterialToolbar toolbar;
    private CircleImageView imgprofile;
    private TextView txtName, txtPostCount;
    private Button btnEditAccount;
    private RecyclerView recyclerView;
    private ArrayList<Post> arrayList;
    private SharedPreferences preferences;
    private AccountPostAdapter adapter;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.layout_account, container, false);
        init();
        return view;
    }

    private void init() {
        preferences = getContext().getSharedPreferences("user", Context.MODE_PRIVATE);
        toolbar = view.findViewById(R.id.toolbarAccount);
        imgprofile = view.findViewById(R.id.imgAccountProfile);
        txtName = view.findViewById(R.id.txtAccountName);
        txtPostCount = view.findViewById(R.id.txtAcountPostCount);
        recyclerView = view.findViewById(R.id.recyclerAccount);
        btnEditAccount = view.findViewById(R.id.btnEditAccount);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        getData();

    }

    private void getData() {
        arrayList = new ArrayList<>();
        StringRequest request = new StringRequest(Request.Method.GET, Constaint.MY_POST, response -> {
            try {
                JSONObject object = new JSONObject(response);
                if (object.getBoolean("success")){
                    JSONArray posts = object.getJSONArray("posts");
                    for (int i=0; i<posts.length(); i++){
                        JSONObject p = posts.getJSONObject(i);
                        Post post = new Post();
                        post.setPhoto(Constaint.URL+"storage/posts/"+p.getString("photo"));
                        arrayList.add(post);

                    }
                    JSONObject user = object.getJSONObject("user");
                    txtName.setText(user.getString("name")+" "+ user.getString("lastname"));
                    txtPostCount.setText(arrayList.size());
                    Picasso.get().load(Constaint.URL+"storage/profiles/"+user.getString("photo")).into(imgprofile);
                    adapter = new AccountPostAdapter(getContext(), arrayList);
                    recyclerView.setAdapter(adapter);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> {
            error.printStackTrace();
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                String token = preferences.getString("token", "");
                HashMap<String,String> map = new HashMap<>();
                map.put("Authorization", "Bearer "+token);
                return map;
            }



        };
        RequestQueue queue = Volley.newRequestQueue(getContext());
        queue.add(request);
    }
}

package com.example.blogapp.Fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.blogapp.AuthActivity;
import com.example.blogapp.Constaint;
import com.example.blogapp.HomeActivity;
import com.example.blogapp.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SignInFragment extends Fragment {
    private View view;
    private  TextInputLayout layoutEmail, layoutPassword;
    private TextInputEditText txtEmail, txtPassword;
    private   TextView txtSignup;
    private Button btn_Signin;
    private ProgressDialog dialog;

    public SignInFragment(){}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.layout_sign_in, container, false);
        init();
        return  view;
    }

    private void init() {
        layoutEmail = view.findViewById(R.id.txtLayoutEmailSignin);
        layoutPassword = view.findViewById(R.id.txtLayoutPasswordSignin);
        txtEmail = view.findViewById(R.id.txtEmailSignin);
        txtPassword = view.findViewById(R.id.txtPasswordSignin);
        txtSignup = view.findViewById(R.id.txtSignup);
        btn_Signin = view.findViewById(R.id.btn_Signin);
        dialog = new ProgressDialog(getContext());
        dialog.setCancelable(false);

        txtSignup.setOnClickListener(v->{
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frameAuthContainer, new SignUpFragment()).commit();
        });
        btn_Signin.setOnClickListener(v->{
            if (validate()){
                login();
            }
        });

        txtEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!txtEmail.getText().toString().isEmpty()){
                    layoutEmail.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        txtPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (txtPassword.getText().toString().length()>7){
                    layoutPassword.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }
    private boolean validate(){
        if (txtEmail.getText().toString().isEmpty()){
            layoutEmail.setErrorEnabled(true);
            layoutEmail.setError("Email không được để trống");
            return false;
        }
        if (txtPassword.getText().toString().length()<8){
            layoutPassword.setErrorEnabled(true);
            layoutPassword.setError("Mật khẩu tối thiểu 8 kí tự");
            return false;
        }
        return true;
    }

    private void login(){
        dialog.setMessage("Đang kiểm tra...");
        dialog.show();
        StringRequest request = new StringRequest(Request.Method.POST, Constaint.LOGIN, response -> {
        //get response if connection success
            try {
                JSONObject object = new JSONObject(response);
                if (object.getBoolean("success")){
                    JSONObject user = object.getJSONObject("user");
                    // make share preference user
                    SharedPreferences userPref = getActivity().getApplicationContext().getSharedPreferences("user",getContext().MODE_PRIVATE);
                    SharedPreferences.Editor editor = userPref.edit();
                    editor.putString("token", object.getString("token"));
                    editor.putString("name", user.getString("name"));
                    editor.putString("lastname", user.getString("lastname"));
                    editor.putInt("id", user.getInt("id"));
                    editor.putString("photo", user.getString("photo"));
                    editor.putBoolean("isLoggedIn", true);
                    editor.apply();
                    // if Success
                    startActivity(new Intent((AuthActivity)getContext(), HomeActivity.class));
                    ((AuthActivity) getContext()).finish();
                    Toast.makeText(getContext(), "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            dialog.dismiss();
        }, error -> {
            // error if connection not success
            error.printStackTrace();
            dialog.dismiss();
        }){
            // add parameters

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<>();
                map.put("email", txtEmail.getText().toString().trim());
                map.put("password", txtPassword.getText().toString());
                return map;
            }
        };
        // add request to RequestQueue
        RequestQueue queue = Volley.newRequestQueue(getContext());
        queue.add(request);
    }
}


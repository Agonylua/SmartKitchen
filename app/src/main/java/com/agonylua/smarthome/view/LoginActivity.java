package com.agonylua.smarthome.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.agonylua.smarthome.R;
import com.agonylua.smarthome.ViewModel.LoginViewModel;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    private LoginViewModel loginViewModel;
    //private TextInputEditText etUsername, etPassword;
    private TextInputEditText etPassword;
    private TextInputEditText etUsername;
    private Button btnLogin;
    private Button btnRegister;
    private ProgressBar progressBar;
    private String TAG = "Login";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        etUsername = findViewById(R.id.user_name);
        etPassword = findViewById(R.id.user_password);
        btnLogin = findViewById(R.id.login);

        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        observeViewModel();

        btnLogin.setOnClickListener(v -> {
            if (etUsername.getText() == null || etPassword.getText() == null) {
                Log.e(TAG, "用户名或密码为空");
            }
            String u = etUsername.getText().toString();
            String p = etPassword.getText().toString();
            loginViewModel.login(u, p);
        });
    }

    private void observeViewModel() {
        // 观察是否正在加载
//        loginViewModel.getIsLoading().observe(this, isLoading -> {
//            if (isLoading) {
//                progressBar.setVisibility(View.VISIBLE);
//                btnLogin.setEnabled(false);
//            } else {
//                progressBar.setVisibility(View.GONE);
//                btnLogin.setEnabled(true);
//            }
//        });

        // 观察登录成功
        loginViewModel.getLoginSuccessToken().observe(this, token -> {
            // 隐藏 loading (如果不希望在 ViewModel 里手动关，可以在这里关)
            //progressBar.setVisibility(View.GONE);

            // 保存 Token
            saveToken(token);

            Toast.makeText(this, "登录成功！", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        // 观察登录失败
        loginViewModel.getLoginErrorMsg().observe(this, errorMsg -> {
            //progressBar.setVisibility(View.GONE);
            btnLogin.setEnabled(true);
            Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
        });
    }

    private void saveToken(String token) {
        SharedPreferences sp = getSharedPreferences("SmartKitchenApp", MODE_PRIVATE);
        sp.edit().putString("jwt_token", token).apply();
    }

}

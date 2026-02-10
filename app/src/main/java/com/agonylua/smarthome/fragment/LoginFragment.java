package com.agonylua.smarthome.fragment;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.agonylua.smarthome.R;
import com.agonylua.smarthome.utils.TokenManager;
import com.agonylua.smarthome.viewModel.LoginViewModel;
import com.google.android.material.textfield.TextInputEditText;

public class LoginFragment extends Fragment {
    private LoginViewModel loginViewModel;
    private TextInputEditText etPassword;
    private TextInputEditText etUsername;
    private Button btnLogin;
    private Button btnRegister;
    private ProgressBar progressBar;
    private String TAG = "Login";
    private Context context;
    private TokenManager tokenManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_login, container, false);

        etUsername = root.findViewById(R.id.user_name);
        etPassword = root.findViewById(R.id.user_password);
        btnLogin = root.findViewById(R.id.login);
        progressBar = root.findViewById(R.id.progress_bar);

        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        observeViewModel();

        btnLogin.setOnClickListener(v -> {
            if (etUsername.getText() == null || etPassword.getText() == null) {
                Log.e(TAG, "用户名或密码为空");
                return;
            }
            String u = etUsername.getText().toString();
            String p = etPassword.getText().toString();
            loginViewModel.login(u, p);
        });
        return root;
    }

    private void observeViewModel() {
        // 观察是否正在加载
        loginViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading) {
                progressBar.setVisibility(View.VISIBLE);
                btnLogin.setText("");
                btnLogin.setEnabled(false);
            } else {
                progressBar.setVisibility(View.GONE);
                btnLogin.setText(R.string.login);
                btnLogin.setEnabled(true);
            }
        });

        // 观察登录成功
        loginViewModel.getLoginSuccessToken().observe(getViewLifecycleOwner(), token -> {
            progressBar.setVisibility(View.GONE);

            if (getContext() != null) {
                tokenManager = new TokenManager(getContext());
            }
            Log.d(TAG, "observeViewModel: " + token);
            tokenManager.saveToken(token);

            Toast.makeText(getContext(), "登录成功！", Toast.LENGTH_SHORT).show();
            if (getView() != null) {
                Navigation.findNavController(getView()).navigate(R.id.mainFragment);
            }
        });

        // 观察登录失败
        loginViewModel.getLoginErrorMsg().observe(getViewLifecycleOwner(), errorMsg -> {
            progressBar.setVisibility(View.GONE);
            btnLogin.setEnabled(true);
            Toast.makeText(getContext(), errorMsg, Toast.LENGTH_SHORT).show();
        });


    }
}

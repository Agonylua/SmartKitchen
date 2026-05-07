package com.agonylua.smartKitchen.view;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.agonylua.smartKitchen.R;
import com.agonylua.smartKitchen.model.MqttLiveBus;
import com.agonylua.smartKitchen.network.MqttManager;
import com.agonylua.smartKitchen.utils.NetworkMonitor;
import com.agonylua.smartKitchen.utils.SnackbarUtils;
import com.agonylua.smartKitchen.utils.UserManager;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {
    @Inject
    public NetworkMonitor networkMonitor;

    @Inject
    public MqttManager mqttManager;

    @Inject
    public UserManager userManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MqttLiveBus.getInstance().init(getApplicationContext());
        mqttManager.connect();
        initFragments();
        networkMonitor.startMonitoring();

        // 注册全局 Token 失效监听
        registerTokenExpiredObserver();
    }

    private void registerTokenExpiredObserver() {
        userManager.getTokenExpiredEvent().observe(this, isExpired -> {
            if (isExpired != null && isExpired) {
                NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);

                if (navController.getCurrentDestination() != null && navController.getCurrentDestination().getId() != R.id.loginFragment) {
                    SnackbarUtils.show(findViewById(android.R.id.content), "登录状态已过期，请重新登录");
                    navController.navigate(R.id.action_global_to_login);
                }
            }
        });
    }

    private void initFragments() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            if (userManager.getToken() == null || userManager.getToken().isEmpty()) {
                navController.navigate(R.id.loginFragment);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        networkMonitor.stopMonitoring();
        mqttManager.disconnect();
    }
}

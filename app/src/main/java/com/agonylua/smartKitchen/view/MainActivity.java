package com.agonylua.smartKitchen.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.agonylua.smartKitchen.R;
import com.agonylua.smartKitchen.model.MqttLiveBus;
import com.agonylua.smartKitchen.network.MqttManager;
import com.agonylua.smartKitchen.utils.NetworkMonitor;
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
    private final BroadcastReceiver logoutReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("com.agonylua.smartKitchen.ACTION_LOGOUT".equals(intent.getAction())) {
                userManager.clear();
                NavController navController = Navigation.findNavController(MainActivity.this, R.id.nav_host_fragment);
                if (navController.getCurrentDestination() != null && navController.getCurrentDestination().getId() != R.id.loginFragment) {
                    navController.navigate(R.id.action_global_to_login);
                }
            }
        }
    };
    private String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MqttLiveBus.getInstance().init(getApplicationContext());
        mqttManager.connect();
        initFragments();
        networkMonitor.startMonitoring();

        // 注册退出广播（401鉴权拦截）
        IntentFilter filter = new IntentFilter("com.agonylua.smartKitchen.ACTION_LOGOUT");
        ContextCompat.registerReceiver(this, logoutReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);
    }

    private void initFragments() {
        // 获取 NavHostFragment
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        // 获取 controller 进行跳转
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
        unregisterReceiver(logoutReceiver);
    }
}

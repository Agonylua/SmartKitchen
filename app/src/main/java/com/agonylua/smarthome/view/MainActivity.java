package com.agonylua.smarthome.view;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.agonylua.smarthome.R;
import com.agonylua.smarthome.model.MqttLiveBus;
import com.agonylua.smarthome.network.MqttManager;
import com.agonylua.smarthome.utils.NetworkMonitor;
import com.agonylua.smarthome.utils.UserManager;
import com.agonylua.smarthome.viewModel.HomeViewModel;

public class MainActivity extends AppCompatActivity {
    private NetworkMonitor networkMonitor;
    private HomeViewModel homeViewModel;
    private String TAG = "MainActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MqttLiveBus.getInstance().init(getApplicationContext());
        MqttManager.getInstance().connect(getApplication());

        initFragments();

        networkMonitor = new NetworkMonitor(getApplication());
        networkMonitor.startMonitoring();

    }

    private void initFragments() {
        // 获取 NavHostFragment
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        // 获取 controller 进行跳转
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            if (UserManager.getInstance(getApplication()).getToken() == null) {
                navController.navigate(R.id.loginFragment);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        networkMonitor.stopMonitoring();
    }
}

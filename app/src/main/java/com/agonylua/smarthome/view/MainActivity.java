package com.agonylua.smarthome.view;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.agonylua.smarthome.R;
import com.agonylua.smarthome.fragment.HomeFragment;
import com.agonylua.smarthome.fragment.MonitorFragment;
import com.agonylua.smarthome.fragment.SmartFragment;
import com.agonylua.smarthome.fragment.SplashFragment;
import com.agonylua.smarthome.fragment.UserFragment;
import com.agonylua.smarthome.model.MqttLiveBus;
import com.agonylua.smarthome.network.MqttManager;
import com.agonylua.smarthome.utils.NetworkMonitor;
import com.agonylua.smarthome.utils.TokenManager;
import com.agonylua.smarthome.utils.UserManager;
import com.agonylua.smarthome.viewModel.HomeViewModel;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private NetworkMonitor networkMonitor;
    private TokenManager tokenManager;
    private HomeViewModel homeViewModel;
    private String TAG = "MainActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MqttLiveBus.getInstance().init(getApplicationContext());
        MqttManager.getInstance().connect();
        tokenManager = new TokenManager(getApplication());

        initFragments();

        homeViewModel = new HomeViewModel(getApplication());
        homeViewModel.syncServiceData(UserManager.getInstance(getApplication()).getHomeId());

        networkMonitor = new NetworkMonitor(getApplicationContext());
        networkMonitor.startMonitoring();

    }

    private void initFragments() {
        // 获取 NavHostFragment
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        // 获取 controller 进行跳转
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            if (tokenManager.getToken() == null) {
                navController.navigate(R.id.loginFragment);
            }
        }
        // Init Fragment
        List<Fragment> fragments = new ArrayList<>();
        fragments.add(new SplashFragment());
        fragments.add(new HomeFragment());
        fragments.add(new SmartFragment());
        fragments.add(new MonitorFragment());
        fragments.add(new UserFragment());
    }

    /**
     * 显示全屏 Fragment 的方法
     *
     * @param fragment 要展示的全屏 Fragment
     */
    public void showSettingsFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        transaction.setCustomAnimations(
                android.R.anim.slide_in_left,
                android.R.anim.slide_out_right,
                android.R.anim.slide_in_left,
                android.R.anim.slide_out_right
        );

        transaction.add(android.R.id.content, fragment);

        transaction.addToBackStack("fullscreen_tag");

        transaction.commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        networkMonitor.stopMonitoring();
    }
}

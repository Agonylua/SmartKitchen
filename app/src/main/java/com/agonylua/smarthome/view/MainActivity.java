package com.agonylua.smarthome.view;

import android.os.Bundle;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.viewpager2.widget.ViewPager2;

import com.agonylua.smarthome.R;
import com.agonylua.smarthome.ViewModel.HomeViewModel;
import com.agonylua.smarthome.fragment.HomeFragment;
import com.agonylua.smarthome.fragment.MonitorFragment;
import com.agonylua.smarthome.fragment.SmartFragment;
import com.agonylua.smarthome.fragment.UserFragment;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private HomeViewModel homeViewModel;
    private ViewPager2 mViewPager2;
    private RadioGroup mRadioGroup;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        homeViewModel = new HomeViewModel(getApplication());
        // Init Fragment
        List<Fragment> fragments = new ArrayList<>();
        fragments.add(new HomeFragment());
        fragments.add(new SmartFragment());
        fragments.add(new MonitorFragment());
        fragments.add(new UserFragment());


        if (homeViewModel.validateToken()) {
            Toast.makeText(this, "登录成功！", Toast.LENGTH_SHORT).show();
            findViewById(R.layout.activity_main).post(() -> {
                NavController navController = Navigation.findNavController(findViewById(R.id.nav_host_fragment));
                navController.navigate(R.id.loginFragment);
            });
        }
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
}

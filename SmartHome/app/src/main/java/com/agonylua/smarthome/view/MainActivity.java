package com.agonylua.smarthome.view;

import android.os.Bundle;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.agonylua.smarthome.R;
import com.agonylua.smarthome.adapter.PagerAdapter;
import com.agonylua.smarthome.fragment.HomeFragment;
import com.agonylua.smarthome.fragment.MonitorFragment;
import com.agonylua.smarthome.fragment.SmartFragment;
import com.agonylua.smarthome.fragment.UserFragment;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ViewPager2 mViewPager2;
    private RadioGroup mRadioGroup;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化控件
        mViewPager2 = findViewById(R.id.viewPager);
        mRadioGroup = findViewById(R.id.radioGroup);

        // 初始化Fragment列表
        List<Fragment> fragments = new ArrayList<>();
        fragments.add(new HomeFragment());
        fragments.add(new SmartFragment());
        fragments.add(new MonitorFragment());
        fragments.add(new UserFragment());

        // 设置ViewPager2适配器
        PagerAdapter adapter = new PagerAdapter(this, fragments);
        mViewPager2.setAdapter(adapter);

        // 监听RadioGroup选中事件，切换ViewPager2页面
        mRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_home)
                mViewPager2.setCurrentItem(0);
            else if (checkedId == R.id.rb_smart)
                mViewPager2.setCurrentItem(1);
            else if (checkedId == R.id.rb_monitor)
                mViewPager2.setCurrentItem(2);
            else if (checkedId == R.id.rb_user) {
                mViewPager2.setCurrentItem(3);
            }
        });

        // 监听ViewPager2页面变化，同步更新RadioButton选中状态
        mViewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                switch (position) {
                    case 0:
                        mRadioGroup.check(R.id.rb_home);
                        break;
                    case 1:
                        mRadioGroup.check(R.id.rb_smart);
                        break;
                    case 2:
                        mRadioGroup.check(R.id.rb_monitor);
                        break;
                    case 3:
                        mRadioGroup.check(R.id.rb_user);
                        break;
                }
            }
        });

        // 默认选中首页
        mRadioGroup.check(R.id.rb_home);
    }
}

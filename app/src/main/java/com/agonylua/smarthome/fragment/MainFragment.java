package com.agonylua.smarthome.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.agonylua.smarthome.R;
import com.agonylua.smarthome.repository.MainRepository;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class MainFragment extends Fragment {

    private static final String TAG = "MainFragment";
    private ViewPager2 mViewPager2;
    private RadioGroup mRadioGroup;
    private MainRepository mainRepository;
    private int currentPosition = 0;
    private long mExitTime = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main_tab, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mViewPager2 = view.findViewById(R.id.viewPager);
        mRadioGroup = view.findViewById(R.id.radioGroup);

        // 注册返回键监听回调
        registerBackPressedCallback();

        // Adapter
        mViewPager2.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                switch (position) {
                    case 1:
                        return new SmartFragment();
                    case 2:
                        return new MonitorFragment();
                    case 3:
                        return new UserFragment();
                    case 0:
                    default:
                        return new HomeFragment();
                }
            }

            @Override
            public int getItemCount() {
                return 4;
            }
        });
        if (savedInstanceState != null) {
            currentPosition = savedInstanceState.getInt("saved_tab_index", 0);
        }

        mViewPager2.setCurrentItem(currentPosition, true);

        updateRadioButton(currentPosition);
        // RadioGroup
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

        // ViewPager
        mViewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                currentPosition = position;
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
    }

    private void updateRadioButton(int position) {
        int targetId = R.id.rb_home;
        switch (position) {
            case 0:
                targetId = R.id.rb_home;
                break;
            case 1:
                targetId = R.id.rb_smart;
                break;
            case 2:
                targetId = R.id.rb_monitor;
                break;
            case 3:
                targetId = R.id.rb_user;
                break;
        }
        if (mRadioGroup.getCheckedRadioButtonId() != targetId) {
            mRadioGroup.check(targetId);
        }
    }

    /**
     * 注册处理系统返回按键的回调
     */
    private void registerBackPressedCallback() {
        // 使用 getViewLifecycleOwner() 可以保证当 Fragment 视图销毁时自动移除该回调，防止内存泄漏
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // 判断当前是否在首页
                if (mViewPager2.getCurrentItem() != 0) {
                    mViewPager2.setCurrentItem(0);
                } else {
                    // 执行退出的逻辑
                    if ((System.currentTimeMillis() - mExitTime) > 2000) {
                        Toast.makeText(requireContext(), "再按一次退出", Toast.LENGTH_SHORT).show();
                        mExitTime = System.currentTimeMillis();
                    } else {
                        requireActivity().finish();
                    }
                }
            }
        });
    }

    /**
     * 系统会在 Fragment 销毁 View 时调用此方法保存数据
     */
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // 保存当前的 Tab 索引
        outState.putInt("saved_tab_index", currentPosition);
    }

}
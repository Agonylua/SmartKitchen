package com.agonylua.smarthome.fragment;

import static com.agonylua.smarthome.network.MqttManager.SUB_TOPIC;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.agonylua.smarthome.R;
import com.agonylua.smarthome.network.MqttManager;
import com.agonylua.smarthome.repository.MainRepository;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class MainFragment extends Fragment {

    private static final String TAG = "MainFragment";
    private ViewPager2 mViewPager2;
    private RadioGroup mRadioGroup;
    private MainRepository mainRepository;
    private int currentPosition = 0;

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

        mainRepository = new MainRepository(getContext());
        MqttManager.getInstance().setOnMessageListener(new MqttManager.OnMessageListener() {
            @Override
            public void onMessage(String topic, String message) {
                String sn = topic.substring(SUB_TOPIC.length());
                Log.d(TAG, "onViewCreated: " + sn + " message: " + message);
                // 在这里处理接收到的 MQTT 消息
                mainRepository.updateDeviceData(sn, message);

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

}
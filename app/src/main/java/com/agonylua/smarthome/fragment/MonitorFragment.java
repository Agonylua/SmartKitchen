package com.agonylua.smarthome.fragment;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.agonylua.smarthome.adapter.MonitorAdapter;
import com.agonylua.smarthome.databinding.FragmentMonitorBinding;
import com.agonylua.smarthome.viewModel.MonitorViewModel;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;

public class MonitorFragment extends Fragment {

    private FragmentMonitorBinding binding;
    private MonitorViewModel monitorViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMonitorBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        monitorViewModel = new ViewModelProvider(this).get(MonitorViewModel.class);

        // 初始化 RecyclerView (Adapter 绑定交给 XML, 我们只需给它一个实例和布局管理器)
        binding.rvRunningDevices.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvRunningDevices.setAdapter(new MonitorAdapter());

        binding.setLifecycleOwner(getViewLifecycleOwner());
        binding.setViewModel(monitorViewModel);


        observeChartData();
        setupChart();
    }

    private void observeChartData() {
        monitorViewModel.getOnlineDevices().observe(getViewLifecycleOwner(), devices -> {
            boolean hasData = devices != null && !devices.isEmpty();
            binding.setHasRunningDevice(hasData);
        });
    }

    private void setupChart() {
        // [图表初始化代码保持不变，与上一版本一致，略]
        binding.lineChartPower.getDescription().setEnabled(false);
        binding.lineChartPower.getLegend().setEnabled(false);
        binding.lineChartPower.setDrawGridBackground(false);
        binding.lineChartPower.setDrawBorders(false);
        binding.lineChartPower.setTouchEnabled(true);
        binding.lineChartPower.setScaleEnabled(false);

        XAxis xAxis = binding.lineChartPower.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setTextColor(Color.parseColor("#9CA3AF"));

        binding.lineChartPower.getAxisRight().setEnabled(false);
        YAxis yAxis = binding.lineChartPower.getAxisLeft();
        yAxis.setDrawAxisLine(false);
        yAxis.setDrawGridLines(true);
        yAxis.setGridColor(Color.parseColor("#F3F4F6"));
        yAxis.setTextColor(Color.parseColor("#9CA3AF"));

        setMockChartData();
    }

    private void setMockChartData() {
        // [图表数据模拟保持不变，略]
        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry(1, 1.2f));
        entries.add(new Entry(2, 2.5f));
        entries.add(new Entry(3, 1.8f));
        entries.add(new Entry(4, 3.2f));
        entries.add(new Entry(5, 4.0f));
        entries.add(new Entry(6, 2.1f));
        entries.add(new Entry(7, 3.5f));

        LineDataSet dataSet = new LineDataSet(entries, "用电量");
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setCubicIntensity(0.2f);
        dataSet.setColor(Color.parseColor("#14B8A6"));
        dataSet.setLineWidth(3f);
        dataSet.setDrawFilled(true);
        GradientDrawable gradientDrawable = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{Color.parseColor("#6614B8A6"), Color.parseColor("#0014B8A6")}
        );
        dataSet.setFillDrawable(gradientDrawable);
        dataSet.setDrawValues(false);

        binding.lineChartPower.setData(new LineData(dataSet));
        binding.lineChartPower.animateX(1000);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
package com.agonylua.smartKitchen.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.agonylua.smartKitchen.adapter.MonitorAdapter;
import com.agonylua.smartKitchen.databinding.FragmentMonitorBinding;
import com.agonylua.smartKitchen.dto.DevicePowerDTO;
import com.agonylua.smartKitchen.viewModel.MonitorViewModel;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
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

        // 初始化 RecyclerView
        binding.rvRunningDevices.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvRunningDevices.setAdapter(new MonitorAdapter());

        binding.setLifecycleOwner(getViewLifecycleOwner());
        binding.setViewModel(monitorViewModel);

        //monitorViewModel.refreshData();
        observeChartData();
        setupChart();
        binding.refreshLayout.setOnRefreshListener(refreshLayout -> {
            monitorViewModel.refreshData();
        });
    }

    private void observeChartData() {
        monitorViewModel.getOnlineDevices().observe(getViewLifecycleOwner(), devices -> {
            boolean hasData = devices != null && !devices.isEmpty();
            binding.setHasRunningDevice(hasData);
        });

        monitorViewModel.getPowerData().observe(getViewLifecycleOwner(), this::setMockChartData);

        monitorViewModel.refreshResult.observe(getViewLifecycleOwner(), success -> {
            binding.refreshLayout.finishRefresh(success);
        });

    }

    private void setupChart() {
        binding.lineChartPower.getDescription().setEnabled(false);
        binding.lineChartPower.getLegend().setEnabled(false);
        binding.lineChartPower.setDrawGridBackground(false);
        binding.lineChartPower.setDrawBorders(false);
        binding.lineChartPower.setTouchEnabled(true);
        binding.lineChartPower.setScaleEnabled(false);
        binding.lineChartPower.animateX(1000);
        binding.lineChartPower.getAxisRight().setEnabled(false);

        XAxis xAxis = binding.lineChartPower.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setTextColor(Color.parseColor("#9CA3AF"));
        xAxis.setGranularity(1f); // 标签间隔为1
        xAxis.setAxisMinimum(-0.1f); // 设置X轴起点的最小值为负数，与Y轴拉开一点距离

        YAxis yAxis = binding.lineChartPower.getAxisLeft();
        yAxis.setAxisMinimum(0f); // 最小值为0
        yAxis.setDrawGridLines(true);
        yAxis.setGridColor(Color.parseColor("#F3F4F6"));
        yAxis.setTextColor(Color.parseColor("#9CA3AF"));

    }

    private void setMockChartData(List<DevicePowerDTO> dataList) {
        if (dataList == null || dataList.isEmpty()) {
            binding.lineChartPower.clear();
            return;
        }

        ArrayList<Entry> entries = new ArrayList<>();
        ArrayList<String> xLabels = new ArrayList<>();

        for (int i = 0; i < dataList.size(); i++) {
            DevicePowerDTO item = dataList.get(i);
            entries.add(new Entry(i, item.totalKwh));
            String shortDate = item.date != null && item.date.length() >= 10
                    ? item.date.substring(5) : item.date;
            xLabels.add(shortDate);
        }

        binding.lineChartPower.getXAxis().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                if (index >= 0 && index < xLabels.size()) {
                    return xLabels.get(index);
                }
                return "";
            }
        });

        LineDataSet dataSet = new LineDataSet(entries, "日功耗");

        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        int tealColor = Color.parseColor("#00BFA5");
        dataSet.setColor(tealColor);
        dataSet.setLineWidth(3f);
        dataSet.setCircleColor(tealColor);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawCircleHole(true);
        dataSet.setCircleHoleColor(Color.WHITE);

        dataSet.setDrawFilled(true);
        dataSet.setFillColor(tealColor);
        dataSet.setFillAlpha(50);

        dataSet.setValueTextSize(10f);
        dataSet.setValueTextColor(Color.DKGRAY);
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format("%.2f 度", value);
            }
        });

        LineData lineData = new LineData(dataSet);
        binding.lineChartPower.setData(lineData);
        binding.lineChartPower.invalidate();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
package com.agonylua.smarthome.fragment;

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

import com.agonylua.smarthome.adapter.MonitorAdapter;
import com.agonylua.smarthome.databinding.FragmentMonitorBinding;
import com.agonylua.smarthome.dto.DevicePowerDTO;
import com.agonylua.smarthome.viewModel.MonitorViewModel;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

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

        monitorViewModel.refreshData();
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
            // 截取日期字符串，例如 "2023-10-25" -> "10-25"
            String shortDate = item.date != null && item.date.length() >= 10
                    ? item.date.substring(5) : item.date;
            xLabels.add(shortDate);
        }

        // 自定义 X 轴显示
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

        // 配置折线数据集
        LineDataSet dataSet = new LineDataSet(entries, "日功耗");

        // 关键美化：设置为平滑的贝塞尔曲线
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        // 颜色配置 (使用项目的主题色 Teal)
        int tealColor = Color.parseColor("#00BFA5");
        dataSet.setColor(tealColor);
        dataSet.setLineWidth(3f); // 线条宽度
        dataSet.setCircleColor(tealColor);
        dataSet.setCircleRadius(4f); // 数据点圆圈大小
        dataSet.setDrawCircleHole(true);
        dataSet.setCircleHoleColor(Color.WHITE);

        // 关键美化：开启下方区域填充渐变色
        dataSet.setDrawFilled(true);
        // 如果你有 bg_app_gradient.xml，可以用 ContextCompat.getDrawable() 替换，这里用纯色带透明度示例
        dataSet.setFillColor(tealColor);
        dataSet.setFillAlpha(50); // 透明度 0-255

        // 数据点上的文本标签加上 "度"
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
        binding.lineChartPower.invalidate(); // 刷新图表
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
package com.agonylua.smartKitchen.adapter;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import androidx.databinding.BindingAdapter;

import com.agonylua.smartKitchen.R;
import com.agonylua.smartKitchen.viewModel.DeviceViewModel;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.List;

public class ChipBindingAdapters {
    private static final String TAG = "ChipBindingAdapters";

    @BindingAdapter("app:tint")
    public static void setTintColor(ImageView imageView, int color) {
        imageView.setImageTintList(ColorStateList.valueOf(color));
    }

    @BindingAdapter(value = {"chipItems", "viewModel"})
    public static void setChipItems(ChipGroup chipGroup, List<String> items, DeviceViewModel viewModel) {
        if (items == null) return;
        chipGroup.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(chipGroup.getContext());
        String currentSelection = viewModel.getSelectedModeLiveData().getValue();
        for (String itemText : items) {
            Chip chip = (Chip) inflater.inflate(R.layout.item_chip, chipGroup, false);
            chip.setId(View.generateViewId());
            chip.setText(itemText);

            if (itemText.equals(currentSelection)) {
                chip.setChecked(true);
            }

            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    viewModel.getSelectedModeLiveData().setValue(itemText);
                }
            });

            chipGroup.addView(chip);
        }
    }
}
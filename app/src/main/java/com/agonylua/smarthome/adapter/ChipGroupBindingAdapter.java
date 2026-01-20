package com.agonylua.smarthome.adapter;

import android.util.Log;

import androidx.databinding.BindingAdapter;
import androidx.databinding.InverseBindingAdapter;
import androidx.databinding.InverseBindingListener;

import com.google.android.material.chip.ChipGroup;

public class ChipGroupBindingAdapter {

    @InverseBindingAdapter(attribute = "checkedChip")
    public static int getCheckedChipId(ChipGroup view) {
        return view.getCheckedChipId();
    }

    @BindingAdapter("checkedChip")
    public static void setCheckedChipId(ChipGroup view, int id) {
        if (view.getCheckedChipId() != id) {
            Log.d("chip", "setCheckedChipId: " + id);
            view.check(id);
        }
    }

    @BindingAdapter("checkedChipAttrChanged")
    public static void setListeners(ChipGroup view, final InverseBindingListener attrChange) {
        view.setOnCheckedStateChangeListener((group, checkedIds) -> {
            // 注意：新版 Material 库这里 checkedIds 是个 List
            // 简单的单选逻辑：
            if (attrChange != null) {
                attrChange.onChange();
            }
        });
    }
}
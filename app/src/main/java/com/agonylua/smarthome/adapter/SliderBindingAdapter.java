package com.agonylua.smarthome.adapter;

import androidx.annotation.NonNull;
import androidx.databinding.BindingAdapter;
import androidx.databinding.InverseBindingAdapter;
import androidx.databinding.InverseBindingListener;

import com.google.android.material.slider.Slider;

public class SliderBindingAdapter {

    @BindingAdapter("android:value")
    public static void setSliderValue(Slider slider, float value) {
        if (slider.getValue() != value) {
            slider.setValue(value);
        }
    }

    @InverseBindingAdapter(attribute = "android:value")
    public static float getSliderValue(Slider slider) {
        return slider.getValue();
    }

    @BindingAdapter("android:valueAttrChanged")
    public static void setSliderListeners(Slider slider, final InverseBindingListener attrChange) {
        slider.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
                // No action needed on start
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                if (attrChange != null) {
                    attrChange.onChange();
                }
            }
        });
    }

    @BindingAdapter("thumbColor")
    public static void setThumbColor(Slider slider, int color) {
        slider.setThumbTintList(android.content.res.ColorStateList.valueOf(color));
    }

    @BindingAdapter("trackColorActive")
    public static void setTrackActiveColor(Slider slider, int color) {
        slider.setTrackActiveTintList(android.content.res.ColorStateList.valueOf(color));
    }
}
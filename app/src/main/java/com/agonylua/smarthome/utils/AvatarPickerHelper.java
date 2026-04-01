package com.agonylua.smarthome.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import com.yalantis.ucrop.UCrop;

import java.io.File;

import top.zibin.luban.Luban;
import top.zibin.luban.OnCompressListener;

public class AvatarPickerHelper {

    private final Fragment fragment;       // 替换为 Fragment
    private final Context context;         // 缓存 Context
    private final OnAvatarProcessListener listener;

    private ActivityResultLauncher<PickVisualMediaRequest> pickMediaLauncher;
    private ActivityResultLauncher<Intent> cropLauncher;

    /**
     * 改造后的构造函数，接收 Fragment
     */
    public AvatarPickerHelper(Fragment fragment, OnAvatarProcessListener listener) {
        this.fragment = fragment;
        this.context = fragment.requireContext(); // 获取安全的 Context
        this.listener = listener;
        initLaunchers();
    }

    private void initLaunchers() {
        // 使用 fragment 来注册启动器
        pickMediaLauncher = fragment.registerForActivityResult(
                new ActivityResultContracts.PickVisualMedia(),
                uri -> {
                    if (uri != null) {
                        startCrop(uri);
                    } else {
                        listener.onError("用户取消了选择");
                    }
                }
        );

        cropLauncher = fragment.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri croppedUri = UCrop.getOutput(result.getData());
                        if (croppedUri != null) {
                            startCompress(new File(croppedUri.getPath()));
                        }
                    } else if (result.getResultCode() == UCrop.RESULT_ERROR && result.getData() != null) {
                        Throwable cropError = UCrop.getError(result.getData());
                        listener.onError(cropError != null ? cropError.getMessage() : "裁剪发生未知错误");
                    }
                }
        );
    }

    public void startSelection() {
        pickMediaLauncher.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }

    private void startCrop(Uri sourceUri) {
        // 使用 context.getCacheDir()
        String destinationFileName = "avatar_crop_" + System.currentTimeMillis() + ".jpg";
        Uri destinationUri = Uri.fromFile(new File(context.getCacheDir(), destinationFileName));

        UCrop uCrop = UCrop.of(sourceUri, destinationUri)
                .withAspectRatio(1, 1)
                .withMaxResultSize(500, 500);

        UCrop.Options options = new UCrop.Options();
        options.setHideBottomControls(true);
        options.setFreeStyleCropEnabled(false);
        uCrop.withOptions(options);

        // 使用 context 获取 Intent
        Intent intent = uCrop.getIntent(context);
        cropLauncher.launch(intent);
    }

    private void startCompress(File croppedFile) {
        // 使用 context 启动 Luban
        Luban.with(context)
                .load(croppedFile)
                .ignoreBy(100)
                .setTargetDir(context.getCacheDir().getAbsolutePath())
                .setCompressListener(new OnCompressListener() {
                    @Override
                    public void onStart() {
                        listener.onProcessStart();
                    }

                    @Override
                    public void onSuccess(File compressedFile) {
                        if (croppedFile.exists() && !croppedFile.getAbsolutePath().equals(compressedFile.getAbsolutePath())) {
                            croppedFile.delete();
                        }
                        listener.onSuccess(compressedFile);
                    }

                    @Override
                    public void onError(Throwable e) {
                        listener.onError("图片压缩失败: " + e.getMessage());
                    }
                }).launch();
    }

    public interface OnAvatarProcessListener {
        void onProcessStart();

        void onSuccess(File compressedImageFile);

        void onError(String errorMessage);
    }
}

package com.agonylua.smartkitchen.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 获取本地文件夹的绝对路径
        File directory = new File(uploadDir);
        String absolutePath = directory.getAbsolutePath();

        // 确保路径以文件分隔符结尾
        if (!absolutePath.endsWith(File.separator)) {
            absolutePath += File.separator;
        }

        // 将 /avatars/** 虚拟路径映射到本地的绝对物理路径，方便直接在浏览器或前端访问
        registry.addResourceHandler("/avatars/**")
                .addResourceLocations("file:" + absolutePath);
    }
}

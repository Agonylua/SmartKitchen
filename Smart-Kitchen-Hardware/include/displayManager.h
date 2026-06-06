#ifndef DISPLAY_MANAGER_H
#define DISPLAY_MANAGER_H

#include <U8g2lib.h>
#include <Wire.h>
#include "common.h"
#include "wifiManager.h"
#include "dht11.h"

class DisplayManager
{
private:
    U8G2_SSD1306_128X64_NONAME_F_HW_I2C *u8g2; // U8g2 实例对象

    // 状态记录
    bool showSplash = true;
    unsigned long splashStartTime = 0;

public:
    DisplayManager();
    void begin();

    // 核心刷新函数，根据状态绘制不同界面
    void update(int status);

private:
    void drawStatusScreen(String title, String info);
    void drawMainScreen(float fridgeTemp, float freezeTemp);
    void drawSplashScreen();

    // 辅助获取模式对应的短文本
    String getModeText(DeviceMode mode);
};

extern DisplayManager displayMgr;

#endif
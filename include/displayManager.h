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
    U8G2_SSD1306_128X64_NONAME_F_HW_I2C *u8g2; // 根据你的具体屏幕型号调整

    // 温度变化追踪
    float lastFridgeTemp = 0;
    float lastFreezeTemp = 0;
    unsigned long lastTempUpdate = 0;
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
    void drawWiFiIcon(int x, int y, bool connected);
    void drawMQTTIcon(int x, int y, bool connected);
    void drawUpArrow(int x, int y);
    void drawDownArrow(int x, int y);
    String getModeText(DeviceMode mode);
};

extern DisplayManager displayMgr;

#endif
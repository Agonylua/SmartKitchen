#ifndef DISPLAY_MANAGER_H
#define DISPLAY_MANAGER_H

#include <U8g2lib.h>
#include <Wire.h>
#include <qrcode.h> // 引入 QRCode 库
#include "common.h"
#include "wifiManager.h"

class DisplayManager
{
private:
    U8G2_SSD1306_128X64_NONAME_F_HW_I2C *u8g2; // 根据你的具体屏幕型号调整
    QRCode qrcode;
    uint8_t *qrcodeBytes; // 动态分配二维码缓存

public:
    DisplayManager();
    void begin();

    // 核心刷新函数，根据状态绘制不同界面
    void update(int status);

private:
    void drawQRCode(String text);
    void drawStatusScreen(String title, String info);
    void drawMainScreen(String temp, String humidity);
};

extern DisplayManager displayMgr;

#endif
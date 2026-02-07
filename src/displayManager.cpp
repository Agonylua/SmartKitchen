#include "displayManager.h"

// I2C 引脚，根据你的 ESP32-S3 开发板实际连线修改 (SDA, SCL)
#define OLED_SDA 42
#define OLED_SCL 41

DisplayManager::DisplayManager()
{
    // 构造函数，实例化 U8g2
    u8g2 = new U8G2_SSD1306_128X64_NONAME_F_HW_I2C(U8G2_R0, U8X8_PIN_NONE, OLED_SCL, OLED_SDA);
}

void DisplayManager::begin()
{
    u8g2->begin();
    u8g2->enableUTF8Print(); // 启用 UTF8 支持

    // 初始化二维码缓冲区 (Version 3, ECC Low 足够容纳 SN 码)
    // 缓冲区大小公式：((version * 4 + 17) * (version * 4 + 17) + 7) / 8 + 1
    qrcodeBytes = new uint8_t[qrcode_getBufferSize(3)];
}

void DisplayManager::update(int status)
{
    u8g2->clearBuffer();
    preferences.begin("config", false);
    bool isBind = preferences.getBool("isBind", false);
    preferences.end();
    if (!wifi.isConnected() && isBind)
    {
        Serial.println("[Display] Showing QR Code for provisioning.");
        drawQRCode(DEV_SN);
        u8g2->sendBuffer();
        return;
    }
    switch (status)
    {
    case 0:

        drawMainScreen("24.5C", "45%");
        u8g2->sendBuffer();
        break;
    case -1:

        drawStatusScreen("Resetting...", "Please wait");
        u8g2->sendBuffer();
        break;
    default:
        break;
    }
}

void DisplayManager::drawQRCode(String text)
{
    // 1. 生成二维码数据
    // Version 3 (29x29 modules), Error Correction Level ECC_LOW
    qrcode_initText(&qrcode, qrcodeBytes, 3, ECC_LOW, text.c_str());

    // 2. 计算绘制位置 (居中)
    // 每个 module 放大 2 倍 (scale = 2)
    int scale = 2;
    int qrSize = qrcode.size * scale;
    int offsetX = (128 - qrSize) / 2;
    int offsetY = (64 - qrSize) / 2;

    // 3. 绘制二维码
    for (uint8_t y = 0; y < qrcode.size; y++)
    {
        for (uint8_t x = 0; x < qrcode.size; x++)
        {
            if (qrcode_getModule(&qrcode, x, y))
            {
                u8g2->drawBox(offsetX + x * scale, offsetY + y * scale, scale, scale);
            }
        }
    }

    // 4. 底部提示文字
    u8g2->setFont(u8g2_font_5x7_tf);
    u8g2->setCursor(0, 64);
    u8g2->print("Scan to Bind");
}

void DisplayManager::drawStatusScreen(String title, String info)
{
    u8g2->setFont(u8g2_font_ncenB10_tr);
    u8g2->setCursor(5, 20);
    u8g2->print(title);

    u8g2->setFont(u8g2_font_6x10_tf);
    u8g2->setCursor(5, 40);
    u8g2->print(info);
}

void DisplayManager::drawMainScreen(String temp, String hum)
{
    u8g2->setFont(u8g2_font_ncenB14_tr);
    u8g2->setCursor(10, 30);
    u8g2->print(temp);

    u8g2->setFont(u8g2_font_ncenB10_tr);
    u8g2->setCursor(80, 30);
    u8g2->print(hum);

    u8g2->setFont(u8g2_font_5x7_tf);
    u8g2->setCursor(30, 60);
    u8g2->print("Smart Kitchen");
}

DisplayManager displayMgr;
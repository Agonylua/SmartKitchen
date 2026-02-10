#include "displayManager.h"
#include "mqttConfig.h"
#include "control.h"

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
    splashStartTime = millis();
    showSplash = true;
}

void DisplayManager::update(int status)
{
    u8g2->clearBuffer();

    // 启动画面显示3秒
    if (showSplash && (millis() - splashStartTime < 3000))
    {
        drawSplashScreen();
        u8g2->sendBuffer();
        return;
    }
    showSplash = false;

    switch (status)
    {
    case 0:
    {
        float fridgeTemp = readFridgeTemp();
        float freezeTemp = readFreezeTemp();
        drawMainScreen(fridgeTemp, freezeTemp);
        u8g2->sendBuffer();
    }
    break;
    case -1:
        drawStatusScreen("Resetting...", "Please wait");
        u8g2->sendBuffer();
        break;
    default:
        break;
    }
}

void DisplayManager::drawSplashScreen()
{
    // 显示SmartKitchen大字体
    u8g2->setFont(u8g2_font_logisoso16_tr);

    // 计算文本宽度以居中显示
    const char *line1 = "Smart";
    const char *line2 = "Kitchen";

    int width1 = u8g2->getStrWidth(line1);
    int width2 = u8g2->getStrWidth(line2);

    int x1 = (128 - width1) / 2;
    int x2 = (128 - width2) / 2;

    u8g2->drawStr(x1, 28, line1);
    u8g2->drawStr(x2, 50, line2);

    // 绘制装饰线
    u8g2->drawHLine(10, 10, 108);
    u8g2->drawHLine(10, 54, 108);
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

void DisplayManager::drawMainScreen(float fridgeTemp, float freezeTemp)
{
    // 顶部状态栏
    // WiFi图标 (左上角)
    extern WiFiConnector wifi;
    drawWiFiIcon(2, 2, wifi.isConnected());

    // MQTT图标 (WiFi图标右侧)
    drawMQTTIcon(20, 2, mqttIsConnected());

    // 模式显示 (右上角)
    extern DeviceMode currentMode;
    String modeText = getModeText(currentMode);
    u8g2->setFont(u8g2_font_5x7_tr);
    int modeWidth = u8g2->getStrWidth(modeText.c_str());
    u8g2->drawStr(128 - modeWidth - 2, 9, modeText.c_str());

    // 分隔线
    u8g2->drawHLine(0, 12, 128);

    // === 左侧：冷藏温度 ===
    u8g2->setFont(u8g2_font_6x10_tr);
    u8g2->drawStr(8, 22, "Fridge");

    u8g2->setFont(u8g2_font_logisoso16_tr);
    char fridgeTempStr[8];
    dtostrf(fridgeTemp, 4, 1, fridgeTempStr);
    u8g2->drawStr(5, 40, fridgeTempStr);

    u8g2->setFont(u8g2_font_6x10_tr);
    u8g2->drawStr(5, 52, "C");
    u8g2->drawCircle(3, 46, 2); // 度符号

    // 冷藏温度上下操作箭头 (状态指示)
    if (isFridgeCooling)
    {
        drawDownArrow(32, 58);
    }
    else if (isFridgeWarming)
    {
        drawUpArrow(32, 58);
    }

    // === 中间分隔线 ===
    u8g2->drawVLine(64, 14, 50);

    // === 右侧：冷冻温度 ===
    u8g2->setFont(u8g2_font_6x10_tr);
    u8g2->drawStr(72, 22, "Freeze");

    u8g2->setFont(u8g2_font_logisoso16_tr);
    char freezeTempStr[8];
    dtostrf(freezeTemp, 4, 1, freezeTempStr);
    u8g2->drawStr(69, 40, freezeTempStr);

    u8g2->setFont(u8g2_font_6x10_tr);
    u8g2->drawStr(69, 52, "C");
    u8g2->drawCircle(67, 46, 2); // 度符号

    // 冷冻温度上下操作箭头
    if (isFreezeCooling)
    {
        drawDownArrow(96, 58);
    }
    else if (isFreezeWarming)
    {
        drawUpArrow(96, 58);
    }
}

void DisplayManager::drawWiFiIcon(int x, int y, bool connected)
{
    if (connected)
    {
        // WiFi信号图标 (已连接)
        u8g2->drawPixel(x + 7, y);
        u8g2->drawLine(x + 5, y + 2, x + 9, y + 2);
        u8g2->drawLine(x + 3, y + 4, x + 11, y + 4);
        u8g2->drawLine(x + 1, y + 6, x + 13, y + 6);
        u8g2->drawDisc(x + 7, y + 9, 1);
    }
    else
    {
        // WiFi断开图标 (X)
        u8g2->drawLine(x, y, x + 8, y + 8);
        u8g2->drawLine(x + 8, y, x, y + 8);
    }
}

void DisplayManager::drawMQTTIcon(int x, int y, bool connected)
{
    if (connected)
    {
        // M字形图标表示MQTT已连接
        u8g2->drawLine(x, y + 8, x, y);
        u8g2->drawLine(x, y, x + 3, y + 4);
        u8g2->drawLine(x + 3, y + 4, x + 6, y);
        u8g2->drawLine(x + 6, y, x + 6, y + 8);
    }
    else
    {
        // 方框+X表示断开
        u8g2->drawFrame(x, y, 8, 8);
        u8g2->drawLine(x + 2, y + 2, x + 6, y + 6);
        u8g2->drawLine(x + 6, y + 2, x + 2, y + 6);
    }
}

void DisplayManager::drawUpArrow(int x, int y)
{
    // 向上大箭头 (实心三角形)
    // x, y 为中心点
    u8g2->drawTriangle(x, y - 5, x - 6, y + 5, x + 6, y + 5);
}

void DisplayManager::drawDownArrow(int x, int y)
{
    // 向下大箭头
    // x, y 为中心点
    u8g2->drawTriangle(x, y + 5, x - 6, y - 5, x + 6, y - 5);
}

String DisplayManager::getModeText(DeviceMode mode)
{
    switch (mode)
    {
    case DeviceMode::STANDARD:
        return "STD";
    case DeviceMode::FAST_COOL:
        return "COOL";
    case DeviceMode::ENERGY_SAVING:
        return "ECO";
    case DeviceMode::HOLIDAY:
        return "VAC";
    default:
        return "---";
    }
}

DisplayManager displayMgr;
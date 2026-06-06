#include "displayManager.h"
#include "mqttConfig.h"
#include "control.h"

// I2C 引脚，根据你的 ESP32-S3 开发板实际连线修改 (SDA, SCL)
#define OLED_SDA 42
#define OLED_SCL 41

// ======================= 精美 XBM 像素图标资源 =======================
// 16x16 WiFi 图标
static const unsigned char icon_wifi_16[] U8X8_PROGMEM = {
    0x00, 0x00, 0x00, 0x00, 0xe0, 0x07, 0xf8, 0x1f, 0x1c, 0x38, 0x06, 0x60, 0xf0, 0x0f, 0x38,
    0x1c, 0x0c, 0x30, 0xc0, 0x03, 0xe0, 0x07, 0x00, 0x00, 0x80, 0x01, 0x80, 0x01, 0x00, 0x00,
    0x00, 0x00};

// 16x16 MQTT(云端) 图标
static const unsigned char icon_cloud_16[] U8X8_PROGMEM = {
    0x00, 0x00, 0x00, 0x00, 0xc0, 0x03, 0xe0, 0x07, 0x70, 0x0e, 0x38, 0x1c, 0x1c, 0x3e, 0x0e,
    0x77, 0xfe, 0xe3, 0xff, 0xc1, 0xff, 0xc1, 0xff, 0xe3, 0xfe, 0x7f, 0x00, 0x00, 0x00, 0x00,
    0x00, 0x00};

// 12x12 雪花图标 (制冷中)
static const unsigned char icon_snow_12[] U8X8_PROGMEM = {
    0x40, 0x00, 0x48, 0x04, 0x54, 0x0a, 0x22, 0x01, 0xdb, 0x0d, 0x7e, 0x03, 0x7e, 0x03, 0xdb,
    0x0d, 0x22, 0x01, 0x54, 0x0a, 0x48, 0x04, 0x40, 0x00};

// 12x12 热浪图标 (加热中)
static const unsigned char icon_heat_12[] U8X8_PROGMEM = {
    0x24, 0x02, 0x24, 0x02, 0x48, 0x04, 0x48, 0x04, 0x90, 0x08, 0x90, 0x08, 0x24, 0x02, 0x24,
    0x02, 0x48, 0x04, 0x48, 0x04, 0x90, 0x08, 0x90, 0x08};
// ====================================================================

DisplayManager::DisplayManager()
{
    // 实例化 U8g2
    u8g2 = new U8G2_SSD1306_128X64_NONAME_F_HW_I2C(U8G2_R0, U8X8_PIN_NONE, OLED_SCL, OLED_SDA);
}

void DisplayManager::begin()
{
    u8g2->begin();
    u8g2->enableUTF8Print();
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
        drawStatusScreen("System Reset", "Please wait...");
        u8g2->sendBuffer();
        break;
    default:
        break;
    }
}

void DisplayManager::drawSplashScreen()
{
    // 0.96寸专属：边框更加紧凑，字号对比更强烈
    u8g2->drawRFrame(0, 0, 128, 64, 4);
    u8g2->drawRFrame(2, 2, 124, 60, 2);

    u8g2->setFont(u8g2_font_helvB12_tf); // 主标题粗体
    int w1 = u8g2->getStrWidth("SMART");
    u8g2->drawStr((128 - w1) / 2, 26, "SMART");

    u8g2->setFont(u8g2_font_helvR10_tf); // 副标题细体，产生设计感
    int w2 = u8g2->getStrWidth("KITCHEN");
    u8g2->drawStr((128 - w2) / 2, 44, "KITCHEN");

    // 底部进度条占位
    u8g2->drawRBox(34, 52, 60, 4, 2);
}

void DisplayManager::drawStatusScreen(String title, String info)
{
    // 弹窗式UI设计：先画黑底抹除下方内容，再画白框
    u8g2->setDrawColor(0);
    u8g2->drawRBox(10, 12, 108, 40, 4);
    u8g2->setDrawColor(1);
    u8g2->drawRFrame(10, 12, 108, 40, 4);

    u8g2->setFont(u8g2_font_helvB08_tr);
    int w1 = u8g2->getStrWidth(title.c_str());
    u8g2->setCursor((128 - w1) / 2, 28);
    u8g2->print(title);

    u8g2->setFont(u8g2_font_5x8_tf);
    int w2 = u8g2->getStrWidth(info.c_str());
    u8g2->setCursor((128 - w2) / 2, 42);
    u8g2->print(info);
}

void DisplayManager::drawMainScreen(float fridgeTemp, float freezeTemp)
{
    extern WiFiConnector wifi;

    // ================= 1. 顶部状态栏 (Y: 0 ~ 15) =================

    // 左上角：运行模式 (药丸式反色徽章)
    String modeText = getModeText(currentMode);
    u8g2->setFont(u8g2_font_helvB08_tr);
    int modeWidth = u8g2->getStrWidth(modeText.c_str());

    u8g2->drawRBox(0, 0, modeWidth + 6, 14, 3);
    u8g2->setDrawColor(0); // 黑色绘制文字
    u8g2->drawStr(3, 11, modeText.c_str());
    u8g2->setDrawColor(1); // 恢复白色

    // 右上角：MQTT 图标 (X: 92)
    u8g2->drawXBMP(92, 0, 16, 16, icon_cloud_16);
    if (!mqttIsConnected())
    {
        u8g2->drawLine(92, 2, 106, 16); // 离线划线
    }

    // 右上角：WiFi 图标 (X: 110)
    u8g2->drawXBMP(110, 0, 16, 16, icon_wifi_16);
    if (!wifi.isConnected())
    {
        u8g2->drawLine(110, 2, 124, 16); // 离线划线
    }

    // 细分割线
    u8g2->drawHLine(0, 16, 128);

    // ================= 2. 核心数据微件区 (Y: 18 ~ 64) =================
    // 采用双卡片设计，适配 0.96寸 屏幕的最佳比例

    // --- 左侧微件卡片：冷藏区 (X: 0~61) ---
    u8g2->drawRFrame(0, 19, 62, 45, 3); // 画圆角边框

    // 标签 "Fridge"
    u8g2->setFont(u8g2_font_helvB08_tr);
    u8g2->drawStr(4, 30, "Fridge");

    // 状态图标 (放置在卡片右上角内侧)
    if (isFridgeCooling)
    {
        u8g2->drawXBMP(48, 21, 12, 12, icon_snow_12);
    }
    else if (isFridgeWarming)
    {
        u8g2->drawXBMP(48, 21, 12, 12, icon_heat_12);
    }

    // 温度数值
    char fridgeTempStr[8];
    dtostrf(fridgeTemp, 4, 1, fridgeTempStr);
    u8g2->setFont(u8g2_font_helvB14_tr); // 使用更紧凑锐利的 14 号粗体
    int ftW = u8g2->getStrWidth(fridgeTempStr);
    int tX = (62 - ftW) / 2 - 2; // 在 62px 宽度的卡片内居中
    u8g2->drawStr(tX, 55, fridgeTempStr);

    // 自绘度数小圆圈 (防止特殊符号乱码)
    u8g2->drawCircle(tX + ftW + 2, 45, 1);

    // --- 右侧微件卡片：冷冻区 (X: 66~127) ---
    u8g2->drawRFrame(66, 19, 62, 45, 3);

    // 标签 "Freeze"
    u8g2->setFont(u8g2_font_helvB08_tr);
    u8g2->drawStr(70, 30, "Freeze");

    // 状态图标
    if (isFreezeCooling)
    {
        u8g2->drawXBMP(114, 21, 12, 12, icon_snow_12);
    }
    else if (isFreezeWarming)
    {
        u8g2->drawXBMP(114, 21, 12, 12, icon_heat_12);
    }

    // 温度数值
    char freezeTempStr[8];
    dtostrf(freezeTemp, 4, 1, freezeTempStr);
    u8g2->setFont(u8g2_font_helvB14_tr);
    int ztW = u8g2->getStrWidth(freezeTempStr);
    int zX = 66 + (62 - ztW) / 2 - 2; // 右侧卡片居中计算
    u8g2->drawStr(zX, 55, freezeTempStr);

    // 自绘度数小圆圈
    u8g2->drawCircle(zX + ztW + 2, 45, 1);
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
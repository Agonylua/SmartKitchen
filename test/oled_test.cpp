/**
 * OLED诊断测试程序
 * 用于检测和测试OLED显示屏连接和配置
 */

#include <Arduino.h>
#include <Wire.h>
#include <U8g2lib.h>

// I2C引脚定义
#define I2C_SCL 41
#define I2C_SDA 42

// 尝试不同的I2C地址
#define OLED_ADDR_1 0x3C
#define OLED_ADDR_2 0x3D

// 创建U8g2对象（先用0x3C地址）
U8G2_SSD1306_128X64_NONAME_F_SW_I2C u8g2(U8G2_R0, /* clock=*/I2C_SCL, /* data=*/I2C_SDA, /* reset=*/U8X8_PIN_NONE);

void scanI2CDevices()
{
    Serial.println("\n=== 开始扫描I2C设备 ===");
    Wire.begin(I2C_SDA, I2C_SCL);

    byte count = 0;
    for (byte address = 1; address < 127; address++)
    {
        Wire.beginTransmission(address);
        byte error = Wire.endTransmission();

        if (error == 0)
        {
            Serial.print("找到I2C设备，地址: 0x");
            if (address < 16)
                Serial.print("0");
            Serial.println(address, HEX);
            count++;
        }
    }

    if (count == 0)
    {
        Serial.println("❌ 未找到任何I2C设备！");
        Serial.println("请检查：");
        Serial.println("  1. SDA和SCL引脚连接是否正确");
        Serial.println("  2. OLED电源是否接好");
        Serial.println("  3. 引脚定义是否匹配硬件");
    }
    else
    {
        Serial.printf("✓ 总共找到 %d 个I2C设备\n", count);
    }
    Serial.println("======================\n");
}

void testOLEDInit()
{
    Serial.println("=== 测试OLED初始化 ===");

    // 测试基本初始化
    Serial.println("正在初始化U8g2...");
    u8g2.begin();
    Serial.println("✓ U8g2初始化完成");

    // 测试清屏
    Serial.println("正在清空缓冲区...");
    u8g2.clearBuffer();
    Serial.println("✓ 清空缓冲区完成");

    // 测试发送缓冲区
    Serial.println("正在发送缓冲区到显示屏...");
    u8g2.sendBuffer();
    Serial.println("✓ 发送缓冲区完成");

    Serial.println("======================\n");
}

void testBasicDisplay()
{
    Serial.println("=== 测试基本显示 ===");

    // 测试1：全屏填充
    Serial.println("测试1: 全屏填充...");
    u8g2.clearBuffer();
    for (int y = 0; y < 64; y++)
    {
        u8g2.drawHLine(0, y, 128);
    }
    u8g2.sendBuffer();
    delay(2000);
    Serial.println("✓ 如果屏幕全亮，说明硬件工作正常");

    // 测试2：绘制方框
    Serial.println("测试2: 绘制方框...");
    u8g2.clearBuffer();
    u8g2.drawFrame(0, 0, 128, 64);
    u8g2.drawFrame(10, 10, 108, 44);
    u8g2.sendBuffer();
    delay(2000);
    Serial.println("✓ 应该看到两个矩形边框");

    // 测试3：显示文字
    Serial.println("测试3: 显示文字...");
    u8g2.clearBuffer();
    u8g2.setFont(u8g2_font_ncenB08_tr);
    u8g2.drawStr(10, 20, "OLED Test");
    u8g2.drawStr(10, 35, "I2C Working");
    u8g2.drawStr(10, 50, "ESP32-S3");
    u8g2.sendBuffer();
    delay(2000);
    Serial.println("✓ 应该看到三行文字");

    // 测试4：大字体
    Serial.println("测试4: 大字体显示...");
    u8g2.clearBuffer();
    u8g2.setFont(u8g2_font_10x20_tf);
    u8g2.drawStr(20, 25, "Smart");
    u8g2.drawStr(10, 50, "Kitchen");
    u8g2.sendBuffer();
    delay(2000);
    Serial.println("✓ 应该看到Smart Kitchen");

    Serial.println("======================\n");
}

void testPinConfiguration()
{
    Serial.println("=== 引脚配置信息 ===");
    Serial.printf("I2C SDA引脚: GPIO %d\n", I2C_SDA);
    Serial.printf("I2C SCL引脚: GPIO %d\n", I2C_SCL);
    Serial.println("请确认这些引脚与您的硬件连接匹配");
    Serial.println("======================\n");
}

void setup()
{
    Serial.begin(115200);
    delay(2000);

    Serial.println("\n\n");
    Serial.println("╔════════════════════════════════════╗");
    Serial.println("║   OLED SSD1306 诊断测试程序        ║");
    Serial.println("║   ESP32-S3 + U8g2库                ║");
    Serial.println("╚════════════════════════════════════╝");
    Serial.println("");

    // 步骤1：显示引脚配置
    testPinConfiguration();
    delay(1000);

    // 步骤2：扫描I2C设备
    scanI2CDevices();
    delay(1000);

    // 步骤3：初始化OLED
    testOLEDInit();
    delay(1000);

    // 步骤4：运行显示测试
    testBasicDisplay();

    Serial.println("=== 测试完成 ===");
    Serial.println("如果OLED仍然不亮，请检查：");
    Serial.println("1. I2C地址是否正确（0x3C或0x3D）");
    Serial.println("2. 引脚连接：SDA->GPIO42, SCL->GPIO41");
    Serial.println("3. 供电是否正常（3.3V或5V）");
    Serial.println("4. OLED屏幕型号是否为SSD1306 128x64");
    Serial.println("==================\n");
}

void loop()
{
    // 循环显示动画
    static int counter = 0;
    char buf[32];

    u8g2.clearBuffer();
    u8g2.setFont(u8g2_font_ncenB08_tr);
    u8g2.drawStr(0, 15, "OLED is Working!");

    snprintf(buf, sizeof(buf), "Counter: %d", counter++);
    u8g2.drawStr(0, 35, buf);

    snprintf(buf, sizeof(buf), "Time: %lu s", millis() / 1000);
    u8g2.drawStr(0, 50, buf);

    u8g2.sendBuffer();
    delay(500);
}

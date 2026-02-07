/**
 * 0.96寸OLED多驱动测试程序
 * 针对NFP315-61A等常见0.96寸OLED
 * 自动测试SSD1306和SH1106驱动
 */

#include <Arduino.h>
#include <Wire.h>
#include <U8g2lib.h>

// I2C引脚定义
#define I2C_SCL 41
#define I2C_SDA 42
#define I2C_ADDR 0x3C

// 创建多个驱动实例供测试
U8G2_SSD1306_128X64_NONAME_F_SW_I2C u8g2_ssd1306_sw(U8G2_R0, I2C_SCL, I2C_SDA, U8X8_PIN_NONE);
U8G2_SH1106_128X64_NONAME_F_SW_I2C u8g2_sh1106_sw(U8G2_R0, I2C_SCL, I2C_SDA, U8X8_PIN_NONE);

void scanI2C() {
    Serial.println("\n╔════════════════════════════════════╗");
    Serial.println("║    I2C设备扫描                     ║");
    Serial.println("╚════════════════════════════════════╝");
    
    Wire.begin(I2C_SDA, I2C_SCL);
    delay(100);
    
    int deviceCount = 0;
    for (byte addr = 1; addr < 127; addr++) {
        Wire.beginTransmission(addr);
        byte error = Wire.endTransmission();
        
        if (error == 0) {
            Serial.printf("✓ 找到I2C设备: 0x%02X\n", addr);
            deviceCount++;
        }
    }
    
    if (deviceCount == 0) {
        Serial.println("❌ 未找到任何I2C设备！");
        Serial.println("\n请检查：");
        Serial.printf("  1. SDA连接到GPIO%d\n", I2C_SDA);
        Serial.printf("  2. SCL连接到GPIO%d\n", I2C_SCL);
        Serial.println("  3. VCC和GND连接正确");
        Serial.println("  4. OLED模块电源正常");
    } else {
        Serial.printf("\n总共找到 %d 个I2C设备\n", deviceCount);
    }
    Serial.println("════════════════════════════════════\n");
}

bool testDriver(U8G2 &u8g2, const char* driverName) {
    Serial.println("\n╔════════════════════════════════════╗");
    Serial.printf("║  测试驱动: %-23s ║\n", driverName);
    Serial.println("╚════════════════════════════════════╝");
    
    Serial.println("步骤1: 初始化驱动...");
    u8g2.begin();
    delay(100);
    
    Serial.println("步骤2: 清空缓冲区...");
    u8g2.clearBuffer();
    delay(50);
    
    Serial.println("步骤3: 测试全屏填充...");
    for(int y = 0; y < 64; y++) {
        u8g2.drawHLine(0, y, 128);
    }
    u8g2.sendBuffer();
    Serial.println("  → 屏幕应该全亮2秒");
    delay(2000);
    
    Serial.println("步骤4: 测试文字显示...");
    u8g2.clearBuffer();
    u8g2.setFont(u8g2_font_ncenB08_tr);
    u8g2.drawStr(0, 15, "NFP315-61A");
    u8g2.drawStr(0, 30, "0.96\" OLED");
    u8g2.drawStr(0, 45, driverName);
    u8g2.drawStr(0, 60, "Test OK!");
    u8g2.sendBuffer();
    Serial.println("  → 应该看到文字显示5秒");
    delay(5000);
    
    Serial.println("步骤5: 测试大字体...");
    u8g2.clearBuffer();
    u8g2.setFont(u8g2_font_10x20_tf);
    u8g2.drawStr(10, 30, "Smart");
    u8g2.drawStr(5, 55, "Kitchen");
    u8g2.sendBuffer();
    Serial.println("  → 应该看到大号文字3秒");
    delay(3000);
    
    Serial.println("════════════════════════════════════");
    Serial.println("测试序列完成！");
    Serial.println("\n请观察屏幕是否有显示：");
    Serial.println("  ✓ 如果有显示 → 这个驱动正确！");
    Serial.println("  ✗ 如果无显示 → 尝试下一个驱动");
    Serial.println("════════════════════════════════════\n");
    
    return true;
}

void testAllDrivers() {
    Serial.println("\n\n");
    Serial.println("╔════════════════════════════════════╗");
    Serial.println("║  0.96寸OLED自动驱动测试程序       ║");
    Serial.println("║  NFP315-61A / 128x64               ║");
    Serial.println("╚════════════════════════════════════╝\n");
    
    Serial.println("配置信息：");
    Serial.printf("  OLED型号: NFP315-61A (0.96寸)\n");
    Serial.printf("  I2C地址: 0x%02X\n", I2C_ADDR);
    Serial.printf("  SDA引脚: GPIO%d\n", I2C_SDA);
    Serial.printf("  SCL引脚: GPIO%d\n", I2C_SCL);
    Serial.println();
    
    delay(2000);
    
    // 扫描I2C总线
    scanI2C();
    delay(2000);
    
    // 测试驱动1: SSD1306
    Serial.println("\n【驱动测试 1/2】");
    testDriver(u8g2_ssd1306_sw, "SSD1306 SW_I2C");
    delay(3000);
    
    // 测试驱动2: SH1106
    Serial.println("\n【驱动测试 2/2】");
    testDriver(u8g2_sh1106_sw, "SH1106 SW_I2C");
    delay(3000);
    
    Serial.println("\n\n");
    Serial.println("════════════════════════════════════");
    Serial.println("║        所有测试完成！              ║");
    Serial.println("════════════════════════════════════");
    Serial.println("\n根据屏幕显示情况：");
    Serial.println("  1. 如果SSD1306测试时有显示 → 使用SSD1306驱动");
    Serial.println("  2. 如果SH1106测试时有显示 → 使用SH1106驱动");
    Serial.println("  3. 如果都无显示 → 检查硬件连接\n");
}

void setup() {
    Serial.begin(115200);
    delay(2000);
    
    testAllDrivers();
}

void loop() {
    // 循环显示计数器（使用最后测试的驱动）
    static unsigned long counter = 0;
    char buf[32];
    
    u8g2_ssd1306_sw.clearBuffer();
    u8g2_ssd1306_sw.setFont(u8g2_font_ncenB08_tr);
    u8g2_ssd1306_sw.drawStr(0, 15, "Test Running");
    
    snprintf(buf, sizeof(buf), "Count: %lu", counter++);
    u8g2_ssd1306_sw.drawStr(0, 35, buf);
    
    snprintf(buf, sizeof(buf), "%lu s", millis() / 1000);
    u8g2_ssd1306_sw.drawStr(0, 55, buf);
    
    u8g2_ssd1306_sw.sendBuffer();
    delay(1000);
}

# OLED显示屏使用说明

## 硬件连接（4针OLED）

连接ESP32和SSD1306 OLED显示屏（I2C接口）：

| OLED引脚 | ESP32引脚 | 说明 |
|---------|----------|------|
| VCC     | 3.3V     | 电源 |
| GND     | GND      | 地   |
| SCL     | GPIO 22  | I2C时钟 |
| SDA     | GPIO 21  | I2C数据 |

## 显示内容

OLED屏幕实时显示以下信息：

1. **WiFi连接状态**：显示 "WiFi: OK" 或 "WiFi: X"
2. **MQTT连接状态**：显示 "MQTT: OK" 或 "MQTT: X"
3. **当前模式**：
   - STANDARD → STD
   - FAST_COOL → FAST
   - ENERGY_SAVING → ECO
   - HOLIDAY → AWAY
4. **传感器数据**：
   - Fridge: XX.XC（冷藏温度）
   - Freeze: XX.XC（冷冻温度）

## 库依赖

项目已在 `platformio.ini` 中配置了以下库：

```ini
adafruit/Adafruit SSD1306@^2.5.7
adafruit/Adafruit GFX Library@^1.11.5
```

## 代码结构

- **OLED.h**：OLED显示相关的头文件和配置
- **OLED.cpp**：OLED显示的实现代码
  - `oledInit()`：初始化OLED显示屏
  - `oledShowWelcome()`：显示欢迎界面
  - `oledDisplayStatus()`：更新系统状态显示
  - `oledUpdate()`：刷新显示内容

## 刷新频率

OLED显示内容每秒更新一次，在 `main.cpp` 的 `loop()` 函数中实现。

## I2C地址

默认I2C地址为 `0x3C`。如果你的OLED模块地址不同（有些是 `0x3D`），请修改 `OLED.h` 中的 `SCREEN_ADDRESS` 定义。

## 排查问题

如果OLED无显示：

1. 检查硬件连接是否正确
2. 确认OLED模块的I2C地址（使用I2C扫描程序）
3. 查看串口输出是否有 "SSD1306 初始化失败" 的错误信息
4. 确认ESP32的GPIO 21和22没有被其他设备占用

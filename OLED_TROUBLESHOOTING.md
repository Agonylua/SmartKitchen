# OLED显示故障诊断指南

## 问题现象
OLED屏幕无法点亮或无显示

## 已实施的诊断功能

代码已添加自动诊断功能，在启动时会：
1. 扫描I2C总线上的所有设备
2. 显示引脚配置信息
3. 测试OLED初始化过程
4. 尝试显示测试文字

## 查看诊断信息

### 方法1：使用PlatformIO Monitor
```bash
pio device monitor --port COM7 --baud 115200
```

### 方法2：重置设备
按下ESP32-S3开发板上的RESET按钮，查看串口输出的初始化信息。

## 诊断输出示例

### 正常输出应该看到：
```
=== 初始化OLED ===
I2C引脚 - SDA: GPIO42, SCL: GPIO41
I2C地址: 0x3D

=== 扫描I2C设备 ===
找到I2C设备: 0x3C
✓ 找到 1 个I2C设备
==================

正在初始化U8g2...
测试OLED显示...
✓ OLED初始化成功
==================
```

### 如果看到问题：

#### 问题1：未找到I2C设备
```
❌ 未找到I2C设备！检查接线：
  SDA: GPIO42
  SCL: GPIO41
```

**解决方案：**
1. 检查接线：
   - OLED VCC → ESP32 3.3V 或 5V（根据OLED模块要求）
   - OLED GND → ESP32 GND
   - OLED SDA → ESP32 GPIO42
   - OLED SCL → ESP32 GPIO41

2. 确认I2C引脚是否正确。如果您的硬件使用不同引脚，修改 `include/OLED.h` 中的定义：
   ```cpp
   #define I2C_SCL 41  // 改成您的SCL引脚
   #define I2C_SDA 42  // 改成您的SDA引脚
   ```

#### 问题2：找到I2C设备，但地址不匹配
```
找到I2C设备: 0x3C
```
如果扫描到的地址是 0x3C，但代码中配置的是 0x3D，请修改 `include/OLED.h`：
```cpp
#define SCREEN_ADDRESS 0x3C  // 改成扫描到的地址
```

#### 问题3：找到设备，但屏幕仍不亮

**可能原因：**
1. **OLED屏幕型号不匹配** - 代码目前配置为 SSD1306 128x64
   
   如果您的OLED是其它型号（如SH1106），需要修改 `src/OLED.cpp` 中的驱动类型：
   ```cpp
   // SSD1306 128x64
   U8G2_SSD1306_128X64_NONAME_F_SW_I2C u8g2(...);
   
   // 或 SH1106 128x64
   U8G2_SH1106_128X64_NONAME_F_SW_I2C u8g2(...);
   ```

2. **供电不足** - 检查OLED的VCC连接，尝试：
   - 如果接在3.3V，试试5V
   - 如果接在5V，试试3.3V

3. **OLED损坏** - 尝试在另一个设备上测试OLED

## 常见OLED尺寸和驱动

| 尺寸 | 驱动芯片 | I2C地址 | U8g2驱动类型 |
|------|----------|---------|--------------|
| 128x64 | SSD1306 | 0x3C/0x3D | U8G2_SSD1306_128X64_NONAME_F_SW_I2C |
| 128x64 | SH1106 | 0x3C/0x3D | U8G2_SH1106_128X64_NONAME_F_SW_I2C |
| 128x32 | SSD1306 | 0x3C/0x3D | U8G2_SSD1306_128X32_UNIVISION_F_SW_I2C |

## 代码改进

### 当前使用：软件I2C（SW_I2C）
- ✅ 优点：兼容性好，任何GPIO都可以用
- ⚠️ 缺点：速度稍慢

### 可选：硬件I2C（HW_I2C）
如果软件I2C工作正常，想提高性能，可以改用硬件I2C：

修改 `src/OLED.cpp`：
```cpp
// 从软件I2C
U8G2_SSD1306_128X64_NONAME_F_SW_I2C u8g2(U8G2_R0, 
    /* clock=*/ I2C_SCL, 
    /* data=*/ I2C_SDA, 
    /* reset=*/ U8X8_PIN_NONE);

// 改为硬件I2C
U8G2_SSD1306_128X64_NONAME_F_HW_I2C u8g2(U8G2_R0, 
    /* reset=*/ U8X8_PIN_NONE, 
    /* clock=*/ I2C_SCL, 
    /* data=*/ I2C_SDA);
```

## 独立测试程序

如果主程序太复杂，可以使用简单的测试程序：

在 `test/oled_test.cpp` 中已经创建了一个独立的测试程序。

### 如何使用测试程序：

1. 临时重命名文件：
   ```
   mv src/main.cpp src/main.cpp.bak
   mv test/oled_test.cpp src/main.cpp
   ```

2. 编译上传：
   ```
   pio run --target upload
   ```

3. 查看串口输出并观察OLED

4. 测试完成后恢复：
   ```
   mv src/main.cpp test/oled_test.cpp
   mv src/main.cpp.bak src/main.cpp
   ```

## 下一步

1. **首先**：查看串口输出的诊断信息
2. **然后**：根据输出的错误信息，参考本文档进行排查
3. **如果需要**：提供串口输出的完整日志以获得进一步帮助

## 常用命令

```bash
# 编译
pio run

# 上传
pio run --target upload

# 监视串口（Windows）
pio device monitor --port COM7 --baud 115200

# 一键上传并监视
pio run --target upload && pio device monitor
```

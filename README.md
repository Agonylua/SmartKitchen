# 🍳 Smart Kitchen (智能厨房) - ESP32 硬件端

![ESP32](https://img.shields.io/badge/Hardware-ESP32--S3-red?style=flat-square&logo=espressif)
![C++](https://img.shields.io/badge/Language-C++-blue?style=flat-square&logo=c%2B%2B)
![PlatformIO](https://img.shields.io/badge/IDE-PlatformIO-orange?style=flat-square&logo=platformio)

“智能厨房”项目的边缘设备端层。基于强大的 ESP32-S3 微控制器，负责实际环境数据的采集（温度、湿度、重量感知）以及接收云端指令对物理家电（风扇、加热器、继电器等）进行直接控制。

## ✨ 核心特性

- 🔌 **全自动配网**: 支持通过 BLE (蓝牙低功耗) 结合 App 端实现一键配网，或者 SmartConfig 自动获取 Wi-Fi 凭证。
- 📡 **毫秒级低延迟通信**: 通过 `PubSubClient` 集成 MQTT，与 Server 维持长连接，保障指令下发与状态上报实时互通。
- 🌡️ **多维传感器采集**: 集成 DHT11 (温湿度)、称重传感器及其它模拟/数字外设，以 JSON 格式定时打包上报数据。
- 📺 **本地状态交互**: 结合 OLED 屏幕展示当前网络状态、设备工作模式及实时温湿度，提供本地无头调试手段。

## 🛠 硬件与技术栈

- **主控芯片**: ESP32-S3
- **开发环境**: PlatformIO (集成于 VS Code) / Arduino 核心框架
- **依赖库**: 
  - `PubSubClient` (MQTT)
  - `WiFiManager` (配网支持)
  - `ArduinoJson` (JSON序列化)
  - `U8g2` 或 `Adafruit_SSD1306` (OLED 驱动)
  - `DHT sensor library`

## 🚀 快速启动

1. 在 VS Code 中安装 **PlatformIO IDE** 插件。
2. 将本工程使用 PlatformIO 打开。
3. 检查 `platformio.ini` 配置，确保开发板型号 (`board = esp32-s3-devkitc-1` 或对应型号)、波特率 (`monitor_speed = 115200`) 匹配你的实际硬件。
4. 在 `include/mqttConfig.h` 等配置文件中预配 MQTT Broker 地址（或由后续配网步骤动态下发）。
5. 连接 ESP32-S3，点击 PlatformIO 的 **Upload and Monitor** (➡️ 箭头图标) 进行编译烧录与串口监视。

## 📂 代码结构

```text
├── include/           # 头文件 (全局定义, 硬件引脚分配)
│   ├── mqttConfig.h   # MQTT 主题与服务器配置
│   ├── control.h      # 继电器/外设控制声明
│   └── ...
├── src/               # 源代码
│   ├── main.cpp       # 主入口 (setup & loop)
│   ├── mqttConfig.cpp # MQTT 连线、重连与回调处理
│   ├── wifiManager.cpp# 网络配置逻辑
│   ├── dht11.cpp      # 温湿度采集逻辑
│   └── displayManager.cpp # OLED 界面刷新
├── platformio.ini     # 硬件编译规则及依赖声明
└── test/              # 硬件模块独立测试脚本 (OLED 测试等)
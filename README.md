# 🔌 ESP32 智能家居设备端
## 项目简介
基于ESP32的智能家居设备固件，支持多种传感器和执行器，通过MQTT协议与手机APP和云端服务器通信。

## 🚀 核心功能
**设备配网** - SmartConfig一键配网

**传感器采集** - 温湿度、光照、运动检测

**设备控制** - 继电器控制、PWM调光

**状态上报** - 实时状态同步

**OTA升级** - 无线固件更新

**能耗管理** - 低功耗模式

## 🛠 技术栈
**微控制器**: ESP32-WROOM-32

**开发框架**: Arduino + ESP-IDF

**通信协议**: WiFi + MQTT

**传感器**: DHT11、光敏电阻、继电器

**协议**: MQTT 3.1.1

## 📁 项目结构
text
firmware/
├── src/
│   ├── main.cpp              # 主程序
│   ├── wifi_manager/         # WiFi管理
│   ├── mqtt_client/          # MQTT客户端
│   ├── device_drivers/       # 设备驱动
│   ├── sensors/              # 传感器模块
│   └── ota/                  # OTA升级
├── include/                  # 头文件
├── lib/                      # 第三方库
└── data/                     # 网页文件
## 🔧 硬件要求
**必需组件**
ESP32开发板

DHT11温湿度传感器

光敏电阻模块

5V继电器模块

LED灯带（可选）

**接线示意图**
```text
ESP32     传感器/执行器
GPIO2  --> DHT11数据线
GPIO4  --> 光敏电阻
GPIO5  --> 继电器控制
GPIO12 --> LED PWM控制
3.3V   --> 传感器VCC
GND    --> 传感器GND
```
## ⚡ 快速开始
**安装环境**

# 安装Arduino IDE或PlatformIO
# 配置ESP32开发板支持
**配置参数**
在 config.h 中修改配置：

```cpp
#define WIFI_SSID "your-wifi"
#define WIFI_PASSWORD "your-password"
#define MQTT_SERVER "your-mqtt-server"
#define MQTT_PORT 1883
```
**编译烧录**

```bash
# 使用PlatformIO
pio run -t upload
```
# 使用Arduino IDE
# 选择ESP32开发板，点击上传
**配网使用**

手机连接2.4GHz WiFi

打开APP进入设备发现

输入WiFi密码进行配网

## 📡 通信协议
**MQTT主题设计**
// 设备控制
home/device/{device_id}/control
// 设备状态
home/device/{device_id}/status  
// 系统消息
home/system/online
**消息格式**
json
{
  "device_id": "sensor_001",
  "type": "temperature",
  "value": 25.6,
  "timestamp": 1633024800
}
## 🔄 OTA升级
支持HTTP OTA升级：

```bash
curl -F "firmware=@firmware.bin" http://device-ip/update
```
## 📊 功耗优化
深度睡眠模式

定时唤醒采集

动态数据传输频率

低功耗WiFi连接

## 🛠 调试工具
串口调试（115200波特率）

MQTT消息监控

网页配置界面

日志系统

## 📞 技术支持
文档: https://agonylua.asia/gitea/Liuwei/Smart-Home.git

邮箱: 3143797485@qq.com
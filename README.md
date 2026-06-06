# Smart Kitchen - 智能厨房家电物联系统

> **基于 Android 的智能厨房家电管理 App 设计** 
> 这是一个端到端的全栈物联网 (IoT) 毕业设计项目，旨在为现代厨房提供高实时性、高可用性的设备接入、远程监控与智能自动化控制平台。

---

## 📖 项目简介

本项目采用 **Android + Spring Boot + ESP32-S3** 的三端协同架构，打通了“设备配网、状态上报、指令下发、场景联动、数据可视化”的完整闭环。移动端应用深度借鉴了“米家”等成熟商业 App 的卡片式清爽 UI 风格，并严格遵循 Material Design 3 设计规范，为用户提供极其流畅的智能家居管理体验。

## ✨ 核心功能特性

- 📱 **移动控制中枢 (Android)**
  - 遵循标准 **MVVM 架构**设计，基于 Navigation 组件实现单 Activity 多 Fragment 的高效路由。
  - 深度结合 Room 离线缓存与“乐观 UI”更新策略，保障弱网环境下的设备开关、模式切换（如微波炉、洗碗机、冰箱等设备）依旧丝滑跟手。
  - 集成 `MPAndroidChart` 打造数据监控大屏，可视化渲染设备耗电趋势与运行日志。

- ☁️ **业务中台与智能引擎 (Spring Boot)**
  - 基于 Spring Security + JWT 体系构建无状态的安全用户中心及家庭组权限管理。
  - 强大的自动化场景联动引擎，支持自定义工作流（例如：当厨房温度 > 30℃ 且湿度超标时，自动下发开启排风扇指令）。
  - 异步处理高频 MQTT 状态上报，彻底解耦业务逻辑。

- 🔌 **嵌入式硬件终端 (ESP32-S3)**
  - 利用传感器（DHT11温湿度、继电器模块）实现精准的厨房环境与物料数据采集。
  - 支持 **BLE (低功耗蓝牙) + SmartConfig 混合配网**，极大提升终端首次绑定成功率。
  - 基于 MQTT 协议与云端建立毫秒级双向通信链路。

## 🛠 核心技术栈

### 1. Android 客户端
* **语言**: Java
* **架构**: MVVM (ViewModel + LiveData + Repository)
* **网络与通信**: Retrofit2 + OkHttp, MQTT Client
* **本地存储**: Room, SharedPreferences
* **UI 组件**: Material Design 3, ConstraintLayout, SmartRefreshLayout, MPAndroidChart

### 2. Spring Boot 后端
* **语言框架**: Java, Spring Boot 
* **持久层**: MyBatis-Plus / JPA
* **认证授权**: Spring Security + JWT
* **消息中间件**: EMQX / Mosquitto Broker (MQTT 服务)

### 3. ESP32 硬件端
* **核心控制板**: ESP32-S3
* **开发环境**: PlatformIO / Arduino IDE (C++)
* **通信协议**: Wi-Fi, MQTT, BLE

## 🔄 系统数据流向图 (跨端协同)

1. **下发控制指令 (App -> Hardware)**:
   `用户点击 UI` -> `ViewModel/Repository` -> `Retrofit 请求后端 API` -> `后端校验权限并持久化日志` -> `Spring Boot 发送指令至 MQTT Topic` -> `ESP32 订阅到消息并驱动继电器`。

2. **设备状态上报 (Hardware -> App)**:
   `ESP32 传感器采集` -> `组装 JSON 并 Publish 到特定 Topic` -> `后端/App 订阅接收` -> `App 异步线程池解析并更新 Room 缓存` -> `LiveData 观测到数据库变更，自动驱动 UI 刷新`。

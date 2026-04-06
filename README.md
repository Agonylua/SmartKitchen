# 🍳 Smart Kitchen (智能厨房) - Android App

![Android](https://img.shields.io/badge/Android-Java-3DDC84?style=flat-square&logo=android)
![Architecture](https://img.shields.io/badge/Architecture-MVVM-blue?style=flat-square)
![License](https://img.shields.io/badge/License-MIT-green.svg)

“智能厨房”项目的移动端应用，负责与用户进行交互、实时展示家电设备状态并下发控制指令。采用现代化的
Android MVVM 架构，结合 MQTT 协议实现与 ESP32 设备的毫秒级双向通信。

## ✨ 核心特性

- 👤 **用户中心**: 基于 JWT 的无状态认证登录/注册、个人信息管理与家庭组动态划分。
- 🔗 **设备接入**: 支持蓝牙 (BLE) 及 SmartConfig 快速配网，将 ESP32 厨房设备安全接入网络。
- 📱 **设备管理**:
    - 实时设备列表：利用 **Room 数据库** 提供离线缓存支持，配合乐观 UI 提升控制流畅度。
    - 全屏详情与控制：支持远程开关、模式调节、定时任务设定（烤箱、微波炉、洗碗机等设备）。
- 🤖 **智能自动化**: 用户自定义场景联动工作流（如：“环境温度 > 30℃ -> 自动开启排风扇”）。
- 📊 **监控大屏**: 基于 `MPAndroidChart` 渲染的设备用电量趋势图与运行状态日志分析。

## 🛠 技术栈与架构规范

- **编程语言**: Java
- **架构模式**: 严格 MVVM (ViewModel + LiveData + Repository)。Repository 负责统一调度后端 API 与
  Room 本地数据。
- **网络通信**: Retrofit2 + OkHttp (拦截器自动注入 JWT Token)，统一包装 `Result<T>`。
- **本地存储**: Room 数据库 (包含 `TypeConverter` 用于复杂结构转换)，SharedPreferences (配置管理)。
- **实时通信**: Paho MQTT Client，封装为 `MqttLiveBus` 实现异步消息解耦。
- **UI 组件**: Material Design 3, ConstraintLayout, RecyclerView, SmartRefreshLayout, Glide。

## 🚀 快速启动

1. 克隆本项目并在 Android Studio (推荐 Iguana 或更高版本) 中打开。
2. 确保已配置 Java 17 环境变量。
3. 在 `app/src/main/java/com/agonylua/smarthome/network/RetrofitClient.java` 中修改 `BASE_URL` 为你的
   Spring Boot 后端 IP。
4. 修改 MQTT Broker 的连接配置（IP/端口）。
5. 编译并运行至真机或模拟器。

## 📦 目录结构

```text
app/src/main/java/com/agonylua/smarthome/
├── DTO/           # 数据传输对象
├── Model/         # 数据模型定义
├── Service/       # 后台服务 (监控、离线任务等)
├── adapter/       # RecyclerView 等适配器
├── database/      # Room 数据库配置、DAO 及 Entities
├── fragment/      # UI 界面 (Fragment)
├── network/       # Retrofit 网络接口与拦截器
├── repository/    # MVVM 仓库层 (数据统一调度)
├── utils/         # 工具类 (MqttManager, JsonUtils, TokenManager等)
├── viewModel/     # 视图模型 (LiveData)
└── view/          # 核心 Activity (如 MainActivity)
```

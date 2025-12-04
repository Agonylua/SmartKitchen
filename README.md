# 📱 Android 智能家居APP
## 项目简介
基于Java开发的Android智能家居控制应用，提供完整的设备管理、情景模式、语音控制等功能，支持多用户权限管理和远程访问。

## 🚀 核心功能
设备管理 - 设备发现、配网、控制、状态同步

情景模式 - 定时触发、设备联动、语音触发

语音控制 - 语音识别、指令解析、语音反馈

多用户系统 - 家庭成员管理、权限控制

远程访问 - 云端同步、异地控制

数据统计 - 使用记录、能耗分析

## 🛠 技术栈
语言: Java

架构: MVVM + 模块化

数据库: Room + SQLite

网络: Retrofit + OkHttp + MQTT

UI: Material Design + Jetpack组件

语音: Android Speech API

## 📁 项目结构
text
app/
├── activity/          # 活动页面
├── fragment/          # 碎片页面
├── adapter/           # 列表适配器
├── viewmodel/         # 视图模型
└── service/           # 后台服务

lib-core/              # 核心业务
├── device/            # 设备管理
├── scene/             # 情景模式
├── user/              # 用户管理
└── voice/             # 语音控制

lib-network/           # 网络通信
├── api/               # REST接口
├── mqtt/              # MQTT客户端
└── websocket/         # WebSocket

lib-database/          # 数据持久化
├── dao/               # 数据访问
├── entity/            # 实体类
└── migration/         # 数据库迁移
## 🔧 快速开始
**克隆项目**

```bash
git clone https://github.com/your-repo/smart-home-android.git
```
**配置环境**

Android Studio Arctic Fox+
JDK 11
Android SDK 34

**配置参数**
在 local.properties 中配置：

```properties
mqtt.server.url=tcp://your-mqtt-server:1883
api.base.url=https://your-api-server.com
```
编译运行

```bash
./gradlew assembleDebug
```
## 📋 依赖说明
主要依赖库：

```gradle
androidx.appcompat:1.6.1
androidx.room:2.6.0
org.eclipse.paho:mqtt-client:1.2.5
com.squareup.retrofit2:retrofit:2.9.0
```

## 🎯 部署要求
最低版本: Android 8.0 (API 26)

目标版本: Android 14 (API 34)

权限要求: 网络访问、麦克风、存储

## 📞 联系方式
开发者: liuwei

邮箱: 3143797485@qq.com

项目地址: https://agonylua.asia/gitea/Liuwei/Smart-Home.git
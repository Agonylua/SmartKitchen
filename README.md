# 🍳 Smart Kitchen (智能厨房) - Server 端

![Java](https://img.shields.io/badge/Java-17-orange?style=flat-square&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-2.x-green?style=flat-square&logo=spring)
![MQTT](https://img.shields.io/badge/Protocol-MQTT-blue?style=flat-square)

“智能厨房”项目的后端中控服务。它负责处理 Android 客户端的 RESTful API 请求，执行 JWT 身份鉴权，并作为中枢节点通过 MQTT
Broker (如 EMQX/Mosquitto) 与 ESP32 硬件进行异步数据桥接。

## ✨ 核心特性

- 🔐 **安全认证**: 基于 Spring Security 与 JWT 机制的无状态接口鉴权。
- 📡 **设备通信**: 集成 MQTT Client，实现设备状态的异步监听、心跳检测机制及设备上下线管理，避免阻塞主线程。
- 👥 **用户与家庭组**: 完善的用户注册、权限划分与家庭组（Home）隔离管理，防止设备越权控制。
- ⚡ **统一响应处理**: 全局异常拦截 (`GlobalExceptionHandler`) 与标准 RESTful `ApiResponse<T>` 封装。

## 🛠 技术栈设计

- **框架**: Spring Boot + Spring Security
- **数据库 ORM**: Spring Data JPA / MyBatis-Plus (视具体实现而定) + MySQL
- **消息中间件**: MQTT 协议栈 (负责设备到云、云到 App 的解耦通讯)
- **工具链**: Lombok, JWT 工具, Maven

## 🚀 快速启动

### 前置要求

- JDK 21
- MySQL 8.x
- MQTT Broker (推荐 Docker 部署 EMQX)

### 配置与运行

1. 修改 `src/main/resources/application.yml` 中的配置项：
    - 数据库连接 (`spring.datasource.url/username/password`)
    - MQTT Broker 地址 (`mqtt.host/username/password`)
    - JWT 密钥 (`jwt.secret`)
2. 执行 Maven 构建并启动：
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

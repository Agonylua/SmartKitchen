# ☁️ 智能家居后端服务器
## 项目简介
基于Spring Boot的智能家居云服务平台，提供设备管理、用户认证、数据存储、消息转发等核心服务。

## 🚀 核心功能
用户管理 - 注册登录、权限控制、家庭管理

设备管理 - 设备注册、状态同步、控制转发

情景引擎 - 规则管理、条件触发、动作执行

消息服务 - MQTT代理、WebSocket推送

数据统计 - 使用分析、能耗统计、报表生成

系统监控 - 服务健康、性能指标、日志管理

## 🛠 技术栈
**后端框架**: Spring Boot 3.x + Spring Security

**数据库**: MySQL 8.0 + Redis 7.0

**消息队列**: EMQX + RabbitMQ

**缓存**: Redis

**API文档**: Swagger/OpenAPI 3.0

**部署**: Docker + Nginx

## 📁 项目结构
```text
smart-home-server/
├── smart-home-gateway/         # API网关
├── smart-home-auth/            # 认证服务
├── smart-home-device/          # 设备服务
├── smart-home-scene/           # 情景服务
├── smart-home-message/         # 消息服务
├── smart-home-monitor/         # 监控服务
└── smart-home-common/          # 公共模块
```
## 🗄 数据库设计
核心表结构
```sql
-- **用户表**
users(id, username, password, email, role, family_id)
-- **家庭表**  
families(id, name, invitation_code, created_by)
-- **设备表**
devices(id, device_id, name, type, room, family_id, online_status)
-- **情景表**
scenes(id, name, trigger_type, trigger_config, enabled)
-- **设备权限表**
device_permissions(id, user_id, device_id, permission_level)
```
## 🔧 部署环境
JDK 17+

MySQL 8.0+

Redis 7.0+

EMQX 5.0+

Docker 20.0+

## ⚡ 快速部署
1. **克隆项目**
bash
git clone https://github.com/your-repo/smart-home-server.git
cd smart-home-server
2. **环境配置**
创建 application.yml：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/smart_home
    username: your-username
    password: your-password
  redis:
    host: localhost
    port: 6379

mqtt:
  broker-url: tcp://localhost:1883
  username: admin
  password: public
```
3. **数据库初始化**
```mysql
mysql -u root -p < database/init.sql
```
4. **Docker部署**
```bash
启动所有服务
docker-compose up -d

查看服务状态
docker-compose ps
```
5. **手动部署**
```bash
编译项目
mvn clean package -DskipTests

运行服务
java -jar smart-home-gateway/target/gateway.jar
java -jar smart-home-auth/target/auth.jar
```
📡 API接口
**认证接口**
```http
POST /api/auth/login
POST /api/auth/register
POST /api/auth/refresh
```
**设备接口**
```http
GET    /api/devices
POST   /api/devices/{id}/control
GET    /api/devices/{id}/status
POST   /api/devices/discover
```
**情景接口**
```http
GET    /api/scenes
POST   /api/scenes
PUT    /api/scenes/{id}
POST   /api/scenes/{id}/trigger
```
## 🔒 安全配置
JWT Token认证

Spring Security权限控制

API访问限流

## 📊 监控指标
服务健康状态

API响应时间

数据库连接池

MQTT消息吞吐量

系统资源使用率

## 🐛 故障排查
日志查看
bash
查看服务日志
docker-compose logs -f gateway
tail -f logs/application.log
## 🔄 备份恢复
**数据备份**
```mysql
数据库备份
mysqldump -u root -p smart_home > backup.sql
数据库恢复
mysql -u root -p smart_home < backup.sql
#配置文件备份
#tar -czf config-backup.tar.gz config/
```
## 📞 运维支持

🌟 性能优化
数据库索引优化
Redis缓存策略
连接池配置调优
JVM参数优化
静态资源CDN加速
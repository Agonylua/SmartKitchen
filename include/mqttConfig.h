#pragma once

#include "common.h"

#define MQTT_BROKER "10.1.1.110"
#define MQTT_PORT 1883
#define MQTT_CLIENT_ID "SK-156400423935CCBA9720B608"
#define MQTT_USERNAME "smartKitchen"
#define MQTT_PASSWORD "wei.liu-liu"

#define TOPIC_SUB "smartKitchen/devices/SK-156400423935CCBA9720B608/" // MQTT 订阅主题

// MQTT 发布主题数组
const char *const TOPIC_PUB[] = {
    "smartKitchen/service/SK-156400423935CCBA9720B608/",
    "smartKitchen/application/SK-156400423935CCBA9720B608/"};
#define TOPIC_PUB_COUNT 2 // 发布主题数量

#define TOPIC_STATUS "smartKitchen/device/SK-156400423935CCBA9720B608/status" // 状态主题
// 定义消息内容
#define MSG_LWT "offline"   // 遗嘱消息（掉线时发送）
#define MSG_ONLINE "online" // 上线消息（连接成功时发送）

void mqttInit();
void mqttCallback(char *topic, byte *payload, unsigned int length);
void mqttReconnect();
void publishBindVerify();
void publishSensorData();
void publishBindHomeId();
void mqttLoop();
String deviceModeToString(DeviceMode mode);
bool mqttIsConnected();
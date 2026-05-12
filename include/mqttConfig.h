#pragma once

#include "common.h"

#define MQTT_BROKER "agonylua.asia"
#define MQTT_PORT 1883
#define MQTT_CLIENT_ID "SK-R001"
#define MQTT_USERNAME "smartKitchen"
#define MQTT_PASSWORD "wei.liu-liu"

#define TOPIC_SUB "smartKitchen/devices/SK-R001/" // MQTT 订阅主题

// MQTT 发布主题数组
const char *const TOPIC_PUB[] = {
    "smartKitchen/service/SK-R001/",
    "smartKitchen/application/SK-R001/"};
#define TOPIC_PUB_COUNT 2 // 发布主题数量

#define TOPIC_STATUS "smartKitchen/status/SK-R001" // 状态主题
// 定义消息内容
#define MSG_LWT "offline"   // 遗嘱消息（掉线时发送）
#define MSG_ONLINE "online" // 上线消息（连接成功时发送）

void mqttInit();
void mqttCallback(char *topic, byte *payload, unsigned int length);
void mqttReconnect();
void publishUnBind();
void publishSensorData();
void publishBindStatus(boolean bindStatus);
void mqttLoop();
bool mqttIsConnected();
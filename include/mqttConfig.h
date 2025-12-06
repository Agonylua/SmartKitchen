#pragma once

#define MQTT_BROKER "10.1.1.1"
#define MQTT_PORT 1883           
#define MQTT_CLIENT_ID "ESP32_mqtt"   
#define MQTT_USERNAME "smartKitchen"
#define MQTT_PASSWORD "wei.liu-liu"

#define TOPIC_PUB "C/B/DEV000"   // ESP32 发送的数据
#define TOPIC_SUB (const char *[]){"A/C/DEV000", "B/C/DEV000"} // ESP32 接收的命令

void mqttInit();
void mqttCallback(char *topic, byte *payload, unsigned int length);
void publishMessage();
void mqttLoop();
#pragma once

#define MQTT_BROKER "10.1.1.1"
#define MQTT_PORT 1883
#define MQTT_CLIENT_ID "Refrigerator"
#define MQTT_USERNAME "smartKitchen"
#define MQTT_PASSWORD "wei.liu-liu"

#define TOPIC_SUB "smartKitchen/devices/DEV000"                                                // MQTT 发布主题
#define TOPIC_PUB (const char *[]){"smartKitchen/service/data", "smartKitchen/application/data"} // MQTT 订阅主题

void mqttInit();
void mqttCallback(char *topic, byte *payload, unsigned int length);
void mqttReconnect();
void publishMessage();
void mqttLoop();
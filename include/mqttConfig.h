#pragma once

#define MQTT_BROKER "10.1.1.1"
#define MQTT_PORT 1883
#define MQTT_CLIENT_ID "Refrigerator"
#define MQTT_USERNAME "smartKitchen"
#define MQTT_PASSWORD "wei.liu-liu"

#define TOPIC_SUB "smartKitchen/devices/156400423935"                                                // MQTT 发布主题
#define TOPIC_PUB (const char *[]){"smartKitchen/service/156400423935", "smartKitchen/application/156400423935"} // MQTT 订阅主题

void mqttInit();
void mqttCallback(char *topic, byte *payload, unsigned int length);
void mqttReconnect();
void publishMessage();
void mqttLoop();
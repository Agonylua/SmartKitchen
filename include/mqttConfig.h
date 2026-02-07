#pragma once


#define MQTT_BROKER "10.1.1.110"
#define MQTT_PORT 1883
#define MQTT_CLIENT_ID "SK-156400423935CCBA9720B608"
#define MQTT_USERNAME "smartKitchen"
#define MQTT_PASSWORD "wei.liu-liu"

#define TOPIC_SUB "smartKitchen/devices/SK-156400423935CCBA9720B608/"                                                  // MQTT 发布主题
#define TOPIC_PUB (const char *[]){"smartKitchen/service/SK-156400423935CCBA9720B608/", "smartKitchen/application/SK-156400423935CCBA9720B608/"} // MQTT 订阅主题

void mqttInit();
void mqttCallback(char *topic, byte *payload, unsigned int length);
void mqttReconnect();
void publishMessage();
void mqttLoop();
bool mqttIsConnected();
#include <Arduino.h>
#include <WiFi.h>
#include <PubSubClient.h>
#include "mqttConfig.h"
#include "dht11.h"    

WiFiClient espClient;
PubSubClient mqtt(espClient);

void mqttInit();
void mqttLoop();
void mqttCallback(char *topic, byte *payload, unsigned int length);
void mqttReconnect();
void publishMessage();

void mqttInit()
{
    mqtt.setServer(MQTT_BROKER, MQTT_PORT);
    mqtt.setCallback(mqttCallback);
    mqtt.setKeepAlive(30); // 30秒足够
}

void mqttLoop()
{
    if (WiFi.status() != WL_CONNECTED)
        return;

    if (!mqtt.connected())
    {
        mqttReconnect();
    }
    mqtt.loop();
}

void mqttCallback(char *topic, byte *payload, unsigned int len)
{
    char msg[len + 1];
    memcpy(msg, payload, len);
    msg[len] = '\0';

    Serial.printf("[MQTT ←] %s : %s\n", topic, msg);

    // 业务逻辑
}

void mqttReconnect()
{
    String clientId = MQTT_CLIENT_ID;
    Serial.printf("MQTT 连接中... ID: %s\n", clientId.c_str());

    bool ok;
    if (MQTT_USERNAME && MQTT_PASSWORD)
    {
        ok = mqtt.connect(clientId.c_str(), MQTT_USERNAME, MQTT_PASSWORD, nullptr, 0, false, nullptr, true);
    }
    else
    {
        ok = mqtt.connect(clientId.c_str(), nullptr, nullptr, nullptr, 0, false, nullptr, true);
    }

    if (ok)
    {
        Serial.println("MQTT 已连接");
        mqtt.publish("ESP32 online", clientId.c_str(), true);

        // 订阅主题
        mqtt.subscribe(TOPIC_SUB[0], 1);
        mqtt.subscribe(TOPIC_SUB[1], 1);
        Serial.println("已订阅"+String(TOPIC_SUB[0]));
    }
    else
    {
        Serial.printf("MQTT 连接失败 rc=%d\n", mqtt.state());
    }
}

void publishMessage()
{
    if (!mqtt.connected())
        return;

    float t = readTemperature();
    float h = readHumidity();
    if (isnan(t) || isnan(h))
        return;

    char payload[100];
    snprintf(payload, sizeof(payload),
             "{\"temp\":%.2f,\"hum\":%.2f,\"up\":%lu}",
             t, h, millis() / 1000);

    if (mqtt.publish(TOPIC_PUB, payload))
    {
        Serial.printf("发布 → %s : %s\n", TOPIC_PUB, payload);
    }
}
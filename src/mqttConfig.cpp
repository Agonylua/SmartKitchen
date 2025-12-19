#include <Arduino.h>
#include <WiFi.h>
#include <PubSubClient.h>
#include <ArduinoJson.h>
#include <mqttConfig.h>
#include <dht11.h>
#include <control.h>
#include <main.h>
#include "timeUtils.h"

WiFiClient espClient;
PubSubClient mqtt(espClient);

void mqttInit()
{
    mqtt.setServer(MQTT_BROKER, MQTT_PORT);
    mqtt.setCallback(mqttCallback);
    mqtt.setSocketTimeout(10); 
    mqtt.setKeepAlive(20); 
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

    // JSON 解析
    JsonDocument doc;
    DeserializationError error = deserializeJson(doc, msg);

    if (error)
    {
        Serial.print(F("deserializeJson() failed: "));
        Serial.println(error.f_str());
        return;
    }

    // MQTT消息解析
    if (doc["cmd"].is<String>())
    {
        return;
    }
    String cmd = doc["cmd"];
    int region = doc["region"];
    int val = doc["value"];
    // 根据控制指令执行相应操作
    if (cmd == "setTempThreshold")
    {
        tempThreshold_1 = val;
        tempThreshold_2 = val;
    }
    else if (cmd == "superCool")
    {
        mode = "SuperCool";
        /* code */
    }
    else if (cmd == "holiday")
    {
        mode = "Holiday";
        /* code */
    }
    else if (cmd == "eco")
    {
        mode = "Eco";
        /* code */
    }
    else
    {
        Serial.println("未知的控制指令: " + String(cmd));
    }
    Serial.println("当前模式: " + mode);
    Serial.printf("温度阈值1: %.2f, 温度阈值2: %.2f\n", tempThreshold_1, tempThreshold_2);
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
        mqtt.subscribe(TOPIC_SUB, 1);
        mqtt.subscribe(TOPIC_SUB, 1);
        Serial.println("已订阅" + String(TOPIC_SUB));
    }
    else
    {
        Serial.printf("MQTT 连接失败 rc=%d\n", mqtt.state());
    }
}

void publishMessage()
{
    if (!mqtt.connected()){return;}
    float t = readTemperature();
    float h = readHumidity();
    if (isnan(t) || isnan(h))
        return;

    // 使用 ArduinoJson 构建 JSON
    JsonDocument doc;
    doc["temp"] = t;
    doc["hum"] = h;
    doc["up"] = timeStr;

    char payload[256];
    serializeJson(doc, payload);

    if (mqtt.publish(TOPIC_PUB[0], payload))
    {
        Serial.printf("发布 → %s : %s\n", TOPIC_PUB[0], payload);
    }
}
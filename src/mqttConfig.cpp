#include <Arduino.h>
#include <WiFi.h>
#include <PubSubClient.h>
#include <ArduinoJson.h>
#include <mqttConfig.h>
#include <dht11.h>
#include <control.h>
#include "timeUtils.h"
#include <Preferences.h>
#include "wifiManager.h"
#include "common.h"

WiFiClient espClient;
PubSubClient mqtt(espClient);

int count = 0;

// 初始化MQTT客户端
void mqttInit()
{
    mqtt.setServer(MQTT_BROKER, MQTT_PORT);
    mqtt.setCallback(mqttCallback);
    mqtt.setSocketTimeout(10);
    mqtt.setKeepAlive(20);
    count = 0;
}

// MQTT 循环处理
void mqttLoop()
{
    if (WiFi.status() != WL_CONNECTED)
    {
        static unsigned long lastWifiWarn = 0;
        if (millis() - lastWifiWarn > 10000)
        {
            lastWifiWarn = millis();
        }
        return;
    }

    if (!mqtt.connected() && count < 3)
    {
        Serial.println("[MQTT] 连接已断开，尝试重连...");
        mqttReconnect();
    }
    mqtt.loop();
}

// 处理收到的MQTT消息
void mqttCallback(char *topic, byte *payload, unsigned int len)
{
    Serial.println("\n========== MQTT 回调触发 ==========");

    char msg[len + 1];
    memcpy(msg, payload, len);
    msg[len] = '\0';

    Serial.printf("[MQTT ←] Topic: %s\n", topic);
    Serial.printf("[MQTT ←] Payload: %s\n", msg);

    String cmd = String(topic).substring(strlen(TOPIC_SUB));
    Serial.printf("[MQTT ←] 提取的命令: %s\n", cmd.c_str());

    // JSON 解析
    JsonDocument doc;
    DeserializationError error = deserializeJson(doc, msg);

    if (error)
    {
        Serial.print(F("deserializeJson() failed: "));
        Serial.println(error.f_str());
        return;
    }

    if (cmd.equals("bind"))
    {
        if (doc["status"].as<int>() == 1)
        {
            preferences.putBool("isBind", true);
            Serial.println("设备已绑定");
        }
        else
        {
            wifi.clearConfig();
            Serial.println("设备解绑，清除配置");
            ESP.restart();
        }
        Serial.println("设备绑定状态已更新");
    }
    else if (cmd.equals("control"))
    {
        // MQTT消息解析
        if (!doc["Threshold"].is<JsonObject>())
        {
            Serial.println("JSON 格式不匹配: 缺少 Threshold 对象");
            return;
        }
        mode = doc["mode"].as<String>();
        JsonObject payloadObj = doc["Threshold"];
        float fridgeTempThreshold = payloadObj["fridgeTempThreshold"];
        float freezeTempThreshold = payloadObj["freezeTempThreshold"];
        float currentFridgeTemp = readFridgeTemp();
        float currentFreezeTemp = readFreezeTemp();

        // 根据控制指令执行相应操作
        scenarioModeControl(mode);
        temperatureControl(fridgeTempThreshold, freezeTempThreshold);

        Serial.println("当前模式: " + mode);
        Serial.printf("温度阈值1: %.2f, 温度阈值2: %.2f\n", freezingTemp, refrigerationTemp);
    }
}

// 连接MQTT服务器
void mqttReconnect()
{
    String clientId = MQTT_CLIENT_ID + String(DEV_SN);
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
        count = 0; // 重置重连计数器
        Serial.println("MQTT 已连接");
        mqtt.publish("ESP32 online", clientId.c_str(), true);

        // 订阅主题（使用 # 通配符订阅所有子主题）
        String subTopic = String(TOPIC_SUB) + "#";
        mqtt.subscribe(subTopic.c_str(), 1);
        Serial.println("已订阅: " + subTopic);
    }
    else
    {
        count++;
        Serial.printf("MQTT 连接失败 rc=%d, 重试次数: %d/3\n", mqtt.state(), count);
    }
}

// 发布传感器数据
void publishMessage()
{
    if (!mqtt.connected())
    {
        return;
    }
    float fridgeTemp = readFridgeTemp();
    float freezeTemp = readFreezeTemp();
    if (isnan(fridgeTemp) || isnan(freezeTemp))
        return;

    // 使用 ArduinoJson 构建 JSON
    JsonDocument doc;
    JsonObject root = doc.to<JsonObject>();
    root["status"] = static_cast<int>(currentStatus);
    root["mode"] = mode;

    // 3. 创建嵌套对象 (sensor)
    JsonObject sensor = root["data"].to<JsonObject>();
    sensor["fridgeTemp"] = fridgeTemp;
    sensor["freezeTemp"] = freezeTemp;
    char payload[256];
    serializeJson(doc, payload);
    for (size_t i = 0; i < sizeof(TOPIC_PUB); i++)
    {
        String topic = String(TOPIC_PUB[i]) + "update";
        mqtt.publish(topic.c_str(), payload);
    }
    Serial.printf("[MQTT →] Published to %s and %s: %s\n", TOPIC_PUB[0], TOPIC_PUB[1], payload);
}

// 检查MQTT连接状态
bool mqttIsConnected()
{
    return mqtt.connected();
}
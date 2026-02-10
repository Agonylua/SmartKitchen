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
#include "main.h"

WiFiClient espClient;
PubSubClient mqtt(espClient);

int count = 0;

// 初始化MQTT客户端
void mqttInit()
{
    mqtt.setServer(MQTT_BROKER, MQTT_PORT);
    mqtt.setCallback(mqttCallback);
    mqtt.setSocketTimeout(10);
    mqtt.setKeepAlive(30);
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

    if (cmd.equals("reset"))
    {
        factoryReset();
    }
    else if (cmd.equals("bind"))
    {
        if (!String(msg).equals("null"))
        {
            preferences.begin("deviceConfig", false);
            preferences.putString("homeId", String(msg));
            preferences.end();
            Serial.println("设备已绑定");
        }
        else
        {
            wifi.resetSettings();
            Serial.println("设备解绑，清除配置");
            ESP.restart();
        }
        Serial.println("设备绑定状态已更新");
    }
    else if (cmd.equals("control"))
    {
        // 检查模式字段
        if (!doc["mode"].is<String>())
        {
            Serial.println("JSON 格式不匹配: 缺少 mode 字段");
            return;
        }
        String mode = doc["mode"].as<String>();

        // 解析 data 字段（可能是对象或字符串）
        JsonDocument dataDoc;
        JsonObject payloadObj;

        if (doc["data"].is<JsonObject>())
        {
            // data 是对象
            payloadObj = doc["data"];
        }
        else if (doc["data"].is<String>())
        {
            // data 是字符串，需要反序列化
            String dataStr = doc["data"].as<String>();
            Serial.printf("[DEBUG] data 是字符串，反序列化: %s\n", dataStr.c_str());

            DeserializationError dataError = deserializeJson(dataDoc, dataStr);
            if (dataError)
            {
                Serial.printf("data 字符串解析失败: %s\n", dataError.f_str());
                return;
            }
            payloadObj = dataDoc.as<JsonObject>();
        }
        else
        {
            Serial.println("JSON 格式不匹配: data 字段格式错误");
            return;
        }

        // 检查温度阈值字段（兼容两种命名）
        float fridgeTempThreshold = 0;
        float freezeTempThreshold = 0;

        if (payloadObj.containsKey("fridgeTempThreshold") && payloadObj.containsKey("freezeTempThreshold"))
        {
            fridgeTempThreshold = payloadObj["fridgeTempThreshold"].as<float>();
            freezeTempThreshold = payloadObj["freezeTempThreshold"].as<float>();
        }
        else
        {
            Serial.println("JSON 格式不匹配: 缺少温度阈值字段");
            return;
        }

        Serial.println("当前模式: " + mode);
        Serial.printf("[DEBUG] 解析的温度阈值 - 冷藏: %.2f, 冷冻: %.2f\n", fridgeTempThreshold, freezeTempThreshold);

        // 根据控制指令执行相应操作
        scenarioModeControl(mode, fridgeTempThreshold, freezeTempThreshold);
    }
}

// 连接MQTT服务器
void mqttReconnect()
{
    String clientId = MQTT_CLIENT_ID;
    Serial.printf("MQTT 连接中... ID: %s\n", clientId.c_str());

    bool ok;
    if (MQTT_USERNAME && MQTT_PASSWORD)
    {
        ok = mqtt.connect(clientId.c_str(), MQTT_USERNAME, MQTT_PASSWORD, TOPIC_STATUS, 1, true, MSG_LWT, true);
    }
    else
    {
        ok = mqtt.connect(clientId.c_str(), nullptr, nullptr, TOPIC_STATUS, 1, true, MSG_LWT, true);
    }

    if (ok)
    {
        count = 0; // 重置重连计数器
        Serial.println("MQTT 已连接");
        mqtt.publish(TOPIC_STATUS, MSG_ONLINE, true);

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

void publishBindVerify()
{
    Serial.println("[publishBindVerify] 开始执行解绑消息发送...");

    if (!mqtt.connected())
    {
        Serial.println("[publishBindVerify] MQTT未连接，尝试重连...");
        mqttReconnect();

        // 等待连接建立
        int retry = 0;
        while (!mqtt.connected() && retry < 10)
        {
            delay(500);
            mqtt.loop();
            retry++;
            Serial.printf("[publishBindVerify] 等待连接... %d/10\n", retry);
        }

        if (!mqtt.connected())
        {
            Serial.println("[publishBindVerify] MQTT连接失败，无法发送解绑消息");
            return;
        }
    }

    String topic = String(TOPIC_PUB[0]) + "unbind";
    bool result = mqtt.publish(topic.c_str(), "");
    Serial.printf("[MQTT →] Published to %s, Result: %s\n", topic.c_str(), result ? "成功" : "失败");
}

// 发布传感器数据
void publishSensorData()
{
    if (!mqtt.connected())
    {
        return;
    }

    float fridgeTemp = readFridgeTemp();
    float freezeTemp = readFreezeTemp();
    if (isnan(fridgeTemp) || isnan(freezeTemp) || fridgeTemp == 0 || freezeTemp == 0)
        return;

    // 使用 ArduinoJson 构建 JSON
    JsonDocument doc;
    JsonObject root = doc.to<JsonObject>();

    root["mode"] = deviceModeToString(currentMode);

    // 3. 创建嵌套对象 (sensor)
    JsonObject sensor = root["data"].to<JsonObject>();
    sensor["fridgeTemp"] = fridgeTemp;
    sensor["freezeTemp"] = freezeTemp;
    char payload[256];
    serializeJson(doc, payload);
    for (size_t i = 0; i < TOPIC_PUB_COUNT; i++)
    {
        String topic = String(TOPIC_PUB[i]) + "update";
        mqtt.publish(topic.c_str(), payload);
    }
}

// 将DeviceMode枚举转换为字符串
String deviceModeToString(DeviceMode mode)
{
    switch (mode)
    {
    case DeviceMode::STANDARD:
        return "STANDARD";
    case DeviceMode::FAST_COOL:
        return "FAST_COOL";
    case DeviceMode::ENERGY_SAVING:
        return "ENERGY_SAVING";
    case DeviceMode::HOLIDAY:
        return "HOLIDAY";
    default:
        return "STANDARD";
    }
}

// 检查MQTT连接状态
bool mqttIsConnected()
{
    return mqtt.connected();
}
#include "wifiManager.h"
#include "common.h"

WiFiConnector::WiFiConnector() : status(ConnectionStatus::DISCONNECTED) {}

bool WiFiConnector::begin()
{
    preferences.begin("wifi-config", false);
    loadConfig();

    WiFi.mode(WIFI_STA);                 // SmartConfig 必须工作在 STA 模式
    WiFi.setHostname("SK-156400423935"); // 建议统一 Hostname

    Serial.printf("[WiFi] MAC: %s\n", WiFi.macAddress().c_str());
    return true;
}

void WiFiConnector::connect()
{
    // 如果没有读取到配置，直接进入配网模式
    if (config.ssid.length() == 0)
    {
        Serial.println("[WiFi] No saved config. Starting SmartConfig...");
        startSmartConfig();
        return;
    }

    Serial.printf("[WiFi] Connecting to stored SSID: %s\n", config.ssid.c_str());
    WiFi.begin(config.ssid.c_str(), config.password.c_str());

    // 尝试连接
    if (!reConnect())
    {
        // 如果连接失败（可能是密码改了），这里策略可以是重试，或者也可以选择进入配网
        // 目前策略：仅保持断开，由 update() 尝试重连
        Serial.println("[WiFi] Connect failed, waiting for update loop or manual reset.");
    }
}

void WiFiConnector::startSmartConfig()
{
    // 清除旧连接
    WiFi.disconnect();
    status = ConnectionStatus::SMART_CONFIG;

    Serial.println("\r\n[WiFi] Waiting for SmartConfig...");
    // 开启 SmartConfig
    WiFi.beginSmartConfig();
}

void WiFiConnector::checkSmartConfig()
{
    // 检查是否收到配网数据
    bool smartConfigDone = WiFi.smartConfigDone();
    if (smartConfigDone)
    {
        // 检查绑定状态
        preferences.begin("config", true);
        bool isBind = preferences.getBool("isBind", false);
        preferences.end();

        Serial.printf("[WiFi] isBind: %d\n", isBind ? 1 : 0);
        Serial.println("\r\n[WiFi] SmartConfig Received!");
        Serial.printf("[WiFi] SSID: %s\n", WiFi.SSID().c_str());
        Serial.printf("[WiFi] PASS: %s\n", WiFi.psk().c_str());

        // 更新内存中的配置
        config.ssid = WiFi.SSID();
        config.password = WiFi.psk();

        // 重新打开 wifi-config 命名空间并保存
        preferences.begin("wifi-config", false);
        saveConfig();
        preferences.end();

        // 停止 SmartConfig
        WiFi.stopSmartConfig();

        Serial.println("[WiFi] Config saved. Restarting to apply...");
        delay(1000);
        ESP.restart(); // 重启
    }
}

void WiFiConnector::update()
{
    // 1. 如果处于配网模式，持续检测是否完成
    if (status == ConnectionStatus::SMART_CONFIG)
    {
        checkSmartConfig();
        // 这里可以加一个非阻塞的 LED 闪烁逻辑提示用户
        return;
    }

    // 2. 正常连接保活逻辑
    if (WiFi.status() != WL_CONNECTED && status == ConnectionStatus::CONNECTED)
    {
        Serial.println("[WiFi] Connection lost!");
        status = ConnectionStatus::DISCONNECTED;
    }

    // 3. 断线重连逻辑
    if (WiFi.status() != WL_CONNECTED &&
        status == ConnectionStatus::DISCONNECTED &&
        config.ssid.length() > 0)
    {
        unsigned long currentTime = millis();

        if (retryCount < MAX_RETRY_COUNT &&
            (currentTime - lastRetryTime >= RETRY_DELAY || lastRetryTime == 0))
        {
            lastRetryTime = currentTime;
            retryCount++;
            Serial.printf("[WiFi] Retry connection %d/%d\n", retryCount, MAX_RETRY_COUNT);
            WiFi.begin(config.ssid.c_str(), config.password.c_str());
        }
    }
}

void WiFiConnector::disconnect()
{
    Serial.println("[WiFi] Disconnecting...");
    WiFi.disconnect();
    status = ConnectionStatus::DISCONNECTED;
    retryCount = 0;
}

String WiFiConnector::getStatus()
{
    if (status == ConnectionStatus::SMART_CONFIG)
        return "SmartConfig Mode";
    return (status == ConnectionStatus::CONNECTED) ? "Connected" : "Disconnected";
}

String WiFiConnector::getWiFiStatus()
{
    return (WiFi.status() == WL_CONNECTED) ? "Connected" : "Disconnected";
}

void WiFiConnector::displayConnectionInfo()
{
    Serial.println("\n=== WiFi Info ===");
    Serial.printf("Internal Status: %s\n", getStatus().c_str());
    Serial.printf("ESP Status: %d\n", WiFi.status());

    if (WiFi.status() == WL_CONNECTED)
    {
        Serial.printf("SSID: %s\n", WiFi.SSID().c_str());
        Serial.printf("IP: %s\n", WiFi.localIP().toString().c_str());
        Serial.printf("RSSI: %d dBm\n", WiFi.RSSI());
    }
    Serial.println("=================\n");
}

bool WiFiConnector::isConnected()
{
    return WiFi.status() == WL_CONNECTED;
}

bool WiFiConnector::reConnect()
{
    unsigned long startTime = millis();
    Serial.print("[WiFi] Connecting");

    // 简单的阻塞等待，用于首次上电快速连接
    while (WiFi.status() != WL_CONNECTED)
    {
        if (millis() - startTime > CONNECTION_TIMEOUT)
        {
            Serial.println("\n[WiFi] Timeout!");
            // 注意：这里超时仅返回 false，具体的重试或进入 SmartConfig 由外部逻辑或 update 控制
            return false;
        }
        delay(500);
        Serial.print(".");
    }

    Serial.println("\n[WiFi] Connected!");
    status = ConnectionStatus::CONNECTED;
    retryCount = 0;
    Serial.printf("[WiFi] IP: %s\n", WiFi.localIP().toString().c_str());
    return true;
}

void WiFiConnector::saveConfig()
{
    Serial.println("[WiFi] Saving config...");
    preferences.putString("ssid", config.ssid);
    preferences.putString("password", config.password);
    Serial.printf("[WiFi] Saved - SSID: %s, Password: %s\n", config.ssid.c_str(), config.password.c_str());
}

void WiFiConnector::loadConfig()
{
    config.ssid = preferences.getString("ssid", "");
    config.password = preferences.getString("password", "");

    if (config.ssid.length() > 0)
    {
        Serial.printf("[WiFi] Loaded Config - SSID: %s, Password: %s\n", config.ssid.c_str(), config.password.c_str());
    }
    else
    {
        Serial.println("[WiFi] No saved config found.");
    }
}

void WiFiConnector::clearConfig()
{
    Serial.println("[WiFi] Clearing configuration...");
    preferences.begin("wifi-config", false);
    preferences.clear();
    preferences.end();

    config.ssid = "";
    config.password = "";
    Serial.println("[WiFi] Configuration cleared!");
}

WiFiConnector wifi;
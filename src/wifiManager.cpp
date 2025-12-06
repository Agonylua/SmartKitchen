#include "wifiManager.h"

WiFiConnector::WiFiConnector() : status(ConnectionStatus::DISCONNECTED) {}

bool WiFiConnector::begin()
{
    preferences.begin("wifi-config", false);
    loadConfig();

    WiFi.mode(WIFI_STA);
    WiFi.setAutoReconnect(true);
    WiFi.setHostname("Refrigerator");

    Serial.printf("[WiFi] MAC: %s\n", WiFi.macAddress().c_str());
    return true;
}

void WiFiConnector::setConfig(const String &ssid, const String &password)
{
    config.ssid = ssid;
    config.password = password;
    saveConfig();
    Serial.printf("[WiFi] Set: %s\n", ssid.c_str());
}

bool WiFiConnector::connect()
{
    if (config.ssid.length() == 0)
    {
        Serial.println("[WiFi] Error: No SSID!");
        return false;
    }

    Serial.printf("[WiFi] Connecting to: %s\n", config.ssid.c_str());
    WiFi.begin(config.ssid.c_str(), config.password.c_str());

    return reConnect();
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
    return (status == ConnectionStatus::CONNECTED) ? "Connected" : "Disconnected";
}

String WiFiConnector::getWiFiStatus()
{
    switch (WiFi.status())
    {
    case WL_CONNECTED:
        return "Connected";
    case WL_DISCONNECTED:
        return "Disconnected";
    default:
        return "Unknown";
    }
}

void WiFiConnector::displayConnectionInfo()
{
    Serial.println("\n=== WiFi Info ===");
    Serial.printf("Status: %s\n", getStatus().c_str());

    if (WiFi.status() == WL_CONNECTED)
    {
        Serial.printf("SSID: %s\n", WiFi.SSID().c_str());
        Serial.printf("IP: %s\n", WiFi.localIP().toString().c_str());
        Serial.printf("RSSI: %d dBm\n", WiFi.RSSI());
    }
    Serial.println("=================\n");
}

void WiFiConnector::update()
{
    if (WiFi.status() != WL_CONNECTED && status == ConnectionStatus::CONNECTED)
    {
        Serial.println("[WiFi] Connection lost!");
        status = ConnectionStatus::DISCONNECTED;
    }

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

            Serial.printf("[WiFi] Retry %d/%d\n", retryCount, MAX_RETRY_COUNT);
            connect();
        }
    }
}

bool WiFiConnector::isConnected()
{
    return WiFi.status() == WL_CONNECTED;
}

bool WiFiConnector::reConnect()
{
    unsigned long startTime = millis();
    Serial.print("[WiFi] Connecting");

    while (WiFi.status() != WL_CONNECTED)
    {
        if (millis() - startTime > CONNECTION_TIMEOUT)
        {
            Serial.println("\n[WiFi] Timeout!");
            status = ConnectionStatus::DISCONNECTED;
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
    preferences.putString("ssid", config.ssid);
    preferences.putString("password", config.password);
}

void WiFiConnector::loadConfig()
{
    config.ssid = preferences.getString("ssid", "");
    config.password = preferences.getString("password", "");

    if (config.ssid.length() > 0)
    {
        Serial.printf("[WiFi] Loaded: %s\n", config.ssid.c_str());
    }
    else
    {
        Serial.println("[WiFi] No saved config");
    }
}

WiFiConnector wifi;
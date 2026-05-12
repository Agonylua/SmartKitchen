#include "wifiManager.h"
#include <esp_wifi.h> // 引入底层 ESP-IDF WiFi 库以读取配置

// 全局静态指针
static WiFiConnector *instance = nullptr;

WiFiConnector::WiFiConnector() : status(ConnectionStatus::DISCONNECTED)
{
    instance = this;
}

// 事件回调
void WiFiConnector::SysProvEvent(arduino_event_t *sys_event)
{
    switch (sys_event->event_id)
    {
    case ARDUINO_EVENT_WIFI_STA_GOT_IP:
        Serial.print("\n[WiFi] Connected! IP: ");
        Serial.println(IPAddress(sys_event->event_info.got_ip.ip_info.ip.addr));
        if (instance)
            instance->status = ConnectionStatus::CONNECTED;
        break;

    case ARDUINO_EVENT_WIFI_STA_DISCONNECTED:
        Serial.println("\n[WiFi] Disconnected. Reason: " + String(sys_event->event_info.wifi_sta_disconnected.reason));
        if (instance)
        {
            instance->status = ConnectionStatus::DISCONNECTED;
            // 移除断开连接立刻重启/清除配置的逻辑，改为仅修改内部状态
            // instance->resetSettings();
        }
        break;

    case ARDUINO_EVENT_PROV_START:
        Serial.println("\n[Provisioning] Started. Please scan for BLE device: " + String(instance->service_name));
        if (instance)
            instance->status = ConnectionStatus::PROVISIONING;
        break;

    case ARDUINO_EVENT_PROV_CRED_RECV:
        Serial.println("\n[Provisioning] Credentials Received...");
        break;

    case ARDUINO_EVENT_PROV_CRED_FAIL:
        Serial.println("\n[Provisioning] Failed!");
        break;

    case ARDUINO_EVENT_PROV_CRED_SUCCESS:
        Serial.println("\n[Provisioning] Success! Connecting...");
        break;

    case ARDUINO_EVENT_PROV_END:
        Serial.println("\n[Provisioning] Session Ended.");
        ESP.restart();
        break;

    default:
        break;
    }
}

bool WiFiConnector::begin()
{
    Serial.println("[WiFi] Initializing...");

    // 注册事件
    WiFi.onEvent(SysProvEvent);

    // 必须先设置模式才能读取配置
    WiFi.mode(WIFI_STA);

    // 检查是否已经配网 (通过检查 NVS 中是否有 SSID)
    bool has_config = false;
    wifi_config_t conf;
    if (esp_wifi_get_config(WIFI_IF_STA, &conf) == ESP_OK)
    {
        if (strlen((const char *)conf.sta.ssid) > 0)
        {
            has_config = true;
            Serial.printf("[WiFi] Found saved SSID: %s\n", (const char *)conf.sta.ssid);
        }
    }

    if (has_config)
    {
        Serial.println("[WiFi] Connecting with saved credentials...");
        status = ConnectionStatus::CONNECTING;
        WiFi.begin();
    }
    else
    {
        Serial.println("[WiFi] No saved credentials. Starting Provisioning...");
        status = ConnectionStatus::PROVISIONING;

        // 修正：直接使用全局常量，去掉 WiFiProv. 前缀
        WiFiProv.beginProvision(
            WIFI_PROV_SCHEME_BLE,
            WIFI_PROV_SCHEME_HANDLER_FREE_BTDM,
            WIFI_PROV_SECURITY_1,
            pop,
            service_name);
    }

    preferences.begin("wifi-config", false);
    return true;
}

void WiFiConnector::resetSettings()
{
    Serial.println("[WiFi] Resetting settings...");
    WiFi.disconnect(true, true); // 清除 Wi-Fi 凭证 (erase = true)
    preferences.clear();

    Serial.println("[WiFi] Reset complete. Restarting...");
    delay(1000);
    ESP.restart();
}

void WiFiConnector::update()
{
    // 应用层简单的看门狗，如果长期断开且已配网，可以尝试重连
    if (status == ConnectionStatus::DISCONNECTED)
    {
        unsigned long currentTime = millis();
        // 只有在非配网模式下才尝试重连
        if (WiFi.status() != WL_CONNECTED && (currentTime - lastRetryTime > RETRY_DELAY))
        {
            // 检查是否处于配网模式，如果正在配网则不打断
            // 这里简单判断：如果是通过 beginProvision 启动的，底层会自动处理
        }
    }
}

String WiFiConnector::getStatus()
{
    switch (status)
    {
    case ConnectionStatus::DISCONNECTED:
        return "Disconnected";
    case ConnectionStatus::PROVISIONING:
        return "Provisioning";
    case ConnectionStatus::CONNECTING:
        return "Connecting";
    case ConnectionStatus::CONNECTED:
        return "Connected";
    default:
        return "Unknown";
    }
}

String WiFiConnector::getWiFiStatus()
{
    switch (WiFi.status())
    {
    case WL_CONNECTED:
        return "WL_CONNECTED";
    case WL_DISCONNECTED:
        return "WL_DISCONNECTED";
    case WL_CONNECTION_LOST:
        return "WL_CONNECTION_LOST";
    case WL_NO_SHIELD:
        return "WL_NO_SHIELD";
    case WL_IDLE_STATUS:
        return "WL_IDLE_STATUS";
    case WL_NO_SSID_AVAIL:
        return "WL_NO_SSID_AVAIL";
    default:
        return "Unknown";
    }
}

void WiFiConnector::displayConnectionInfo()
{
    Serial.println("\n=== WiFi Info ===");
    Serial.printf("Status: %s\n", getStatus().c_str());
    if (isConnected())
    {
        Serial.printf("SSID: %s\n", WiFi.SSID().c_str());
        Serial.printf("IP: %s\n", WiFi.localIP().toString().c_str());
        Serial.printf("Signal: %d dBm\n", WiFi.RSSI());
    }
    Serial.println("=================\n");
}

bool WiFiConnector::isConnected()
{
    return (WiFi.status() == WL_CONNECTED);
}

WiFiConnector wifi;
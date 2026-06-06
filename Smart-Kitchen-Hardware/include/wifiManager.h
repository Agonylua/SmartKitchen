#ifndef WIFI_MANAGER_H_
#define WIFI_MANAGER_H_

#include <WiFi.h>
#include <WiFiProv.h>
#include <Preferences.h>
#include "mqttConfig.h"
#include <esp_wifi.h>
#include <ArduinoJson.h>
#include <wifi_provisioning/manager.h>
#include <wifi_provisioning/scheme_ble.h>

class WiFiConnector
{
private:
    struct WiFiConfig
    {
        String ssid;
    };

    enum class ConnectionStatus
    {
        DISCONNECTED,
        PROVISIONING,
        CONNECTING,
        CONNECTED
    };

    WiFiConfig config;
    ConnectionStatus status;

    // 参数配置
    static const unsigned long RETRY_DELAY = 5000;
    unsigned long lastRetryTime = 0;

    // 配网参数
    const char *service_name = "SK-R001";
    const char *pop = "DYCMV7";

public:
    Preferences preferences;
    WiFiConnector();

    bool begin();
    void resetSettings();
    String getStatus();
    String getWiFiStatus();
    void displayConnectionInfo();
    void update();
    bool isConnected();

private:
    static void SysProvEvent(arduino_event_t *sys_event);
};

extern WiFiConnector wifi;

#endif // WIFI_MANAGER_H_
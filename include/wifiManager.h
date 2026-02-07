#ifndef WIFI_MANAGER_H_
#define WIFI_MANAGER_H_

#include <WiFi.h>
#include <Preferences.h>

class WiFiConnector
{
private:
    struct WiFiConfig
    {
        String ssid;
        String password;
    };

    enum class ConnectionStatus
    {
        DISCONNECTED,
        CONNECTED,
        SMART_CONFIG
    };

    WiFiConfig config;
    ConnectionStatus status;
    Preferences preferences;

    // 重连配置
    static const int MAX_RETRY_COUNT = 3;
    static const unsigned long RETRY_DELAY = 10000;
    static const unsigned long CONNECTION_TIMEOUT = 15000;

    int retryCount = 0;
    unsigned long lastRetryTime = 0;

public:
    WiFiConnector();

    bool begin();

    // 修改：connect 逻辑将自动判断是否需要配网
    void connect();

    void disconnect();

    // 手动强制进入配网模式（可绑定物理按键）
    void startSmartConfig();

    String getStatus();
    String getWiFiStatus();
    void displayConnectionInfo();
    void update();
    bool isConnected();

    // 新增：清除配置（用于重置设备）
    void clearConfig();

private:
    bool reConnect();
    void saveConfig();
    void loadConfig();

    // 新增：检查 SmartConfig 状态
    void checkSmartConfig();
};

extern WiFiConnector wifi;

#endif // WIFI_MANAGER_H_
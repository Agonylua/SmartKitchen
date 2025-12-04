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
        CONNECTED
    };

    WiFiConfig config;
    ConnectionStatus status;
    Preferences preferences;

    // 重连配置
    static const int MAX_RETRY_COUNT = 3;                  // 最大重连3次
    static const unsigned long RETRY_DELAY = 10000;        // 10秒重连间隔
    static const unsigned long CONNECTION_TIMEOUT = 15000; // 15秒连接超时

    int retryCount = 0;              // 当前重试次数
    unsigned long lastRetryTime = 0; // 上次重试时间

public:
    WiFiConnector();

    bool begin();

    void setConfig(const String &ssid, const String &password);

    bool connect();

    void disconnect();

    String getStatus();

    String getWiFiStatus();

    void displayConnectionInfo();

    void update();

    bool isConnected();

private:
    bool reConnect();

    void saveConfig();

    void loadConfig();
};

extern WiFiConnector wifi;

#endif // WIFI_MANAGER_H_

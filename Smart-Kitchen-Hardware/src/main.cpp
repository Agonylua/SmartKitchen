#include "main.h"
#include <esp_wifi.h>

// 全局变量定义
String homeId = "";
DeviceMode currentMode = DeviceMode::STANDARD;
DeviceStatus currentStatus = DeviceStatus::UNBOUND;
const char *DEV_SN = "SK-156400423935CCBA9720B608";
Preferences preferences;
unsigned long pressStartTime = 0;
bool isPressing = false;
bool isBind = false;
const unsigned long RESET_TIME_MS = 2000; // 5秒
unsigned long lastDisplayUpdate = 0;
Ticker ticker;

void setup()
{
  Serial.begin(115200);
  delay(2000);
  // 初始化显示模块
  displayMgr.begin();
  displayMgr.update(0);
  // 初始化WiFi
  wifi.begin();
  // 初始化MQTT
  mqttInit();
  // 初始化DHT11
  dht_begin();
  // 初始化RGB
  RGBinit();
  // 初始化按键
  pinMode(RESET_BTN_PIN, INPUT_PULLUP);

  // 初始状态设定
  preferences.begin("deviceConfig", false);
  String savedHomeId = preferences.getString("homeId", "");
Serial.printf("[Setup] 从 NVS 读取的 homeId: %s\n", savedHomeId.c_str());
  // 获取底层 WiFi 配置缓存
  wifi_config_t conf;
  bool hasWifiCache = false;
  if (esp_wifi_get_config(WIFI_IF_STA, &conf) == ESP_OK)
  {
    if (strlen((const char *)conf.sta.ssid) > 0)
    {
      hasWifiCache = true;
    }
  }

  if (!savedHomeId.isEmpty() && !savedHomeId.equals(""))
  {
    homeId = savedHomeId;
    isBind = true;
    // 温度监测
    ticker.attach(5.0, publishSensorData);
    Serial.println("设备已绑定");
    preferences.end();
  }
  else
  {
    if (hasWifiCache)
    {
      Serial.println("[System] 没有绑定家庭 ID，但残留有网络缓存，执行清空并重启...");
      preferences.clear();
      preferences.end();
      wifi.resetSettings();
    }
    else
    {
      Serial.println("[System] 暂未绑定或已为初始状态，等待配网无需重启...");
      preferences.end();
    }
  }
}

void loop()
{
  wifi.update();
  mqttLoop();
  orderController();
  temperatureControl();

  // === 按键长按检测 ===
  if (digitalRead(RESET_BTN_PIN) == LOW)
  { // 假设低电平有效
    if (!isPressing)
    {
      isPressing = true;
      pressStartTime = millis();
      Serial.println("[Button] Pressed...");
    }
    else
    {
      unsigned long duration = millis() - pressStartTime;

      // 可选：在串口打印倒计时
      if (duration % 1000 == 0)
      {
        Serial.printf("Resetting in %lu s...\n", (RESET_TIME_MS - duration) / 1000);
      }

      if (duration > RESET_TIME_MS)
      {
        factoryReset(); // 触发重置
      }
    }
  }
  else
  {
    if (isPressing)
    {
      Serial.println("[Button] Released.");
      isPressing = false;
    }
  }

  // 刷新显示
  if (millis() - lastDisplayUpdate > 200)
  {
    lastDisplayUpdate = millis();

    int currentStatus = 0; // 0 代表正常工作界面

    if (isPressing)
    {
      currentStatus = -1; // -1 代表处于配网或系统特殊状态
    }

    // 调用我们在 displayManager 中封装好的核心刷新函数
    displayMgr.update(currentStatus);
  }
}

// 工厂重置函数
void factoryReset()
{
  Serial.println("\n[System] !!! FACTORY RESET TRIGGERED !!!");

  // 屏幕提示
  displayMgr.update(-1);
  delay(500);

  // 发送MQTT解绑消息
  preferences.begin("deviceConfig", false);
  if (preferences.getString("homeId", "").isEmpty())
  {
    publishUnBind();
    delay(2000);
  }


  // 清除设备绑定信息
  Serial.println("[FactoryReset] 清除设备绑定信息...");
  preferences.clear();
  preferences.end();

  // 清除 WiFi 配置
  Serial.println("[FactoryReset] 清除WiFi配置...");
  wifi.resetSettings();
}

// 处理串口命令
void orderController()
{
  if (Serial.available())
  {
    String cmd = Serial.readStringUntil('\n');
    cmd.trim();
    String command = "";
    String param1 = "";
    String param2 = "";
    if (cmd.length() > 0)
    {
      int firstSpace = cmd.indexOf(' ');
      // not Parameter
      if (firstSpace == -1)
      {
        command = cmd;

        if (command == "info" || command == "i")
        {
          wifi.displayConnectionInfo();
        }
        else if (command == "help" || command == "h")
        {
          showHelp();
        }
        else if (command == "dht" || command == "d")
        {
          Serial.printf("FridgeTemp: %.2f, FreezeTemp: %.2f\n", readFridgeTemp(), readFreezeTemp());
        }
        else if (command == "send" || command == "s")
        {
          publishSensorData();
        }
        else if (command == "tempThr" || command == "t")
        {
          Serial.printf("Temp Threshold 1: %.2f, Temp Threshold 2: %.2f\n", targetFreezeTemp, targetFridgeTemp);
        }
        else if (command == "reset" || command == "r")
        {
          Serial.println("Resetting WiFi config and restarting...");
          wifi.resetSettings();
          delay(500);
          ESP.restart();
        }
        else
        {
          publishSensorData();
        }
        return;
      }
    }
  }
}

void showHelp()
{
  Serial.println("Available commands:");
  Serial.println("  info (i)      - Display WiFi connection info");
  Serial.println("  reset (r)     - Clear WiFi config and restart");
  Serial.println("  dht (d)       - Read DHT11 sensor values");
  Serial.println("  send          - Publish MQTT message");
  Serial.println("  help (h)      - Show this help message");
}
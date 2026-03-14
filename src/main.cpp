#include "main.h"

// 全局变量定义
DeviceMode currentMode = DeviceMode::STANDARD;
DeviceStatus currentStatus = DeviceStatus::UNBOUND;
const char *DEV_SN = "SK-156400423935CCBA9720B608";
Preferences preferences;
unsigned long pressStartTime = 0;
bool isPressing = false;
bool isBind = false;
const unsigned long RESET_TIME_MS = 20000; // 20秒
Ticker ticker;

void setup()
{
  Serial.begin(115200);
  delay(2000);
  // 初始化显示模块
  displayMgr.begin();
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
  // if (!preferences.getString("homeId", "").isEmpty())
  // {
  //   isBind = true;
  //   // 温度监测
  //   ticker.attach(10.0, publishSensorData);
  // }
  preferences.end();
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

  // 刷新显示 (内部有状态判断，不会一直刷屏)
  displayMgr.update(0);
  delay(200);
}

// 工厂重置函数
void factoryReset()
{
  Serial.println("\n[System] !!! FACTORY RESET TRIGGERED !!!");

  // 1. 屏幕提示
  displayMgr.update(-1);
  delay(500);

  // 2. 发送MQTT解绑消息
  publishBindVerify();
  delay(2000);

  // 3. 清除 WiFi 配置
  Serial.println("[FactoryReset] 清除WiFi配置...");
  wifi.resetSettings();

  // 4. 清除设备绑定信息
  Serial.println("[FactoryReset] 清除设备绑定信息...");
  preferences.begin("deviceConfig", false);
  preferences.clear();
  preferences.end();

  // 5. 重启
  Serial.println("[FactoryReset] 3秒后重启...");
  delay(3000);
  ESP.restart();
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
        else if (command == "time")
        {
          printCurrentTime();
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
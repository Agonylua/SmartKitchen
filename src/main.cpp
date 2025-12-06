#include <Arduino.h>
#include <PubSubClient.h>
#include "wifiManager.h"
#include "dht11.h"
#include "mqttConfig.h"

#define WIFI_SSID "God-wifi"
#define WIFI_PASSWORD "111111111"

void orderController();
void showHelp();
void dht11Date();

void setup()
{
  Serial.begin(115200);
  delay(2000);
  
  if (wifi.begin())
  {
    Serial.println("[System] WiFi initialized");
  }
  wifi.setConfig(WIFI_SSID, WIFI_PASSWORD);
  wifi.connect();
  delay(5000);
  if(wifi.isConnected()){
    mqttInit();
  }
  dht_begin();
}

void loop()
{
  wifi.update();
  mqttLoop();
  orderController();
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
      if (firstSpace == -1){
        command = cmd;

        if (command == "info" || command == "i")
        {
          wifi.displayConnectionInfo();
        }
        else if (command == "help" || command == "h")
        {
          showHelp();
        }
        else if (command == "send")
        {
          publishMessage();
        }
        return;
      }
      // one Parameter
      param1 = cmd.substring(0, firstSpace);

      int secondSpace = cmd.indexOf(' ', firstSpace + 1);

      if (secondSpace == -1)
      {
        param1 = cmd.substring(firstSpace + 1);
        //.......
        //.........
        return;
      }
      // two Parameters
      param1 = cmd.substring(firstSpace + 1, secondSpace);
      param2 = cmd.substring(secondSpace + 1);
      if (command == "config")
      {
        wifi.setConfig(param1, param2);
        wifi.connect();
      }
    }
  }
}

void showHelp()
{
  Serial.println("Available commands:");
  Serial.println("  info (i)      - Display WiFi connection info");
  Serial.println("  config [ssid] [password]    - Show WiFi status");
  Serial.println("  send          - Publish MQTT message");
  Serial.println("  help (h)      - Show this help message");
}
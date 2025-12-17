#include <Arduino.h>
#include <Ticker.h>
#include <PubSubClient.h>
#include "wifiManager.h"
#include "dht11.h"
#include "mqttConfig.h"
#include "control.h"

#define WIFI_SSID "God-wifi"
#define WIFI_PASSWORD "111111111"




void orderController();
void showHelp();
void dht11Data();
void RGBinit();
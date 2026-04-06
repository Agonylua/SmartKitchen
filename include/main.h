#include "timeUtils.h"
#include "common.h"
#include "displayManager.h"
#include <Arduino.h>
#include <Ticker.h>
#include <PubSubClient.h>
#include "wifiManager.h"
#include "dht11.h"
#include "mqttConfig.h"
#include "control.h"

// === 定义引脚 ===
#define RESET_BTN_PIN 0
// === 函数前向声明 ===
void orderController();
void factoryReset();
void publishBindVerify();
void showHelp();
// 全局变量声明
extern DeviceStatus currentStatus;
extern const char *DEV_SN;
extern Preferences preferences;
extern bool isBind;
// === 按键计时变量 ===
extern unsigned long pressStartTime;
extern bool isPressing;
extern const unsigned long RESET_TIME_MS;
extern Ticker ticker;
#include <Arduino.h>
#include <WiFi.h>
#include <dht11.h>
#include <Adafruit_NeoPixel.h>
#include <mqttConfig.h>
#include "common.h"

#define LED_PIN 48
#define LED_COUNT 1

extern float targetFreezeTemp;
extern float targetFridgeTemp;
extern float freezingHum; // Keeping hum as is for now
extern float refrigerationHum;
extern bool isFridgeCooling;
extern bool isFridgeWarming;
extern bool isFreezeCooling;
extern bool isFreezeWarming;
extern Adafruit_NeoPixel pixels;

void temperatureControl();
void scenarioModeControl(String mode, float fridgeTempThreshold, float freezeTempThreshold);
void RGBLightControl(String color);
void RGBinit();

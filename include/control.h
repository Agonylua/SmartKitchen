#include <Arduino.h>
#include <WiFi.h>
#include <dht11.h>
#include <Adafruit_NeoPixel.h>
#include <mqttConfig.h>

#define LED_PIN 48
#define LED_COUNT 1

extern String mode;
extern float freezingTemp;
extern float freezingHum;
extern float refrigerationTemp;
extern float refrigerationHum;
extern Adafruit_NeoPixel pixels;

void temperatureControl(float fridgeTempThreshold, float freezeTempThreshold);
void scenarioModeControl(String mode);
void RGBLightControl(String color);
void RGBinit();

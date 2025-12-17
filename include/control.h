#include <Arduino.h>
#include <WiFi.h>
#include <dht11.h>
#include <Adafruit_NeoPixel.h>
#include <mqttConfig.h>

#define LED_PIN 48
#define LED_COUNT 1

extern String mode;
extern float tempThreshold_1;
extern float humThreshold_1;
extern float tempThreshold_2;
extern float humThreshold_2;
extern Adafruit_NeoPixel pixels;

void temperatureControl();
void scenarioModeControl(String mode);
void RGBLightControl(String color);

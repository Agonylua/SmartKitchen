#include "control.h"
#include "dht11.h"
#include <Adafruit_NeoPixel.h>
#include <ArduinoJson.h>

Adafruit_NeoPixel pixels(LED_COUNT, LED_PIN, NEO_GRB + NEO_KHZ800);
float targetFreezeTemp = 25.0;
float freezingHum = 60.0;
float targetFridgeTemp = 5.0;
float refrigerationHum = 30.0;

bool isFridgeCooling = false;
bool isFridgeWarming = false;
bool isFreezeCooling = false;
bool isFreezeWarming = false;

/**
 * @brief 温度控制函数，根据设定的温度阈值进行相应的控制操作
 */
void temperatureControl()
{
    float currentFridgeTemp = readFridgeTemp();
    float currentFreezeTemp = readFreezeTemp();

    // Fridge Control
    if (targetFridgeTemp < currentFridgeTemp-1)
    {
        isFridgeCooling = true;
        isFridgeWarming = false;
        // 执行降温操作
    }
    else if (targetFridgeTemp > currentFridgeTemp+1)
    {
        isFridgeCooling = false;
        isFridgeWarming = true;
        // 执行升温操作
    }
    else
    {
        isFridgeCooling = false;
        isFridgeWarming = false;
    }

    // Freeze Control
    if (targetFreezeTemp < currentFreezeTemp-1)
    {
        isFreezeCooling = true;
        isFreezeWarming = false;
        // 执行降温操作
    }
    else if (targetFreezeTemp > currentFreezeTemp+1)
    {
        isFreezeCooling = false;
        isFreezeWarming = true;
        // 执行升温操作
    }
    else
    {
        isFreezeCooling = false;
        isFreezeWarming = false;
    }
}

/**
 * @brief 场景模式控制函数，根据传入的模式参数执行相应的控制操作
 * @param mode 模式字符串
 */
void scenarioModeControl(String mode, float fridgeTempThreshold, float freezeTempThreshold)
{
    if (mode == "STANDARD")
    {
        currentMode = DeviceMode::STANDARD;
        targetFridgeTemp = fridgeTempThreshold;
        targetFreezeTemp = freezeTempThreshold;
    }
    else if (mode == "FAST_COOL")
    {
        currentMode = DeviceMode::FAST_COOL;
        targetFridgeTemp = 2;
        targetFreezeTemp = -24;
    }
    else if (mode == "ENERGY_SAVING")
    {
        currentMode = DeviceMode::ENERGY_SAVING;
        targetFridgeTemp = 7;
        targetFreezeTemp = -15;
    }
    else if (mode == "HOLIDAY")
    {
        currentMode = DeviceMode::HOLIDAY;
        targetFridgeTemp = 17;
        targetFreezeTemp = -18;
    }

    // Update control state immediately
    temperatureControl();
}

/**
 * @brief RGB灯光控制函数，根据传入的颜色参数设置RGB灯的颜色
 * @param Color 颜色字符串
 */
void RGBLightControl(String Color)
{
    if (Color == "Blue")
    {
        pixels.fill(pixels.Color(0, 0, 255));
    }
    else if (Color == "Green")
    {
        pixels.fill(pixels.Color(0, 255, 0));
    }
    else if (Color == "Red")
    {
        pixels.fill(pixels.Color(255, 0, 0));
    }
    else if (Color == "Yellow")
    {
        pixels.fill(pixels.Color(255, 255, 0));
    }
    else if (Color == "off")
    {
        pixels.fill(pixels.Color(0, 0, 0));
    }
    pixels.show();
}

void RGBinit()
{
    pixels.begin();
    pixels.setBrightness(50);
    pixels.show();
}
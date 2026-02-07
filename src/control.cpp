#include "control.h"
#include "dht11.h"
#include <Adafruit_NeoPixel.h>
#include <ArduinoJson.h>

Adafruit_NeoPixel pixels(LED_COUNT, LED_PIN, NEO_GRB + NEO_KHZ800);
float freezingTemp = 25.0;
float freezingHum = 60.0;
float refrigerationTemp = 5.0;
float refrigerationHum = 30.0;
/**
 * @brief 温度控制函数，根据设定的温度阈值进行相应的控制操作
 * @param region 控制区域标识符
 */
void temperatureControl(float fridgeTempThreshold, float freezeTempThreshold)
{
    float currentFridgeTemp = readFridgeTemp();
    float currentFreezeTemp = readFreezeTemp();
    if (fridgeTempThreshold < currentFridgeTemp)
    {
        // 执行降温操作
    }
    else if (fridgeTempThreshold > currentFridgeTemp)
    {
        // 执行升温操作
    }
    if (freezeTempThreshold < currentFreezeTemp)
    {
        // 执行降温操作
    }
    else if (freezeTempThreshold > currentFreezeTemp)
    {
        // 执行升温操作
    }
}

/**
 * @brief 场景模式控制函数，根据传入的模式参数执行相应的控制操作
 * @param mode 模式字符串
 */
void scenarioModeControl(String mode)
{
    if (mode == "STANDARD")
    {
        /* code */
    }
    else if (mode == "FAST_COOL")
    {
        /* code */
    }
    else if (mode == "ENERGY_SAVING")
    {
        /* code */
    }
    else if (mode == "HOLIDAY")
    {
        /* code */
    }
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
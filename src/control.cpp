#include "control.h"
#include <Adafruit_NeoPixel.h>

Adafruit_NeoPixel pixels(LED_COUNT, LED_PIN, NEO_GRB + NEO_KHZ800);
float tempThreshold_1 = 25.0;
float humThreshold_1 = 60.0;
float tempThreshold_2 = 5.0;
float humThreshold_2 = 30.0;
/**
 * @brief 温度控制函数，根据设定的温度阈值进行相应的控制操作
 * @param region 控制区域标识符
 */
void temperatureControl(){
    float temperature = readTemperature();
    if (isnan(temperature))
    {
        Serial.println("[Temp Control] Failed to read temperature!");
        return;
    }
    if (temperature == tempThreshold_1 && temperature == tempThreshold_2)
    {
        RGBLightControl("off");
        return;
    }
    else if (temperature > tempThreshold_1 && temperature > tempThreshold_2)
    {
        // 执行降温操作
        RGBLightControl("Blue");
        /* 其他业务逻辑 */
    }
    else if (temperature < tempThreshold_1 && temperature < tempThreshold_2)
    {
        // 执行升温操作
        RGBLightControl("Red");
        /* 其他业务逻辑 */
    }
}

/**
 * @brief 场景模式控制函数，根据传入的模式参数执行相应的控制操作
 * @param mode 模式字符串
 */
void scenarioModeControl(String mode){
    if (mode == "SuperCool")
    {
        // 超强制冷模式逻辑
        RGBLightControl("Blue");
        /* 其他业务逻辑 */
    }
    else if (mode == "Holiday")
    {
        // 假日模式逻辑
        RGBLightControl("Yellow");
        /* 其他业务逻辑 */
    }
    else if (mode == "Energysaving")
    {
        // 节能模式逻辑
        RGBLightControl("Green");
        /* 其他业务逻辑 */
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
#include<Arduino.h>
#include<DHT.h>
#include "dht11.h"

DHT dhtFridge(DHTPIN0, DHTTYPE);
DHT dhtFreeze(DHTPIN1, DHTTYPE);
void dht_begin(){
    dhtFridge.begin();
    dhtFreeze.begin();
}
float readFridgeTemp()
{
    return dhtFridge.readTemperature();
}
float readFreezeTemp(){
    return dhtFreeze.readTemperature();
}

void dht11Data()
{
    float fridgeTemp = readFridgeTemp();
    float freezeTemp = readFreezeTemp();

    if (isnan(fridgeTemp) || isnan(freezeTemp))
    {
        Serial.println("[DHT11] Failed to read from DHT sensor!");
        return;
    }

    Serial.printf("[DHT11] fridgeTemp: %.1f °C, freezeTemp: %.1f %%\n", fridgeTemp, freezeTemp);
}
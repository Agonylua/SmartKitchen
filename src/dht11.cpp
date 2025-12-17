#include<Arduino.h>
#include<DHT.h>
#include "dht11.h"


DHT dht(DHTPIN, DHTTYPE);

void dht_begin(){
    dht.begin();
}
float readTemperature(){
    return dht.readTemperature();
}
float readHumidity(){
    return dht.readHumidity();
}

void dht11Data()
{
    float temperature = readTemperature();
    float humidity = readHumidity();

    if (isnan(temperature) || isnan(humidity))
    {
        Serial.println("[DHT11] Failed to read from DHT sensor!");
        return;
    }

    Serial.printf("[DHT11] Temperature: %.1f °C, Humidity: %.1f %%\n", temperature, humidity);
}
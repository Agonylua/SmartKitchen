#include<Arduino.h>
#include<DHT.h>
#include "dht11.h"

#define DHTPIN 1
#define DHTTYPE DHT11

DHT dht(DHTPIN, DHTTYPE);

void begin(){
    dht.begin();
}
float readTemperature(){
    return dht.readTemperature();
}
float readHumidity(){
    return dht.readHumidity();
}
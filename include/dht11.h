#include <Arduino.h>
#include <DHT.h>

#define DHTPIN 2
#define DHTTYPE DHT11

extern DHT dht;

void dht_begin();
float readHumidity();
float readTemperature();
void dht11Data();
#include <Arduino.h>
#include <DHT.h>

#define DHTPIN 1
#define DHTTYPE DHT11

extern DHT dht;

void dht_begin();
float readHumidity();
float readTemperature();
void dht11Date();
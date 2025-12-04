#include <Arduino.h>
#include <DHT.h>

#define DHTPIN 1
#define DHTTYPE DHT11

extern DHT dht;

void begin();
float readHumidity();
float readTemperature();
#include <Arduino.h>
#include <DHT.h>

#define DHTPIN0 2
#define DHTPIN1 4
#define DHTTYPE DHT11

extern DHT dhtFridge;
extern DHT dhtFreeze;

void dht_begin();
float readFreezeTemp();
float readFridgeTemp();
void dht11Data();
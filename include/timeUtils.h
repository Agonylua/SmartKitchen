#ifndef TIME_UTILS_H
#define TIME_UTILS_H

#include <Arduino.h>
#include <time.h>

extern const char *NTP_SERVER1;
extern const char *NTP_SERVER2;
extern const long GMT_OFFSET_SEC;
extern const int DAYLIGHT_OFFSET_SEC;

extern char timeStr[20];
extern char dateStr[40];

void printCurrentTime();
bool syncNTPTime();

#endif
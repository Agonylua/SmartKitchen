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

// 新增：记录开机时间
extern time_t bootTime;

void printCurrentTime();
bool syncNTPTime();

// 新增：获取设备绝对运行秒数
unsigned long getRunTimeSeconds();

#endif
#include <Arduino.h>
#include <time.h>
#include "timeUtils.h"

extern char *timeStr;
char *dateStr;

struct tm currentTime;

bool syncNTPTime()
{
    Serial.println("[NTP]正在同步时间...");
    configTime(GMT_OFFSET_SEC, DAYLIGHT_OFFSET_SEC, NTP_SERVER1, NTP_SERVER2);

    int retry = 0;
    while (!getLocalTime(&currentTime) && retry < 10)
    {
        delay(500);
        Serial.print(".");
        retry++;
    }

    if (retry >= 10)
    {
        Serial.println("[NTP]时间同步失败！");
        return false;
    }
    else
    {
        Serial.println("[NTP]时间同步成功！");
        return true;
    }
}

// 打印当前时间（格式化输出）
void printCurrentTime()
{
    if (getLocalTime(&currentTime))
    { // 再次获取最新时间（可选，也可直接用currentTime）

        // 格式化时间：时:分:秒
        strftime(timeStr, sizeof(timeStr), "%H:%M:%S", &currentTime);
        // 格式化日期：年-月-日 星期
        strftime(dateStr, sizeof(dateStr), "%Y-%m-%d %A", &currentTime);

        Serial.print("当前时间：");
        Serial.print(dateStr);
        Serial.print(" ");
        Serial.println(timeStr);

        // int year = currentTime.tm_year + 1900; // tm_year 是从1900年开始的偏移量
        // int month = currentTime.tm_mon + 1;    // tm_mon 是0-11，需+1
        // int day = currentTime.tm_mday;         // 日（1-31）
        // int hour = currentTime.tm_hour;        // 时（0-23）
        // int minute = currentTime.tm_min;       // 分（0-59）
        // int second = currentTime.tm_sec;       // 秒（0-59）
        // int weekday = currentTime.tm_wday;     // 星期（0=周日，1=周一，...，6=周六）

        // Serial.printf("单独字段：%d年%d月%d日 %d时%d分%d秒 星期%d\n",
        //               year, month, day, hour, minute, second, weekday);
    }
    else
    {
        Serial.println("获取当前时间失败！");
    }
}

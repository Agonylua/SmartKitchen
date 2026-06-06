#include <Arduino.h>
#include <time.h>
#include "timeUtils.h"

// 定义 NTP 常量
const char *NTP_SERVER1 = "pool.ntp.org";
const char *NTP_SERVER2 = "cn.ntp.org.cn";
const long GMT_OFFSET_SEC = 8 * 3600;
const int DAYLIGHT_OFFSET_SEC = 0;

char timeStr[20];
char dateStr[40];

struct tm currentTime;
time_t bootTime = 0; // 新增：记录开机基准时间

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
        // 记录首次成功同步的时间
        if (bootTime == 0)
        {
            bootTime = time(nullptr);
        }
        return true;
    }
}

// 获取设备绝对运行秒数
unsigned long getRunTimeSeconds()
{
    if (bootTime == 0)
    {
        // 如果断网导致 NTP 尚未同步，降级使用 millis() (除以1000转为秒)
        return millis() / 1000;
    }
    return time(nullptr) - bootTime;
}

// 打印当前时间（格式化输出）
void printCurrentTime()
{
    if (getLocalTime(&currentTime))
    {
        strftime(timeStr, sizeof(timeStr), "%H:%M:%S", &currentTime);
        strftime(dateStr, sizeof(dateStr), "%Y/%m/%d %A", &currentTime);

        Serial.print("当前时间：");
        Serial.print(dateStr);
        Serial.print(" ");
        Serial.println(timeStr);
    }
    else
    {
        Serial.println("获取当前时间失败！");
    }
}
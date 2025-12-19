#include <Arduino.h>
#include <time.h>

const char *NTP_SERVER1 = "pool.ntp.org";
const char *NTP_SERVER2 = "cn.ntp.org.cn";
const long GMT_OFFSET_SEC = 8 * 3600;
const int DAYLIGHT_OFFSET_SEC = 0;

extern char *timeStr;
#include <Arduino.h>
#include <time.h>

static const char *NTP_SERVER1 = "pool.ntp.org";
static const char *NTP_SERVER2 = "cn.ntp.org.cn";
static const long GMT_OFFSET_SEC = 8 * 3600;
static const int DAYLIGHT_OFFSET_SEC = 0;

extern char timeStr[20];
extern char dateStr[40];

void printCurrentTime();
bool syncNTPTime();
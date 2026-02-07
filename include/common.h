#ifndef COMMON_H_
#define COMMON_H_

#include <Preferences.h>

// 设备运行状态枚举
enum class DeviceStatus
{
    UNBOUND, // 未绑定 (显示二维码)
    IDLE,    // 待机
    RUNNING, // 运行中
    OFFLINE, // 离线
    RESET    // 重置
};

// 全局变量声明 (在 main.cpp 中定义)
extern DeviceStatus currentStatus;
extern const char *DEV_SN;
extern String mode;
extern Preferences preferences;

#endif
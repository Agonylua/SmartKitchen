#ifndef COMMON_H_
#define COMMON_H_

#include <Preferences.h>

// 设备运行状态枚举
enum class DeviceStatus
{
    UNBOUND, // 未知
    ONLINE,  // 运行中
    OFFLINE, // 离线
};
enum class DeviceMode
{
    STANDARD,      // 标准模式
    FAST_COOL,     // 快速制冷
    ENERGY_SAVING, // 节能模式
    HOLIDAY,       // 假日模式
};

// 工具函数：设备模式与字符串之间的转换
String deviceModeToString(DeviceMode mode);
DeviceMode stringToDeviceMode(const String &modeStr);

// 全局变量声明 (在 main.cpp 中定义)
extern DeviceStatus currentStatus;
extern DeviceMode currentMode; // 全局设备模式变量
extern const char *DEV_SN;
extern Preferences preferences;
extern String homeId;

#endif
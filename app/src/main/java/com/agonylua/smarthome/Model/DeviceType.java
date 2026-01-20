package com.agonylua.smarthome.model;

import com.agonylua.smarthome.R;

public enum DeviceType {
    REFRIGERATOR(R.drawable.ic_device_refrigerator_online, R.drawable.ic_device_refrigerator_offline),
    RICE_COOKER(R.drawable.ic_device_rice_cooker_online, R.drawable.ic_device_rice_cooker_offline),
    DISHWASHER(R.drawable.ic_device_dishwasher_online, R.drawable.ic_device_dishwasher_offline),
    STERILIZER(R.drawable.ic_device_sterilizer_online, R.drawable.ic_device_sterilizer_offline),
    MICROWAVE(R.drawable.ic_device_microwave_online, R.drawable.ic_device_microwave_offline);
    private final int online;
    private final int offline;

    DeviceType(int online, int offline) {
        this.online = online;
        this.offline = offline;
    }

    public static DeviceType fromName(String name) {
        for (DeviceType type : DeviceType.values()) {
            if (type.name().equalsIgnoreCase(name)) {
                return type;
            }
        }
        return null;
    }

    public int getOnline() {
        return online;
    }

    public int getOffline() {
        return offline;
    }
}

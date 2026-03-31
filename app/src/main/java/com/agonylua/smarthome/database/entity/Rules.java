package com.agonylua.smarthome.database.entity;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Objects;

@Entity(tableName = "automation_rules")
public class Rules implements Parcelable {
    public static final Creator<Rules> CREATOR = new Creator<Rules>() {
        @Override
        public Rules createFromParcel(Parcel in) {
            return new Rules(in);
        }

        @Override
        public Rules[] newArray(int size) {
            return new Rules[size];
        }
    };
    @PrimaryKey(autoGenerate = false)
    @NonNull
    @ColumnInfo(name = "rule_id")
    private String ruleId = "-1";
    @ColumnInfo(name = "user_id")
    @NonNull
    private String userId = "";
    @ColumnInfo(name = "rule_name")
    @NonNull
    private String ruleName = "";
    @ColumnInfo(name = "is_enable")
    private Boolean isEnable = true;
    @ColumnInfo(name = "condition_type")
    private String conditionType; // 触发类型 (如 device_property, schedule)
    @ColumnInfo(name = "condition_device_sn")
    private String conditionDeviceSn; // 触发设备SN (如 esp32_01)
    @ColumnInfo(name = "condition_property")
    private String conditionProperty; // 监听属性 (如 temperature, status)
    @ColumnInfo(name = "condition_operator")
    private String conditionOperator; // 运算符: >, <, ==
    @ColumnInfo(name = "condition_value")
    private String conditionValue;    // 阈值 (如 30.0, OFFLINE)
    @ColumnInfo(name = "action_device_sn")
    private String actionDeviceSn;    // 目标设备SN
    @ColumnInfo(name = "action_command")
    private String actionCommand;     // 动作指令 (如 mode)
    @ColumnInfo(name = "action_payload")
    private String actionPayload;     // 指令参数 (如 auto)
    @ColumnInfo(name = "created_at")
    private Long createdAt = System.currentTimeMillis();

    public Rules() {
    }

    @Ignore
    public Rules(@NonNull String ruleId, @NonNull String userId, @NonNull String ruleName, Boolean isEnable,
                 String conditionType, String conditionProperty, String conditionDeviceSn, String conditionOperator, String conditionValue,
                 String actionDeviceSn, String actionCommand, String actionPayload, Long createdAt) {
        this.ruleId = ruleId;
        this.userId = userId;
        this.ruleName = ruleName;
        this.isEnable = isEnable;
        this.conditionType = conditionType;
        this.conditionDeviceSn = conditionDeviceSn;
        this.conditionProperty = conditionProperty;
        this.conditionOperator = conditionOperator;
        this.conditionValue = conditionValue;
        this.actionDeviceSn = actionDeviceSn;
        this.actionCommand = actionCommand;
        this.actionPayload = actionPayload;
        this.createdAt = createdAt != null ? createdAt : System.currentTimeMillis();
    }

    protected Rules(Parcel in) {
        ruleId = in.readString();
        userId = in.readString();
        ruleName = in.readString();
        isEnable = in.readByte() != 0;
        conditionType = in.readString();
        conditionDeviceSn = in.readString();
        conditionProperty = in.readString();
        conditionOperator = in.readString();
        conditionValue = in.readString();
        actionDeviceSn = in.readString();
        actionCommand = in.readString();
        actionPayload = in.readString();
        createdAt = in.readLong();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeString(ruleId);
        parcel.writeString(userId);
        parcel.writeString(ruleName);
        parcel.writeByte((byte) (isEnable ? 1 : 0));
        parcel.writeString(conditionType);
        parcel.writeString(conditionDeviceSn);
        parcel.writeString(conditionProperty);
        parcel.writeString(conditionOperator);
        parcel.writeString(conditionValue);
        parcel.writeString(actionDeviceSn);
        parcel.writeString(actionCommand);
        parcel.writeString(actionPayload);
        parcel.writeLong(createdAt != null ? createdAt : -1L);
    }

    // 【新增】重写 equals 和 hashCode 以支持 RecyclerView DiffUtil 丝滑刷新
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Rules rules = (Rules) o;
        return ruleId.equals(rules.ruleId) &&
                userId.equals(rules.userId) &&
                ruleName.equals(rules.ruleName) &&
                Objects.equals(isEnable, rules.isEnable) &&
                Objects.equals(conditionType, rules.conditionType) &&
                Objects.equals(conditionDeviceSn, rules.conditionDeviceSn) &&
                Objects.equals(conditionProperty, rules.conditionProperty) &&
                Objects.equals(conditionOperator, rules.conditionOperator) &&
                Objects.equals(conditionValue, rules.conditionValue) &&
                Objects.equals(actionDeviceSn, rules.actionDeviceSn) &&
                Objects.equals(actionCommand, rules.actionCommand) &&
                Objects.equals(actionPayload, rules.actionPayload);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ruleId, userId, ruleName, isEnable, conditionType,
                conditionDeviceSn, conditionProperty, conditionOperator,
                conditionValue, actionDeviceSn, actionCommand, actionPayload);
    }

    // Getters and Setters
    public Boolean getEnable() {
        return isEnable;
    }

    public void setEnable(Boolean enable) {
        isEnable = enable;
    }

    @NonNull
    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(@NonNull String ruleId) {
        this.ruleId = ruleId;
    }

    @NonNull
    public String getUserId() {
        return userId;
    }

    public void setUserId(@NonNull String userId) {
        this.userId = userId;
    }

    public String getConditionDeviceSn() {
        return conditionDeviceSn;
    }

    public void setConditionDeviceSn(String conditionDeviceSn) {
        this.conditionDeviceSn = conditionDeviceSn;
    }

    @NonNull
    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(@NonNull String ruleName) {
        this.ruleName = ruleName;
    }

    public String getConditionType() {
        return conditionType;
    }

    public void setConditionType(String conditionType) {
        this.conditionType = conditionType;
    }

    public String getConditionProperty() {
        return conditionProperty;
    }

    public void setConditionProperty(String conditionProperty) {
        this.conditionProperty = conditionProperty;
    }

    public String getConditionOperator() {
        return conditionOperator;
    }

    public void setConditionOperator(String conditionOperator) {
        this.conditionOperator = conditionOperator;
    }

    public String getConditionValue() {
        return conditionValue;
    }

    public void setConditionValue(String conditionValue) {
        this.conditionValue = conditionValue;
    }

    public String getActionDeviceSn() {
        return actionDeviceSn;
    }

    public void setActionDeviceSn(String actionDeviceSn) {
        this.actionDeviceSn = actionDeviceSn;
    }

    public String getActionCommand() {
        return actionCommand;
    }

    public void setActionCommand(String actionCommand) {
        this.actionCommand = actionCommand;
    }

    public String getActionPayload() {
        return actionPayload;
    }

    public void setActionPayload(String actionPayload) {
        this.actionPayload = actionPayload;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }
}

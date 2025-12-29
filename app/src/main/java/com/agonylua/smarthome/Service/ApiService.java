package com.agonylua.smarthome.Service;

import com.agonylua.smarthome.network.ControlRequest;
import com.agonylua.smarthome.network.DeviceRequest;
import com.agonylua.smarthome.network.LoginRequest;
import com.agonylua.smarthome.network.LoginResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {

    /**
     * 登录接口
     * 对应后端: @PostMapping("/api/auth/login")
     * 不需要 Token
     */
    @POST("api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    /**
     * 获取厨房设备的当前状态 (例如：烤箱温度、开关状态)
     * 对应后端: @GetMapping("/api/device/status")
     * 需要 Token！
     *
     * @param token    用户 Token
     * @param deviceSn 设备SN
     */
    @GET("api/device/status")
    Call<DeviceRequest> getDeviceStatus(
            @Header("Authorization") String token,
            @Query("deviceSn") String deviceSn
    );

    /**
     * 控制设备 (例如：打开开关、设置温度)
     * 对应后端: @PostMapping("/api/device/control")
     * 需要 Token！
     */
    @POST("api/device/control")
    Call<Void> controlDevice(
            @Header("Authorization") String token,
            @Body ControlRequest request
    );
}

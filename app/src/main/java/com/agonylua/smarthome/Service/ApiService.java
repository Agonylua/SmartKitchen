package com.agonylua.smarthome.Service;

import com.agonylua.smarthome.DTO.UserDTO;
import com.agonylua.smarthome.Model.Device;
import com.agonylua.smarthome.Model.DeviceResponse;
import com.agonylua.smarthome.Model.LoginRequest;
import com.agonylua.smarthome.Model.LoginResponse;
import com.agonylua.smarthome.network.ControlRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiService {

    /**
     * 登录接口
     * 对应后端: @PostMapping("/login")
     * 不需要 Token
     */
    @POST("/user/login")
    Call<LoginResponse<UserDTO>> login(@Body LoginRequest request);

    /**
     * 获取厨房设备信息
     *
     * @param homeId 家庭 ID
     */
    @GET("device/list/{homeId}")
    Call<DeviceResponse<Device>> getDeviceList(@Path("homeId") String homeId);

    /**
     * 控制设备
     * @param token    用户 Token
     * @param request  控制请求体
     */
    @POST("/user/control")
    Call<Void> controlDevice(
            @Header("Authorization") String token,
            @Body ControlRequest request
    );

    /**
     * 验证 Token 接口
     *
     * @param token 用户 Token
     */
    @POST("/user/validateToken")
    Call<Void> validateToken(@Header("Authorization") String token);
}

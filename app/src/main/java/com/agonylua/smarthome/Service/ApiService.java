package com.agonylua.smarthome.service;

import com.agonylua.smarthome.database.entity.Device;
import com.agonylua.smarthome.dto.UserDTO;
import com.agonylua.smarthome.model.ApiResponse;
import com.agonylua.smarthome.model.DeviceResponse;
import com.agonylua.smarthome.model.LoginRequest;
import com.agonylua.smarthome.model.UserRequest;
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
    Call<ApiResponse<UserDTO>> login(@Body LoginRequest request);

    /**
     * 注册接口
     */
    @POST("/user/register")
    Call<ApiResponse<UserDTO>> register(@Body LoginRequest request);

    /**
     * 更新用户信息
     *
     * @param request 更新数据
     */
    @POST("user/update")
    Call<ApiResponse<Void>> updateUserInfo(@Body UserRequest request);
    /**
     * 获取厨房设备信息
     *
     * @param homeId 家庭 ID
     */
    @GET("device/list/{homeId}")
    Call<DeviceResponse<Device>> getDeviceList(@Path("homeId") String homeId);

    /**
     * 控制设备
     *
     * @param token   用户 Token
     * @param request 控制请求体
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

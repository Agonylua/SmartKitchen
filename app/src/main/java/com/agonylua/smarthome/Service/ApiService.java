package com.agonylua.smarthome.service;

import com.agonylua.smarthome.database.entity.Device;
import com.agonylua.smarthome.dto.UserDTO;
import com.agonylua.smarthome.model.UserRequest;
import com.agonylua.smarthome.network.ApiResponse;
import com.agonylua.smarthome.network.DeviceBindRequest;
import com.agonylua.smarthome.network.DeviceResponse;
import com.agonylua.smarthome.network.LoginRequest;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
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
     * 获取家庭设备信息
     *
     * @param homeId 家庭 ID
     */
    @GET("device/list/{homeId}")
    Call<DeviceResponse<Device>> getDeviceList(@Path("homeId") String homeId);

    /**
     * 更新设备信息
     *
     * @param deviceSn 设备序列号
     */
    @POST("device/update/{deviceSn}")
    Call<ApiResponse<Void>> updateDeviceInfo(@Path("deviceSn") String deviceSn, @Body Device device);

    /**
     * 验证 Token 接口
     *
     */
    @POST("/user/validateToken")
    Call<Void> validateToken();

    /**
     * 绑定设备接口
     *
     * @param request 绑定请求数据
     */
    @POST("/device/bind")
    Call<ApiResponse<Integer>> bindDevice(@Body DeviceBindRequest request);

    @POST("/device/control")
    Call<ApiResponse<Void>> controlDevice(@Body Map<String, String> payload);
}

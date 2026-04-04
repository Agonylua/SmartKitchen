package com.agonylua.smartKitchen.service;

import com.agonylua.smartKitchen.common.ApiResponse;
import com.agonylua.smartKitchen.common.DeviceRequest;
import com.agonylua.smartKitchen.common.LoginRequest;
import com.agonylua.smartKitchen.common.UserRequest;
import com.agonylua.smartKitchen.database.entity.Device;
import com.agonylua.smartKitchen.database.entity.Home;
import com.agonylua.smartKitchen.dto.AutomationRuleDTO;
import com.agonylua.smartKitchen.dto.DevicePowerDTO;
import com.agonylua.smartKitchen.dto.HomeDTO;
import com.agonylua.smartKitchen.dto.UserDTO;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    //------------------------------ 用户相关接口 ---------------------------------

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
     * 验证 Token 接口
     *
     */
    @POST("/user/validateToken")
    Call<Void> validateToken();


    /**
     * 获取家庭用户信息接口
     *
     */
    @GET("user/info")
    Call<ApiResponse<UserDTO>> getUserInfo();

    /**
     * 获取家庭用户信息接口
     *
     * @param homeId 家庭 ID 列表 (逗号分隔)
     */
    @GET("user/list")
    Call<ApiResponse<List<UserDTO>>> getUserListInfo(@Query("homeId") String homeId);

    /**
     * 上传用户头像接口
     *
     * @param file   头像文件
     * @param userId 用户 ID
     */
    @Multipart
    @POST("/user/updateAvatar")
    Call<ApiResponse<String>> updateAvatar(@Part MultipartBody.Part file, @Part("userId") String userId);

    @POST("user/updateNickname")
    Call<ApiResponse<String>> updateNickname(@Query("userId") String userId, @Query("nickName") String newNickName);

    @POST("user/resetPassword")
    Call<ApiResponse<String>> resetPassword(@Query("userId") String userId, @Query("oldPassword") String oldPassword, @Query("newPassword") String newPassword);

    @POST("/user/exitHome")
    Call<ApiResponse<UserDTO>> exitHome(@Query("homeId") String homeId);
    //------------------------------ 设备相关接口 ---------------------------------

    /**
     * 获取家庭设备信息
     *
     * @param homeId 家庭 ID
     */
    @GET("device/list/{homeId}")
    Call<ApiResponse<List<Device>>> getDeviceList(@Path("homeId") String homeId);

    /**
     * 更新设备信息
     *
     * @param deviceSn 设备序列号
     */
    @POST("device/update/{deviceSn}")
    Call<ApiResponse<Void>> updateDeviceInfo(@Path("deviceSn") String deviceSn, @Body Device device);

    /**
     * 绑定设备接口
     *
     * @param request 绑定请求数据
     */
    @POST("/device/bind")
    Call<ApiResponse<Integer>> bindDevice(@Body DeviceRequest request);

    /**
     * 删除/解绑设备接口
     *
     * @param request 解绑请求数据
     */
    @POST("/device/unBind")
    Call<ApiResponse<Boolean>> unBindDevice(@Body DeviceRequest request);

    /**
     * 控制设备接口
     *
     * @param payload 控制命令数据 (例如: {"deviceSn": "12345", "power": "on"})
     */
    @POST("/device/control")
    Call<ApiResponse<Void>> controlDevice(@Body Map<String, String> payload);

    /**
     * 获取设备功率接口
     *
     * @param homeId 家庭 ID
     */
    @GET("/device/power")
    Call<ApiResponse<List<DevicePowerDTO>>> getDevicesPower(@Query("homeId") String homeId);

    @POST("/device/updateStatus")
    Call<ApiResponse<String>> updateDeviceStatus(@Query("deviceSn") String deviceSn);

    //------------------------------ 家庭相关接口 ---------------------------------

    /**
     * 获取家庭信息接口
     *
     * @param homeId 家庭 ID
     */
    @GET("home/info/{homeId}")
    Call<ApiResponse<Home>> getHomeInfo(@Path("homeId") String homeId);

    @POST("/home/removeMember")
    Call<ApiResponse<HomeDTO>> removeMember(@Query("homeId") String homeId, @Query("userId") String userId);

    @POST("/home/joinHome")
    Call<ApiResponse<String>> joinHome(@Query("homeId") String homeId);

    @POST("/home/joinHomeApproval")
    Call<ApiResponse<String>> joinHomeApproval(@Query("result") Boolean result, @Query("ownerId") String ownerId, @Query("memberId") String memberId);

    //------------------------------ 自动化规则相关接口 ---------------------------------

    /**
     * 创建自动化规则接口
     *
     * @param ruleData 规则数据
     */
    @POST("/rules/create")
    Call<ApiResponse<Boolean>> createRule(@Body AutomationRuleDTO ruleData);

    /**
     * 获取自动化规则列表接口
     *
     * @param userId 用户 ID
     */
    @GET("/rules/list")
    Call<ApiResponse<List<AutomationRuleDTO>>> getRules(@Query("userId") String userId);

    /**
     * 删除自动化规则接口
     *
     * @param ruleId 规则 ID
     */
    @POST("/rules/delete")
    Call<ApiResponse<Boolean>> deleteRule(@Query("ruleId") String ruleId);

    /**
     * 执行自动化场景接口
     *
     * @param ruleMode 场景模式 (例如: "morning", "evening", "away")
     */
    @POST("/rules/scene")
    Call<ApiResponse<Boolean>> executeScene(@Query("ruleMode") String ruleMode);


    @GET("/global/sync")
    Call<ApiResponse<Boolean>> globalDataSync();
}

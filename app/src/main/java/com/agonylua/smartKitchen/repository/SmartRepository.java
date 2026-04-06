package com.agonylua.smartKitchen.repository;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.agonylua.smartKitchen.common.ApiResponse;
import com.agonylua.smartKitchen.database.dao.DeviceDao;
import com.agonylua.smartKitchen.database.dao.RulesDao;
import com.agonylua.smartKitchen.database.entity.Device;
import com.agonylua.smartKitchen.database.entity.Rules;
import com.agonylua.smartKitchen.dto.AutomationRuleDTO;
import com.agonylua.smartKitchen.network.RetrofitClient;
import com.agonylua.smartKitchen.utils.RuleMapper;
import com.agonylua.smartKitchen.utils.ThreadPoolUtils;
import com.agonylua.smartKitchen.utils.UserManager;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Singleton
public class SmartRepository {

    private static final String TAG = "SmartRepository";
    private RetrofitClient retrofit;
    private DeviceDao deviceDao;
    private RulesDao rulesDao;
    private UserManager userManager;

    @Inject
    public SmartRepository(RetrofitClient retrofit, DeviceDao deviceDao, RulesDao rulesDao, UserManager userManager) {
        this.retrofit = retrofit;
        this.deviceDao = deviceDao;
        this.rulesDao = rulesDao;
        this.userManager = userManager;
    }

    public UserManager getUserManager() {
        return userManager;
    }

    public List<Device> getDeviceList() {
        return deviceDao.getDevicesByHomeId(userManager.getHomeId());
    }

    public LiveData<Device> getDeviceByDeviceName(String deviceName) {
        Log.d(TAG, "getDeviceByDeviceName: " + deviceName);
        return deviceDao.getDeviceByDeviceName(deviceName);
    }

    public LiveData<List<Rules>> getRulesList() {
        return rulesDao.getRulesListByUserId(userManager.getUserId());
    }

    public void createRule(AutomationRuleDTO rule, SmartCallback callback) {
        retrofit.getApi().createRule(rule).enqueue(new Callback<ApiResponse<Boolean>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Boolean>> call, @NonNull Response<ApiResponse<Boolean>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().getCode() == 200 && response.body().getData()) {
                        callback.onSuccess();
                    } else {
                        Log.d(TAG, "onResponse: 规则保存失败");
                        callback.onError(response.body().getMessage());
                    }
                } else {
                    callback.onError("规则保存失败，服务器返回错误");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Boolean>> call, @NonNull Throwable t) {
                Log.e(TAG, "Retrofit 内部解析或网络错误", t);
                callback.onError("网络或数据解析错误");
            }
        });
    }

    public void getRules(SmartCallback callback) {
        retrofit.getApi().getRules(userManager.getUserId()).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<AutomationRuleDTO>>> call, @NonNull Response<ApiResponse<List<AutomationRuleDTO>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<AutomationRuleDTO>> apiResponse = response.body();
                    if (apiResponse.getCode() == 200) {
                        // 将 DTO 转换为 Entity 并保存到本地数据库
                        List<AutomationRuleDTO> ruleDTOs = apiResponse.getData();
                        List<Rules> rules = RuleMapper.toRulesList(ruleDTOs, userManager.getUserId());
                        new Thread(() -> {
                            rulesDao.clearAll();
                            rulesDao.insertAll(rules);
                        }).start();
                        callback.onSuccess();
                    } else {
                        callback.onError("规则获取失败，错误代码: " + apiResponse.getCode());
                    }
                } else {
                    callback.onError("规则获取失败，服务器返回错误");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<AutomationRuleDTO>>> call, @NonNull Throwable t) {
                callback.onError("网络连接错误: " + t.getMessage());
            }
        });
    }

    public void deleteRule(String ruleId, SmartCallback callback) {
        // 【修复】乐观 UI 更新策略：网络请求前先删除本地数据库对应项，使得 UI 立刻响应
        ThreadPoolUtils.getInstance().execute(() -> {
            rulesDao.deleteByRuleId(ruleId);
        });

        retrofit.getApi().deleteRule(ruleId).enqueue(new Callback<ApiResponse<Boolean>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Boolean>> call, @NonNull Response<ApiResponse<Boolean>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getCode() == 200 && response.body().getData()) {
                    // 删除成功后静默拉取最新列表，保证多端数据一致
                    getRules(new SmartCallback() {
                        @Override
                        public void onSuccess() {
                            callback.onSuccess();
                        }

                        @Override
                        public void onError(String errorMessage) {
                            callback.onError(errorMessage);
                        }
                    });
                } else {
                    // 如果服务器删除失败，重新拉取列表以恢复刚才被乐观删除的本地数据
                    getRules(new SmartCallback() {
                        @Override
                        public void onSuccess() {
                        }

                        @Override
                        public void onError(String error) {
                        }
                    });
                    callback.onError("规则删除失败，服务器返回错误");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Boolean>> call, @NonNull Throwable t) {
                // 网络异常，回退本地状态
                getRules(new SmartCallback() {
                    @Override
                    public void onSuccess() {
                    }

                    @Override
                    public void onError(String error) {
                    }
                });
                callback.onError("网络连接错误: " + t.getMessage());
            }
        });
    }

    public void executeScene(String ruleMode, SmartCallback callback) {
        if (ruleMode == null || ruleMode.isEmpty()) {
            return;
        }
        retrofit.getApi().executeScene(ruleMode).enqueue(new Callback<ApiResponse<Boolean>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Boolean>> call, @NonNull Response<ApiResponse<Boolean>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getCode() == 200 && response.body().getData()) {
                    callback.onSuccess();
                } else {
                    callback.onError("场景执行失败，服务器返回错误");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Boolean>> call, @NonNull Throwable t) {
                callback.onError("网络连接错误: " + t.getMessage());
            }
        });
    }

    public interface SmartCallback {
        void onSuccess();

        void onError(String errorMessage);
    }

}

package com.agonylua.smarthome.repository;

public class GlobalRepository {
    private static GlobalRepository instance;

    // 使用 MutableLiveData 使其具有响应式特性
    private boolean networkState;

    private GlobalRepository() {
        networkState = false;
    }

    public static synchronized GlobalRepository getInstance() {
        synchronized (GlobalRepository.class) {
            if (instance == null) {
                instance = new GlobalRepository();
            }
        }
        return instance;
    }

    public boolean getNetworkState() {
        return networkState;
    }

    public void updateTheme(Boolean newState) {
        this.networkState = newState;
    }
}
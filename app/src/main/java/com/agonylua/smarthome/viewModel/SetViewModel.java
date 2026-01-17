package com.agonylua.smarthome.viewModel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;

import com.agonylua.smarthome.utils.UserManager;

import org.jspecify.annotations.NonNull;

public class SetViewModel extends AndroidViewModel {

    public SetViewModel(@NonNull Application application) {
        super(application);
    }

    public void logout() {
        UserManager userManager = UserManager.getInstance(getApplication());
        userManager.clear();
    }
}

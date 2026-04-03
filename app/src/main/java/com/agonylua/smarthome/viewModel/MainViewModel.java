package com.agonylua.smarthome.viewModel;

import androidx.lifecycle.ViewModel;

import com.agonylua.smarthome.repository.MainRepository;

public class MainViewModel extends ViewModel {

    private MainRepository repository;

    public MainViewModel() {

    }

    public void init(MainRepository repository) {
        this.repository = repository;
    }

    public void joinHomeApproval(Boolean result, String memberId) {
        repository.joinHomeApproval(result, memberId, new MainRepository.joinCallback() {
            @Override
            public void onSuccess(String refresh) {
            }

            @Override
            public void onError(String message) {
            }
        });
    }
}

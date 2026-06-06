package com.agonylua.smartKitchen.viewModel;

import androidx.lifecycle.ViewModel;

import com.agonylua.smartKitchen.repository.MainRepository;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class MainViewModel extends ViewModel {

    private MainRepository repository;

    @Inject
    public MainViewModel(MainRepository repository) {
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

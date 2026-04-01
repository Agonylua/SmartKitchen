package com.agonylua.smarthome.fragment;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;

import com.agonylua.smarthome.R;
import com.agonylua.smarthome.databinding.FragmentSplashBinding;
import com.agonylua.smarthome.repository.LoginRepository;
import com.agonylua.smarthome.viewModel.LoginViewModel;

public class SplashFragment extends Fragment {
    private static final String TAG = "SplashFragment";
    private FragmentSplashBinding binding;
    private LoginViewModel viewModel;
    private LoginRepository repository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSplashBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);
        repository = new LoginRepository(requireActivity().getApplication());

        playEntryAnimation();
        viewModel.init(repository);
        viewModel.tokenValidate();
        observeViewModel();
        binding.setViewModel(viewModel);
        binding.setLifecycleOwner(getViewLifecycleOwner());
    }

    private void observeViewModel() {
        if (viewModel.isNetwork()) {
            viewModel.getLoginResult().observe(getViewLifecycleOwner(), valid -> {
                if (valid != null && isAdded() && getView() != null) {
                    if (valid) {
                        Log.d(TAG, "observeViewModel: Token valid, navigating to main");
                        NavOptions navOptions = new NavOptions.Builder()
                                .setPopUpTo(R.id.splashFragment, true) // 清空栈
                                .build();

                        Navigation.findNavController(getView())
                                .navigate(R.id.mainFragment, null, navOptions);
                    } else {
                        Log.d(TAG, "observeViewModel: " + "Token invalid or not exist, navigating to login");
                        Navigation.findNavController(getView()).navigate(R.id.loginFragment);
                    }
                }
            });

        } else {
            if (getView() != null) {
                Navigation.findNavController(getView()).navigate(R.id.loginFragment);
            }
        }
    }

    private void playEntryAnimation() {
        binding.llLogoContainer.setAlpha(0f);
        binding.llLogoContainer.setTranslationY(80f);
        binding.pbLoading.setAlpha(0f);

        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(binding.llLogoContainer, "alpha", 0f, 1f);
        ObjectAnimator translationYAnimator = ObjectAnimator.ofFloat(binding.llLogoContainer, "translationY", 80f, 0f);
        ObjectAnimator loadingAlphaAnimator = ObjectAnimator.ofFloat(binding.pbLoading, "alpha", 0f, 1f);

        loadingAlphaAnimator.setStartDelay(600);
        loadingAlphaAnimator.setDuration(500);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(alphaAnimator, translationYAnimator, loadingAlphaAnimator);
        animatorSet.setDuration(1200);
        animatorSet.setInterpolator(new DecelerateInterpolator(1.5f));
        animatorSet.start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
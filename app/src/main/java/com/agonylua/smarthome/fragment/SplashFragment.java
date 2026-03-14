package com.agonylua.smarthome.fragment;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.agonylua.smarthome.R;
import com.agonylua.smarthome.databinding.FragmentSplashBinding;
import com.agonylua.smarthome.utils.ThreadPoolUtils;
import com.agonylua.smarthome.utils.UserManager;
import com.agonylua.smarthome.viewModel.SplashViewModel;

public class SplashFragment extends Fragment {

    private FragmentSplashBinding binding;
    private SplashViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSplashBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new SplashViewModel(requireActivity().getApplication());

        playEntryAnimation();
        checkLoginStateAndNavigate();
        observeViewModel();

        binding.setViewModel(viewModel);
        binding.setLifecycleOwner(getViewLifecycleOwner());
    }

    private void observeViewModel() {
        viewModel.getTokenValid().observe(getViewLifecycleOwner(), valid -> {
            if (valid != null && isAdded() && getView() != null) {
                if (valid) {
                    Navigation.findNavController(getView()).navigate(R.id.mainFragment);
                } else {
                    Navigation.findNavController(getView()).navigate(R.id.loginFragment);
                }
            }
        });
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

    private void checkLoginStateAndNavigate() {
        ThreadPoolUtils.getInstance().executeDelay(() -> {
            if (!isAdded() && getView() == null) return;

            if (viewModel.validateNetwork()) {
                if (checkIsUserLoggedIn()) {
                    viewModel.LoginCheck();
                } else {
                    requireActivity().runOnUiThread(() ->
                            Navigation.findNavController(getView()).navigate(R.id.loginFragment)
                    );
                }
            } else {
                requireActivity().runOnUiThread(() -> {
                    if (checkIsUserLoggedIn()) {
                        Navigation.findNavController(getView()).navigate(R.id.mainFragment);
                    } else {
                        Navigation.findNavController(getView()).navigate(R.id.loginFragment);
                    }
                });
            }
        }, 2200);
    }

    private boolean checkIsUserLoggedIn() {
        String token = UserManager.getInstance(requireContext()).getToken();
        return token != null && !token.isEmpty();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
package com.agonylua.smartKitchen.fragment;

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
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.agonylua.smartKitchen.R;
import com.agonylua.smartKitchen.databinding.FragmentSplashBinding;
import com.agonylua.smartKitchen.repository.LoginRepository;
import com.agonylua.smartKitchen.viewModel.LoginViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
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
        playEntryAnimation();
        viewModel.tokenValidate();
        observeViewModel();
        binding.setViewModel(viewModel);
        binding.setLifecycleOwner(getViewLifecycleOwner());
    }

    private void observeViewModel() {
        NavController navController = Navigation.findNavController(requireView());
        if (viewModel.isNetwork()) {
            viewModel.getLoginResult().observe(getViewLifecycleOwner(), valid -> {
                if (valid != null && isAdded() && getView() != null) {
                    if (valid) {
                        Log.d(TAG, "observeViewModel: Token valid, navigating to main");
                        navController.navigate(R.id.action_splash_to_main);
                    } else {
                        Log.d(TAG, "observeViewModel: " + "Token invalid or not exist, navigating to login");
                        navController.navigate(R.id.action_splash_to_login);
                    }
                }
            });

        } else {
            if (getView() != null) {
                binding.pbLoading.setVisibility(View.GONE);
                binding.tvErrorIcon.setVisibility(View.VISIBLE);
                binding.tvErrorMessage.setVisibility(View.VISIBLE);
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
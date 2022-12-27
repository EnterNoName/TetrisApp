package com.example.tetrisapp.ui.fragment;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.tetrisapp.R;
import com.example.tetrisapp.databinding.FragmentProfileBinding;
import com.example.tetrisapp.ui.activity.MainActivity;
import com.example.tetrisapp.util.OnTouchListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileFragment extends Fragment {
    private static final String TAG = "ProfileFragment";
    private FragmentProfileBinding binding;

    private FirebaseAuth mAuth;
    private FirebaseUser mUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (mUser == null) {
            Navigation.findNavController(binding.getRoot()).navigate(R.id.action_profileFragment_to_signInFragment);
            return;
        }

        initOnClickListeners();
        updateUI();
    }

    private void updateUI() {
        binding.tvEmailAddress.setText(mUser.getEmail());
        binding.tvUsername.setText(mUser.getDisplayName());
        Glide.with(this).load(mUser.getPhotoUrl()).circleCrop().error(R.drawable.ic_round_account_circle_24).listener(new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                binding.ivProfileImage.setImageTintList(null);
                return false;
            }
        }).into(binding.ivProfileImage);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initOnClickListeners() {
        binding.btnSignOut.setOnTouchListener(new OnTouchListener((MainActivity) requireActivity()));
        binding.btnSignOut.setOnClickListener(v -> {
            mAuth.signOut();
            Navigation.findNavController(binding.getRoot()).navigate(R.id.action_profileFragment_to_signInFragment);
        });
    }
}

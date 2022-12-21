package com.example.tetrisapp.ui.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.tetrisapp.R;
import com.example.tetrisapp.databinding.FragmentSignupBinding;
import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.common.api.ApiException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;

public class SignUpFragment extends Fragment {
    private static final String TAG = "SignInFragment";
    private FragmentSignupBinding binding;

    private SignInClient oneTapClient;
    private BeginSignInRequest signInRequest;

    private FirebaseAuth mAuth;
    private final ActivityResultLauncher<IntentSenderRequest> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartIntentSenderForResult(), result -> {
        try {
            SignInCredential credential = oneTapClient.getSignInCredentialFromIntent(result.getData());
            String idToken = credential.getGoogleIdToken();
            if (idToken !=  null) {
                AuthCredential firebaseCredential = GoogleAuthProvider.getCredential(idToken, null);
                mAuth.signInWithCredential(firebaseCredential)
                        .addOnCompleteListener(requireActivity(), task -> {
                                    if (task.isSuccessful()) {
                                        Log.d(TAG, "signUpWithCredential:success");
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        if (user != null) {
                                            Navigation.findNavController(binding.getRoot()).navigate(R.id.action_signUpFragment_to_profileFragment);
                                        }
                                    } else {
                                        Log.w(TAG, "signUpWithCredential:failure", task.getException());
                                    }
                                }
                        );
                Log.d(TAG, "Got ID token.");
            }
        } catch (ApiException e) {
            Log.d(TAG, e.getLocalizedMessage());
        }
    });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSignupBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initClickListeners();
    }

    private void initClickListeners() {
        binding.btnSignUp.setOnClickListener(v -> {
            String email = binding.etEmail.getText().toString();
            String password = binding.etPassword.getText().toString();
            String passwordConfirm = binding.etPasswordConfirm.getText().toString();
            String username = binding.etUsername.getText().toString();

            if (password.equals(passwordConfirm)) {
                resetFields();
                createAccount(email, password, username);
            } else {
                binding.etPassword.setText("");
                binding.etPasswordConfirm.setText("");
                Toast.makeText(requireContext(), "Passwords don't match.",
                        Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnSignInGoogle.setOnClickListener(v -> oneTapClient.beginSignIn(signInRequest)
                .addOnCompleteListener(requireActivity(), result -> {
                    activityResultLauncher.launch(new IntentSenderRequest.Builder(result.getResult().getPendingIntent().getIntentSender()).build());
                })
                .addOnFailureListener(requireActivity(), e -> {
                    Log.d(TAG, e.getLocalizedMessage());
                }));

        binding.btnSwitchToSignIn.setOnClickListener(v -> {
            Navigation.findNavController(binding.getRoot()).navigate(R.id.action_signUpFragment_to_signInFragment);
        });
    }

    private void resetFields() {
        binding.etUsername.setText("");
        binding.etEmail.setText("");
        binding.etPassword.setText("");
        binding.etPasswordConfirm.setText("");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        oneTapClient = Identity.getSignInClient(requireActivity());
        signInRequest = BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        .setServerClientId(getString(R.string.default_web_client_id))
                        .setFilterByAuthorizedAccounts(false)
                        .build())
                .setAutoSelectEnabled(true)
                .build();
    }

    private void createAccount(String email, String password, String username) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "createUserWithEmail:success");
                        FirebaseUser user = mAuth.getCurrentUser();

                        if (user != null) {
                            user.updateProfile(
                                    new UserProfileChangeRequest.Builder()
                                            .setDisplayName(username)
                                            .build()
                            );
                            Navigation.findNavController(binding.getRoot()).navigate(R.id.action_signUpFragment_to_profileFragment);
                        }
                    } else {
                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                        Toast.makeText(requireContext(), "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

package com.example.tetrisapp.ui.fragment;

import android.Manifest;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.tetrisapp.BuildConfig;
import com.example.tetrisapp.R;
import com.example.tetrisapp.data.service.UpdateService;
import com.example.tetrisapp.databinding.MainMenuFragmentBinding;
import com.example.tetrisapp.model.Update;
import com.example.tetrisapp.ui.activity.MainActivity;
import com.example.tetrisapp.util.ConnectionHelper;
import com.example.tetrisapp.util.DownloadUtil;
import com.example.tetrisapp.util.PermissionHelper;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.io.EOFException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainMenuFragment extends Fragment {
    private MainMenuFragmentBinding binding;
    private boolean hasWriteStoragePermission = false;
    private boolean hasInstallPackagesPermission = false;
    private PermissionHelper permissionHelper;
    private ConnectionHelper connectionHelper;
    private Retrofit retrofit;

    UpdateService updateService;
    ActivityResultLauncher<Intent> activityResultLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                requestPermissions();
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = MainMenuFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        permissionHelper = new PermissionHelper(requireActivity(), this);
        connectionHelper = new ConnectionHelper(requireActivity());
        retrofit = new Retrofit.Builder()
                .baseUrl(getString(R.string.update_url))
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        updateService = retrofit.create(UpdateService.class);
        initClickListeners();
        checkUpdate();
    }

    private void checkUpdate() {
        connectionHelper.checkInternetConnection(
                () -> {
                    updateService.getUpdate().enqueue(new Callback<Update>() {
                        @Override
                        public void onResponse(Call<Update> call, Response<Update> response) {
                            if (response.isSuccessful()) {
                                assert response.body() != null;
                                if (BuildConfig.VERSION_NAME.compareTo(response.body().version) < 0) {
                                    showUpdateDialog(response);
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<Update> call, Throwable t) {
                            if (!(t instanceof EOFException)) {
                                t.printStackTrace();
                            }
                        }
                    });
                },
                () -> {
                    // TODO
                }
        );
    }

    private void showUpdateDialog(Response<Update> response) {
        new MaterialAlertDialogBuilder(requireContext(), R.style.LightDialogTheme)
                .setTitle(response.body().title)
                .setMessage(response.body().description)
                .setNegativeButton(getString(R.string.disagree), (dialog, which) -> {

                })
                .setPositiveButton(getString(R.string.agree), (dialog, which) -> {
                    requestPermissions();
                    if (hasWriteStoragePermission && hasInstallPackagesPermission) {
                        String URL = response.body().url;
                        Executors.newSingleThreadScheduledExecutor().schedule(
                                new DownloadUtil(requireActivity(),
                                        URL,
                                        getString(R.string.app_name) + ".apk"),
                                0,
                                TimeUnit.MILLISECONDS
                        );
                    }
                })
                .show();
    }

    private void initClickListeners() {
        binding.btnSingleplayer.setOnClickListener(v -> {
            Navigation.findNavController(binding.getRoot()).navigate(R.id.action_mainMenuFragment_to_gameFragment);
            ((MainActivity) requireActivity()).getClickMP().start();
            ((MainActivity) requireActivity()).getGameStartBtnMP().start();
        });
        binding.btnExit.setOnClickListener(v -> {
            requireActivity().finishAndRemoveTask();
            ((MainActivity) requireActivity()).getClickMP().start();
        });
        binding.btnLeaderboard.setOnClickListener(v -> {
            ((MainActivity) requireActivity()).getClickMP().start();
        });
        binding.btnSettings.setOnClickListener(v -> {
            ((MainActivity) requireActivity()).getClickMP().start();
        });
        binding.btnMultiplayer.setOnClickListener(v -> {
            ((MainActivity) requireActivity()).getClickMP().start();
        });
        binding.btnSocials.setOnClickListener(v -> {
            ((MainActivity) requireActivity()).getClickMP().start();
        });
        binding.btnSignIn.setOnClickListener(v -> {
            ((MainActivity) requireActivity()).getClickMP().start();
        });
    }

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            permissionHelper.checkPermission(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    () -> {
                        hasWriteStoragePermission = true;
                    },
                    () -> {
                        permissionHelper.requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                () -> {
                                    hasWriteStoragePermission = true;
                                },
                                () -> {
                                    Snackbar.make(binding.getRoot(), R.string.update_feature_unavailable, Snackbar.LENGTH_LONG)
                                            .setAction(R.string.update_feature_retry, v -> {
                                                requestPermissions();
                                            }).show();
                                });
                    },
                    () -> {
                    }
            );
        } else {
            hasWriteStoragePermission = true;
        }

        permissionHelper.checkPermission(
                Manifest.permission.REQUEST_INSTALL_PACKAGES,
                () -> {
                    hasInstallPackagesPermission = true;
                },
                () -> {
                    permissionHelper.requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            () -> {
                                hasInstallPackagesPermission = true;
                            },
                            () -> {
                                Snackbar.make(binding.getRoot(), R.string.update_feature_unavailable, Snackbar.LENGTH_LONG)
                                        .setAction(R.string.update_feature_retry, v -> {
                                            requestPermissions();
                                        }).show();
                            });
                },
                () -> {
                    activityResultLauncher.launch(new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS));
                }
        );
    }
}

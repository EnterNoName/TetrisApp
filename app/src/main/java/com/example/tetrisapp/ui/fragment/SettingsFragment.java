package com.example.tetrisapp.ui.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.tetrisapp.R;
import com.example.tetrisapp.databinding.FragmentSettingsListBinding;
import com.example.tetrisapp.interfaces.RecyclerViewInterface;
import com.example.tetrisapp.model.game.configuration.PieceConfigurations;
import com.example.tetrisapp.ui.adapters.SettingsRecyclerViewAdapter;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;

public class SettingsFragment extends BottomSheetDialogFragment implements RecyclerViewInterface {
    public static final String TAG = "SettingsFragment";
    private FragmentSettingsListBinding binding;
    private final List<SettingsRecyclerViewAdapter.Setting> settingList = new ArrayList<>();
    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme);
        preferences = requireActivity().getPreferences(Context.MODE_PRIVATE);
        editor = preferences.edit();
        initializeSettings();
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void initializeSettings() {
        SettingsRecyclerViewAdapter.Setting pieceConfigurationSetting = new SettingsRecyclerViewAdapter.Setting(
                requireActivity().getDrawable(R.drawable.ic_launcher_foreground),
                "Piece Configuration",
                PieceConfigurations.values()[preferences.getInt(getString(R.string.setting_configuration_index), 0)].name(),
                () -> {
                    int configurationInd = preferences.getInt(getString(R.string.setting_configuration_index), 0);
                    int newConfigurationInd = configurationInd - 1;
                    if (newConfigurationInd < 0)  {
                        newConfigurationInd = PieceConfigurations.values().length - 1;
                    }
                    editor.putInt(getString(R.string.setting_configuration_index), newConfigurationInd);
                    editor.putString(getString(R.string.setting_configuration), PieceConfigurations.values()[newConfigurationInd].name());
                    editor.apply();
                },
                () -> {
                    int configurationInd = preferences.getInt(getString(R.string.setting_configuration_index), 0);
                    int newConfigurationInd = configurationInd + 1;
                    if (newConfigurationInd >= PieceConfigurations.values().length)  {
                        newConfigurationInd = 0;
                    }
                    editor.putInt(getString(R.string.setting_configuration_index), newConfigurationInd);
                    editor.putString(getString(R.string.setting_configuration), PieceConfigurations.values()[newConfigurationInd].name());
                    editor.apply();
                }
        );
        this.settingList.add(pieceConfigurationSetting);

        SettingsRecyclerViewAdapter.Setting autoUpdateSetting = new SettingsRecyclerViewAdapter.Setting(
                requireActivity().getDrawable(R.drawable.ic_round_cloud_download_24),
                getString(R.string.setting_auto_update_title),
                preferences.getBoolean(getString(R.string.setting_auto_update), true) ? "Enabled" : "Disabled",
                () -> {
                    editor.putBoolean(
                            getString(R.string.setting_auto_update),
                            !preferences.getBoolean(getString(R.string.setting_auto_update), true)
                    );
                    editor.apply();
                }
        );
        this.settingList.add(autoUpdateSetting);

        SettingsRecyclerViewAdapter.Setting controlSchemeSetting = new SettingsRecyclerViewAdapter.Setting(
                requireActivity().getDrawable(R.drawable.ic_round_videogame_controller_24),
                getString(R.string.setting_control_scheme_title),
                preferences.getBoolean(getString(R.string.setting_control_scheme), false) ? "Touch" : "Buttons",
                () -> {
                    editor.putBoolean(
                            getString(R.string.setting_control_scheme),
                            !preferences.getBoolean(getString(R.string.setting_control_scheme), false)
                    );
                    editor.apply();
                }
        );
        this.settingList.add(controlSchemeSetting);

        SettingsRecyclerViewAdapter.Setting countdownSetting = new SettingsRecyclerViewAdapter.Setting(
                requireActivity().getDrawable(R.drawable.ic_round_timer_24),
                getString(R.string.setting_countdown_title),
                preferences.getInt(getString(R.string.setting_countdown), 5) == 0 ?
                        "Disabled" :
                        preferences.getInt(getString(R.string.setting_countdown), 5) + " Sec",
                () -> {
                    int countdown = preferences.getInt(getString(R.string.setting_countdown), 5);
                    editor.putInt(getString(R.string.setting_countdown), countdown > 0 ? countdown - 1 : countdown);
                    editor.apply();
                },
                () -> {
                    int countdown = preferences.getInt(getString(R.string.setting_countdown), 5);
                    editor.putInt(getString(R.string.setting_countdown), countdown < 10 ? countdown + 1 : countdown);
                    editor.apply();
                }
        );
        this.settingList.add(countdownSetting);

        SettingsRecyclerViewAdapter.Setting musicSetting = new SettingsRecyclerViewAdapter.Setting(
                preferences.getInt(getString(R.string.setting_music_volume), 5) > 0 ?
                        requireActivity().getDrawable(R.drawable.ic_round_music_note_24) :
                        requireActivity().getDrawable(R.drawable.ic_round_music_off_24),
                getString(R.string.setting_music_volume_title),
                preferences.getInt(getString(R.string.setting_music_volume), 5) == 0 ?
                        "Off" :
                        preferences.getInt(getString(R.string.setting_music_volume), 5) + "",
                () -> {
                    int volume = preferences.getInt(getString(R.string.setting_music_volume), 5);
                    editor.putInt(getString(R.string.setting_music_volume), volume > 0 ? volume - 1 : volume);
                    editor.apply();
                },
                () -> {
                    int volume = preferences.getInt(getString(R.string.setting_music_volume), 5);
                    editor.putInt(getString(R.string.setting_music_volume), volume < 10 ? volume + 1 : volume);
                    editor.apply();
                }
        );
        this.settingList.add(musicSetting);

        SettingsRecyclerViewAdapter.Setting sfxSetting = new SettingsRecyclerViewAdapter.Setting(
                preferences.getInt(getString(R.string.setting_sfx_volume), 5) >= 5 ?
                        requireActivity().getDrawable(R.drawable.ic_round_volume_up_24) :
                        preferences.getInt(getString(R.string.setting_sfx_volume), 5) > 0 ?
                                requireActivity().getDrawable(R.drawable.ic_round_volume_down_24) :
                                requireActivity().getDrawable(R.drawable.ic_round_volume_off_24),
                getString(R.string.setting_sfx_volume_title),
                preferences.getInt(getString(R.string.setting_sfx_volume), 5) == 0 ?
                        "Off" :
                        preferences.getInt(getString(R.string.setting_sfx_volume), 5) + "",
                () -> {
                    int volume = preferences.getInt(getString(R.string.setting_sfx_volume), 5);
                    editor.putInt(getString(R.string.setting_sfx_volume), volume > 0 ? volume - 1 : volume);
                    editor.apply();
                },
                () -> {
                    int volume = preferences.getInt(getString(R.string.setting_sfx_volume), 5);
                    editor.putInt(getString(R.string.setting_sfx_volume), volume < 10 ? volume + 1 : volume);
                    editor.apply();
                }
        );
        this.settingList.add(sfxSetting);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsListBinding.inflate(inflater, container, false);

        binding.list.setAdapter(new SettingsRecyclerViewAdapter(this.settingList, this));

        return binding.getRoot();
    }

    @Override
    public void onItemClick(int position) {
        settingList.clear();
        initializeSettings();
        binding.list.getAdapter().notifyItemChanged(position);
    }
}

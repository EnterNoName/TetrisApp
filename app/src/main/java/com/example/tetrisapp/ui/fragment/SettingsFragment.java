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
import com.example.tetrisapp.ui.adapters.SettingsRecyclerViewAdapter;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;

public class SettingsFragment extends BottomSheetDialogFragment implements RecyclerViewInterface {
    public static final String TAG = "SettingsFragment";
    private FragmentSettingsListBinding binding;
    private List<SettingsRecyclerViewAdapter.Setting> settingList = new ArrayList<>();
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
//        SettingsRecyclerViewAdapter.Setting controlSchemeSetting = new SettingsRecyclerViewAdapter.Setting(
//                requireActivity().getDrawable(R.drawable.ic_round_videogame_controller_24),
//                "Control Scheme",
//                "Buttons"
//        );
//        this.settingList.add(controlSchemeSetting);
        SettingsRecyclerViewAdapter.Setting countdownSetting = new SettingsRecyclerViewAdapter.Setting(
                requireActivity().getDrawable(R.drawable.ic_round_timer_24),
                "Countdown",
                preferences.getInt(getString(R.string.setting_countdown), 5) == 0 ?
                        "Disabled" :
                        preferences.getInt(getString(R.string.setting_countdown), 5) + " Sec",
                () -> {
                    switch (preferences.getInt(getString(R.string.setting_countdown), 5)) {
                        case 5:
                            editor.putInt(getString(R.string.setting_countdown), 3);
                            break;
                        case 3:
                            editor.putInt(getString(R.string.setting_countdown), 0);
                            break;
                        default:
                            editor.putInt(getString(R.string.setting_countdown), 5);
                            break;
                    }
                    editor.apply();
                }
        );
        this.settingList.add(countdownSetting);
        SettingsRecyclerViewAdapter.Setting autoUpdateSetting = new SettingsRecyclerViewAdapter.Setting(
                requireActivity().getDrawable(R.drawable.ic_round_cloud_download_24),
                "Auto-update",
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
        SettingsRecyclerViewAdapter.Setting setting = settingList.get(position);
        setting.getCallback().call();
        settingList.clear();
        initializeSettings();
        binding.list.getAdapter().notifyItemChanged(position);
    }
}

package com.example.tetrisapp.ui.adapters;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tetrisapp.databinding.FragmentSettingsBinding;
import com.example.tetrisapp.databinding.FragmentSettingsCounterBinding;
import com.example.tetrisapp.interfaces.RecyclerViewInterface;

import java.util.List;

public class SettingsRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<Setting> mValues;
    private final RecyclerViewInterface recyclerViewInterface;

    public SettingsRecyclerViewAdapter(List<Setting> mValues, RecyclerViewInterface recyclerViewInterface) {
        this.mValues = mValues;
        this.recyclerViewInterface = recyclerViewInterface;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case 1: return new ViewHolderSettingCounter(FragmentSettingsCounterBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
            default: return new ViewHolderSetting(FragmentSettingsBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case 0:
                ViewHolderSetting viewHolder = (ViewHolderSetting) holder;
                viewHolder.setting = mValues.get(position);
                viewHolder.setViews();
                break;
            case 1:
                ViewHolderSettingCounter viewHolderCounter = (ViewHolderSettingCounter) holder;
                viewHolderCounter.setting = mValues.get(position);
                viewHolderCounter.setViews();
                break;
        }

    }

    @Override
    public int getItemViewType(int position) {
        return mValues.get(position).secondCallback == null ? 0 : 1;
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolderSettingCounter extends RecyclerView.ViewHolder {
        public final ImageView mSettingIcon;
        public final TextView mSettingName;
        public final ImageButton mBtnDecrease;
        public final ImageButton mBtnIncrease;
        public final TextView mSettingValue;
        public Setting setting;

        public ViewHolderSettingCounter(FragmentSettingsCounterBinding binding) {
            super(binding.getRoot());
            this.mSettingIcon = binding.ivSettingIcon;
            this.mSettingName = binding.tvSettingName;
            this.mSettingValue = binding.tvSettingValue;
            this.mBtnDecrease = binding.btnDecrease;
            this.mBtnIncrease = binding.btnIncrease;

            mBtnDecrease.setOnClickListener(v -> {
                if (recyclerViewInterface != null) {
                    int pos = getBindingAdapterPosition();

                    if (pos == RecyclerView.NO_POSITION) return;

                    setting.getFirstCallback().call();
                    recyclerViewInterface.onItemClick(pos);
                }
            });

            mBtnIncrease.setOnClickListener(v -> {
                if (recyclerViewInterface != null) {
                    int pos = getBindingAdapterPosition();

                    if (pos == RecyclerView.NO_POSITION) return;

                    setting.getSecondCallback().call();
                    recyclerViewInterface.onItemClick(pos);
                }
            });
        }

        public void setViews() {
            mSettingIcon.setImageDrawable(setting.getIcon());
            mSettingName.setText(setting.getName());
            mSettingValue.setText(setting.getValue());
        }
    }

    public class ViewHolderSetting extends RecyclerView.ViewHolder {
        public final ImageView mSettingIcon;
        public final TextView mSettingName;
        public final TextView mSettingValue;
        public Setting setting;

        public ViewHolderSetting(FragmentSettingsBinding binding) {
            super(binding.getRoot());
            this.mSettingIcon = binding.ivSettingIcon;
            this.mSettingName = binding.tvSettingName;
            this.mSettingValue = binding.tvSettingValue;

            mSettingValue.setOnClickListener(v -> {
                if (recyclerViewInterface != null) {
                    int pos = getBindingAdapterPosition();

                    if (pos == RecyclerView.NO_POSITION) return;

                    setting.getFirstCallback().call();
                    recyclerViewInterface.onItemClick(pos);
                }
            });
        }

        public void setViews() {
            mSettingIcon.setImageDrawable(setting.getIcon());
            mSettingName.setText(setting.getName());
            mSettingValue.setText(setting.getValue());
        }
    }

    public static class Setting {
        private final Callback firstCallback;
        private final Callback secondCallback;
        private final Drawable icon;
        private final String name;
        private final String value;

        public Setting(Drawable icon, String name, String value, Callback callback) {
            this.icon = icon;
            this.name = name;
            this.value = value;
            this.firstCallback = callback;
            this.secondCallback = null;
        }

        public Setting(Drawable icon, String name, String value, Callback callbackDecrease, Callback callbackIncrease) {
            this.icon = icon;
            this.name = name;
            this.value = value;
            this.firstCallback = callbackDecrease;
            this.secondCallback = callbackIncrease;
        }

        public Drawable getIcon() {
            return icon;
        }

        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }

        public Callback getFirstCallback() {
            return firstCallback;
        }

        public Callback getSecondCallback() {
            return secondCallback;
        }

        public interface Callback {
            void call();
        }
    }
}

package com.example.tetrisapp.ui.adapters;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tetrisapp.databinding.FragmentSettingsBinding;
import com.example.tetrisapp.interfaces.RecyclerViewInterface;

import java.util.List;

public class SettingsRecyclerViewAdapter extends RecyclerView.Adapter<SettingsRecyclerViewAdapter.ViewHolder> {

    private final List<Setting> mValues;
    private final RecyclerViewInterface recyclerViewInterface;

    public SettingsRecyclerViewAdapter(List<Setting> mValues, RecyclerViewInterface recyclerViewInterface) {
        this.mValues = mValues;
        this.recyclerViewInterface = recyclerViewInterface;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(FragmentSettingsBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.setting = mValues.get(position);
        holder.setViews();
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final ImageView mSettingIcon;
        public final TextView mSettingName;
        public final TextView mSettingValue;
        public Setting setting;

        public ViewHolder(FragmentSettingsBinding binding) {
            super(binding.getRoot());
            this.mSettingIcon = binding.ivSettingIcon;
            this.mSettingName = binding.tvSettingName;
            this.mSettingValue = binding.tvSettingValue;

            mSettingValue.setOnClickListener(v -> {
                if (recyclerViewInterface != null) {
                    int pos = getBindingAdapterPosition();

                    if (pos == RecyclerView.NO_POSITION) return;

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
        private final Callback callback;
        private final Drawable icon;
        private final String name;
        private final String value;

        public Setting(Drawable icon, String name, String value, Callback callback) {
            this.icon = icon;
            this.name = name;
            this.value = value;
            this.callback = callback;
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

        public Callback getCallback() {
            return callback;
        }

        public interface Callback {
            void call();
        }
    }
}

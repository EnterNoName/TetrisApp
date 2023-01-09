package com.example.tetrisapp.ui.adapters;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.tetrisapp.R;
import com.example.tetrisapp.databinding.FragmentUserBinding;
import com.example.tetrisapp.model.local.model.UserInfo;

import java.util.List;

public class UsersRecyclerViewAdapter extends RecyclerView.Adapter<UsersRecyclerViewAdapter.ViewHolder> {
    private final List<UserInfo> mValues;

    public UsersRecyclerViewAdapter(List<UserInfo> mValues) {
        this.mValues = mValues;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(FragmentUserBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.user = mValues.get(position);
        holder.setViews();
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView mUserName;
        public final ImageView mUserPhoto;
        public UserInfo user;

        public ViewHolder(FragmentUserBinding binding) {
            super(binding.getRoot());
            this.mUserName = binding.tvUsername;
            this.mUserPhoto = binding.ivProfileImage;
        }

        public void setViews() {
            mUserName.setText(user.getName());
            Glide.with(mUserPhoto).load(user.getPhotoUrl()).circleCrop().error(R.drawable.ic_round_account_circle_24).listener(new RequestListener<Drawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                    return false;
                }

                @Override
                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                    mUserPhoto.setImageTintList(null);
                    return false;
                }
            }).into(mUserPhoto);
        }

        @Override
        public String toString() {
            return super.toString() + "'" + user.toString() + "'";
        }
    }
}

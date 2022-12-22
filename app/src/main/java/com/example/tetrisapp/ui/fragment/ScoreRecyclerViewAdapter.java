package com.example.tetrisapp.ui.fragment;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tetrisapp.R;
import com.example.tetrisapp.databinding.FragmentScoresBinding;
import com.example.tetrisapp.model.local.entity.LeaderboardEntry;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ScoreRecyclerViewAdapter extends RecyclerView.Adapter<ScoreRecyclerViewAdapter.ViewHolder> {

    private final List<LeaderboardEntry> mValues;
    private final Context ctx;

    public ScoreRecyclerViewAdapter(Context context, List<LeaderboardEntry> items) {
        mValues = items;
        ctx = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(FragmentScoresBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.setViews();
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView mDateView;
        public final TextView mScoreView;
        public final TextView mLinesView;
        public final TextView mLevelView;
        public final TextView mItemNumber;
        public final CardView mCvItemNumber;
        public LeaderboardEntry mItem;

        public ViewHolder(FragmentScoresBinding binding) {
            super(binding.getRoot());
            mDateView = binding.tvDate;
            mScoreView = binding.score;
            mLinesView = binding.lines;
            mLevelView = binding.level;
            mItemNumber = binding.tvItemNumber;
            mCvItemNumber = binding.cvItemNumber;
        }

        public void setViews() {
            switch (getBindingAdapterPosition()) {
                case 0:
                    mCvItemNumber.setCardBackgroundColor(ctx.getColor(R.color.gold));
                    break;
                case 1:
                    mCvItemNumber.setCardBackgroundColor(ctx.getColor(R.color.silver));
                    break;
                case 2:
                    mCvItemNumber.setCardBackgroundColor(ctx.getColor(R.color.bronze));
                    break;
            }

            mItemNumber.setText(String.format(Locale.getDefault(), "%d", getBindingAdapterPosition() + 1));
            mScoreView.setText(String.format(Locale.getDefault(), "%d", mItem.score));
            mLevelView.setText(String.format(Locale.getDefault(), "%d", mItem.level));
            mLinesView.setText(String.format(Locale.getDefault(), "%d", mItem.lines));

            SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm\nMM.dd.yyyy", Locale.getDefault());
            mDateView.setText(dateFormat.format(mItem.date));
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mItem.toString() + "'";
        }
    }
}
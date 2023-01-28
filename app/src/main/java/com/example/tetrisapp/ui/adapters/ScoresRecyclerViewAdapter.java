package com.example.tetrisapp.ui.adapters;

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
import com.example.tetrisapp.model.remote.response.ScorePayload;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ScoresRecyclerViewAdapter extends RecyclerView.Adapter<ScoresRecyclerViewAdapter.ViewHolder> {

    public final List<Score> mValues;
    private final Context ctx;

    public ScoresRecyclerViewAdapter(Context context, List<Score> items) {
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
        public Score mItem;

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

            if (mItem.type == 0) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm\nMM.dd.yyyy", Locale.getDefault());
                mDateView.setText(dateFormat.format(mItem.date));
            } else {
                mDateView.setText(mItem.name);
            }
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mItem.toString() + "'";
        }
    }

    public static class Score {
        public int type = 0;
        public int score;
        public int level;
        public int lines;
        public Date date;
        public String name;
        public String userId;

        public Score(int score, int level, int lines, Date date) {
            this.score = score;
            this.level = level;
            this.lines = lines;
            this.date = date;
        }

        public Score(int score, int level, int lines, Date date, String name, String userId) {
            this.score = score;
            this.level = level;
            this.lines = lines;
            this.date = date;
            this.name = name;
            this.userId = userId;
            this.type = 1;
        }
    }
}
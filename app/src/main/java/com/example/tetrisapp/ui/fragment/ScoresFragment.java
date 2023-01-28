package com.example.tetrisapp.ui.fragment;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.tetrisapp.R;
import com.example.tetrisapp.data.local.dao.LeaderboardDao;
import com.example.tetrisapp.databinding.FragmentScoresListBinding;
import com.example.tetrisapp.model.local.entity.LeaderboardEntry;
import com.example.tetrisapp.ui.adapters.ScoresRecyclerViewAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;

@AndroidEntryPoint
public class ScoresFragment extends Fragment {
    private static final String TAG = "ScoresFragment";
    private FragmentScoresListBinding binding;

    @Inject
    LeaderboardDao leaderboardDao;

    private int width;
    private int height;
    private Drawable deleteIcon;

    private ItemTouchHelper swipeHelper;
    private List<LeaderboardEntry> leaderboardEntries = new ArrayList<>();

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        width = metrics.widthPixels;
        height = metrics.heightPixels;
        deleteIcon = requireActivity().getDrawable(R.drawable.ic_round_delete_24);
        deleteIcon.setTint(requireActivity().getColor(R.color.white));

        swipeHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.END) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int pos = viewHolder.getBindingAdapterPosition();
                leaderboardDao.delete(leaderboardEntries.get(pos))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe((integer) -> {
                            leaderboardEntries.remove(pos);
                            requireActivity().runOnUiThread(() -> {
                                ((ScoresRecyclerViewAdapter) binding.list.getAdapter()).mValues.remove(pos);
                                binding.list.getAdapter().notifyItemRemoved(pos);
                                initUI();
                            });
                        }, throwable -> Log.e("ScoresFragment", throwable.getLocalizedMessage()));
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                if (Math.abs(dX) < width / 3f) {
                    c.drawColor(requireActivity().getColor(R.color.grey_700));
                } else if (dX > width / 3f) {
                    c.drawColor(requireActivity().getColor(R.color.red_700));
                }

                int textMargin = (int) getResources().getDimension(R.dimen.text_margin);
                deleteIcon.setBounds(
                        textMargin,
                        viewHolder.itemView.getTop() + textMargin + convertDpToPixel(8),
                        textMargin + deleteIcon.getIntrinsicWidth(),
                        viewHolder.itemView.getTop() + deleteIcon.getIntrinsicHeight()
                                + textMargin + convertDpToPixel(8)
                );

                if (dX > 0) deleteIcon.draw(c);

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        });
    }

    @SuppressLint({"UseCompatLoadingForDrawables", "NotifyDataSetChanged"})
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentScoresListBinding.inflate(inflater, container, false);

        // RecyclerView swipe handler
        swipeHelper.attachToRecyclerView(binding.list);

        // RecyclerView divider
        DividerItemDecoration divider = new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL);
        divider.setDrawable(requireActivity().getDrawable(R.drawable.recyclerview_divider));
        binding.list.addItemDecoration(divider);
        binding.list.setAdapter(new ScoresRecyclerViewAdapter(requireContext(), new ArrayList<>()));

        // Load RecyclerView items
        leaderboardDao.getSorted().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(leaderboardEntries -> {
            this.leaderboardEntries = leaderboardEntries;
            // RecyclerView adapter
            binding.list.setAdapter(new ScoresRecyclerViewAdapter(
                    requireContext(),
                    this.leaderboardEntries
                            .stream()
                            .map(entry ->
                                    new ScoresRecyclerViewAdapter.Score(entry.score, entry.level, entry.lines, entry.date)
                            ).collect(Collectors.toList())
            ));
        }, throwable -> {
            if (!(throwable instanceof NullPointerException)) {
                Log.e("ScoresFragment", throwable.getLocalizedMessage());
            }
        });

        binding.btnBack.setOnClickListener(v -> Navigation.findNavController(binding.getRoot()).popBackStack());

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initUI();
    }

    private void initUI() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            binding.tvUsername.setText(user.getDisplayName());
            binding.tvEmailAddress.setText(user.getEmail());
            binding.tvStatistics.setText(String.format(Locale.getDefault(), "%s's Statistics", user.getDisplayName()));
            Glide.with(this)
                    .load(user.getPhotoUrl())
                    .circleCrop()
                    .error(R.drawable.ic_round_account_circle_24)
                    .placeholder(R.drawable.ic_round_account_circle_24)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            binding.ivProfileImage.setImageTintList(null);
                            return false;
                        }
                    })
                    .into(binding.ivProfileImage);
        } else {
            binding.userDataGroup.setVisibility(View.GONE);
            binding.tvStatistics.setText("Your Statistics");
        }

        leaderboardDao.getGamesCount().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(count -> binding.tvGamesCount.setText(String.format(Locale.getDefault(), "Games played:\n%d", count)));
        leaderboardDao.getBestScore().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(bestScore -> binding.tvBestScore.setText(String.format(Locale.getDefault(), "Best:\n%d", bestScore)));
        leaderboardDao.getAverageScore().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(avgScore -> binding.tvAverageScore.setText(String.format(Locale.getDefault(), "Average:\n%d", avgScore)));
        leaderboardDao.getBestLevel().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(bestLevel -> binding.tvBestLevel.setText(String.format(Locale.getDefault(), "Best:\n%d", bestLevel)));
        leaderboardDao.getAverageLevel().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(avgLevel -> binding.tvAverageLevel.setText(String.format(Locale.getDefault(), "Average:\n%d", avgLevel)));
        leaderboardDao.getBestLines().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(bestLines -> binding.tvBestLines.setText(String.format(Locale.getDefault(), "Best:\n%d", bestLines)));
        leaderboardDao.getAverageLines().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(avgLines -> binding.tvAverageLines.setText(String.format(Locale.getDefault(), "Average:\n%d", avgLines)));
    }

    public int convertDpToPixel(float dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                getResources().getDisplayMetrics()
        );
    }
}
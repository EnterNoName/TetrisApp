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
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tetrisapp.R;
import com.example.tetrisapp.data.local.dao.LeaderboardDao;
import com.example.tetrisapp.databinding.FragmentScoresListBinding;
import com.example.tetrisapp.model.local.entity.LeaderboardEntry;

import java.util.ArrayList;
import java.util.List;

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
                            requireActivity().runOnUiThread(() -> binding.list.getAdapter().notifyItemRemoved(pos));
                        }, throwable -> Log.e("ScoresFragment", throwable.getLocalizedMessage()));
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                if (Math.abs(dX) < width / 3f) {
                    c.drawColor(requireActivity().getColor(R.color.dark_grey));
                } else if (dX > width / 3f) {
                    c.drawColor(requireActivity().getColor(R.color.red));
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
        binding.list.setAdapter(new ScoreRecyclerViewAdapter(requireContext(), this.leaderboardEntries));

        // Load RecyclerView items
        leaderboardDao.getSorted().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(leaderboardEntries -> {
            this.leaderboardEntries = leaderboardEntries;
            // RecyclerView adapter
            binding.list.setAdapter(new ScoreRecyclerViewAdapter(requireContext(), this.leaderboardEntries));
        }, throwable -> {
            if (!(throwable instanceof NullPointerException)) {
                Log.e("ScoresFragment", throwable.getLocalizedMessage());
            }
        });

        binding.btnBack.setOnClickListener(v -> Navigation.findNavController(binding.getRoot()).popBackStack());

        return binding.getRoot();
    }

    public int convertDpToPixel(float dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                getResources().getDisplayMetrics()
        );
    }
}
package com.example.tetrisapp.ui.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.tetrisapp.data.remote.ScoreboardService;
import com.example.tetrisapp.databinding.FragmentScoreboardBinding;
import com.example.tetrisapp.model.remote.request.GetScorePayload;
import com.example.tetrisapp.model.remote.response.ScorePayload;
import com.example.tetrisapp.ui.adapters.ScoresRecyclerViewAdapter;
import com.example.tetrisapp.util.FirebaseTokenUtil;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class ScoreboardFragment extends Fragment implements Callback<List<ScorePayload>> {
    private static final String TAG = "ScoreboardFragment";
    private FragmentScoreboardBinding binding;

    private int page = 1;

    @Inject
    ScoreboardService scoreboardService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentScoreboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getScores();
        initOnClickListeners();
    }

    private void initOnClickListeners() {
        binding.btnPrevPage.setOnClickListener(v -> {
            if (page > 1) page -= 1;
            getScores();
        });

        binding.btnNextPage.setOnClickListener(v -> {
            if (page > 1) page += 1;
            getScores();
        });
    }

    private void getScores() {
        FirebaseTokenUtil.getFirebaseToken(token -> {
            scoreboardService.getScores(new GetScorePayload(token, page)).enqueue(this);
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onResponse(Call<List<ScorePayload>> call, Response<List<ScorePayload>> response) {
        if(response.body() == null) return;

        binding.list.setAdapter(new ScoresRecyclerViewAdapter(
                requireContext(),
                response.body().stream().map(data -> new ScoresRecyclerViewAdapter.Score(
                        data.score,
                        data.level,
                        data.lines,
                        new Date(data.date),
                        data.name,
                        data.userId
                )).collect(Collectors.toList())
        ));
        Objects.requireNonNull(binding.list.getAdapter()).notifyDataSetChanged();
    }

    @Override
    public void onFailure(Call<List<ScorePayload>> call, Throwable t) {
        Log.e(TAG, t.getLocalizedMessage());
    }
}

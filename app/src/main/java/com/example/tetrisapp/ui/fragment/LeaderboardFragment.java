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
import androidx.navigation.Navigation;

import com.example.tetrisapp.data.local.dao.LeaderboardDao;
import com.example.tetrisapp.data.remote.LeaderboardService;
import com.example.tetrisapp.databinding.FragmentLeaderboardBinding;
import com.example.tetrisapp.model.remote.request.GetScorePayload;
import com.example.tetrisapp.model.remote.response.ResponseLeaderboardGet;
import com.example.tetrisapp.ui.activity.MainActivity;
import com.example.tetrisapp.ui.adapters.ScoresRecyclerViewAdapter;
import com.example.tetrisapp.util.FirebaseTokenUtil;
import com.example.tetrisapp.util.OnTouchListener;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class LeaderboardFragment extends Fragment implements Callback<ResponseLeaderboardGet> {
    private static final String TAG = "LeaderboardFragment";
    private FragmentLeaderboardBinding binding;

    private static final int LIMIT = 25;

    private int page = 1;
    private int pageCount = 1;

    @Inject LeaderboardDao leaderboardDao;
    @Inject LeaderboardService leaderboardService;
    private Call<ResponseLeaderboardGet> apiCall;

    private String token = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLeaderboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initOnClickListeners();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (apiCall != null) apiCall.cancel();
    }

    @Override
    public void onResume() {
        super.onResume();
        FirebaseTokenUtil.getFirebaseToken(token -> {
            this.token = token;
            getScores();
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initOnClickListeners() {
        binding.btnBack.setOnTouchListener(new OnTouchListener((MainActivity) requireActivity()));
        binding.btnBack.setOnClickListener(v -> Navigation.findNavController(binding.getRoot()).popBackStack());

        binding.btnPrevPage.setOnClickListener(v -> {
            if (this.page <= 1) return;

            this.page -= 1;
            getScores();
        });

        binding.btnNextPage.setOnClickListener(v -> {
            if (this.page >= pageCount) return;

            this.page += 1;
            getScores();
        });
    }

    private void getScores() {
        if (token == null) return;
        apiCall = leaderboardService.getScores(new GetScorePayload(token, page, LIMIT));
        apiCall.enqueue(this);
    }

    @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
    @Override
    public void onResponse(Call<ResponseLeaderboardGet> call, @NonNull Response<ResponseLeaderboardGet> response) {
        if (call.isCanceled()) return;
        if(response.body() == null) return;

        this.page = response.body().currentPage;
        this.pageCount = response.body().pageCount;
        List<ScoresRecyclerViewAdapter.Score> items = response.body(). data.stream().map(data -> new ScoresRecyclerViewAdapter.Score(
                data.score,
                data.level,
                data.lines,
                data.date,
                data.name,
                data.userId
        )).collect(Collectors.toList());
        requireActivity().runOnUiThread(() -> {
            binding.list.setAdapter(new ScoresRecyclerViewAdapter(requireContext(), items));
            Objects.requireNonNull(binding.list.getAdapter()).notifyDataSetChanged();
            binding.tvPage.setText(String.format(Locale.getDefault(), "Page: %s of %s", this.page, this.pageCount));

            binding.btnPrevPage.setEnabled(this.page > 1);
            binding.btnNextPage.setEnabled(this.page < this.pageCount);
        });
    }

    @Override
    public void onFailure(Call<ResponseLeaderboardGet> call, @NonNull Throwable t) {
        if (call.isCanceled()) return;
        Log.e(TAG, t.getLocalizedMessage());
    }
}

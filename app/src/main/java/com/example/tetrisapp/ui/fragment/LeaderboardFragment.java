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
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.tetrisapp.data.local.dao.LeaderboardDao;
import com.example.tetrisapp.data.remote.LeaderboardService;
import com.example.tetrisapp.databinding.FragmentLeaderboardBinding;
import com.example.tetrisapp.model.remote.callback.SimpleCallback;
import com.example.tetrisapp.model.remote.request.GetScorePayload;
import com.example.tetrisapp.model.remote.response.ResponseLeaderboardGet;
import com.example.tetrisapp.ui.activity.MainActivity;
import com.example.tetrisapp.ui.adapters.ScoresRecyclerViewAdapter;
import com.example.tetrisapp.ui.viewmodel.GameViewModel;
import com.example.tetrisapp.ui.viewmodel.LeaderboardViewModel;
import com.example.tetrisapp.util.FirebaseTokenUtil;
import com.example.tetrisapp.util.OnTouchListener;

import java.util.Comparator;
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
public class LeaderboardFragment extends Fragment {
    private static final String TAG = "LeaderboardFragment";
    private FragmentLeaderboardBinding binding;
    private LeaderboardViewModel viewModel;

    private static final int LIMIT = 25;

    private int page = 1;
    private int pageCount = 1;

    @Inject LeaderboardDao leaderboardDao;
    @Inject LeaderboardService leaderboardService;
    private Call<ResponseLeaderboardGet> apiCall;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(LeaderboardViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLeaderboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        startLoading();
        binding.list.setAdapter(new ScoresRecyclerViewAdapter(requireContext(), viewModel.getScores()));
        FirebaseTokenUtil.getFirebaseToken(token -> {
            viewModel.setToken(token);
            getScores();
        });
        initOnClickListeners();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (apiCall != null) apiCall.cancel();
    }

    private void startLoading() {
        binding.btnNextPage.setEnabled(false);
        binding.btnPrevPage.setEnabled(false);
        binding.progressBar.setVisibility(View.VISIBLE);
    }

    private void stopLoading() {
        binding.tvPage.setText(String.format(Locale.getDefault(), "Page: %s of %s", this.page, this.pageCount));
        binding.btnNextPage.setEnabled(this.page < this.pageCount);
        binding.btnPrevPage.setEnabled(this.page > 1);
        binding.progressBar.setVisibility(View.GONE);
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

    @SuppressLint("NotifyDataSetChanged")
    private void getScores() {
        if (viewModel.getToken() == null) return;
        startLoading();
        apiCall = leaderboardService.getScores(new GetScorePayload(viewModel.getToken(), page, LIMIT));
        apiCall.enqueue(new SimpleCallback.Builder<ResponseLeaderboardGet>()
                .setOnSuccessCallback((call, res) -> {
                    page = res.body().currentPage;
                    pageCount = res.body().pageCount;
                    viewModel.getScores().clear();
                    viewModel.getScores().addAll(res.body().data.stream().map(data -> new ScoresRecyclerViewAdapter.Score(
                            data.score,
                            data.level,
                            data.lines,
                            data.date,
                            data.name,
                            data.userId
                    )).sorted((o1, o2) -> o2.score - o1.score).collect(Collectors.toList()));
                    requireActivity().runOnUiThread(() -> {
                        Objects.requireNonNull(binding.list.getAdapter()).notifyDataSetChanged();
                        stopLoading();
                    });
        }).setOnFailureCallback(((call, t) -> stopLoading())).build());
    }
}

package com.example.tetrisapp.util;

import android.app.ActivityManager;
import android.util.Log;

import com.example.tetrisapp.data.local.dao.LeaderboardDao;
import com.example.tetrisapp.data.remote.LeaderboardService;
import com.example.tetrisapp.interfaces.Callback;
import com.example.tetrisapp.model.local.entity.LeaderboardEntry;
import com.example.tetrisapp.model.remote.callback.SimpleCallback;
import com.example.tetrisapp.model.remote.request.ScorePayload;
import com.example.tetrisapp.model.remote.request.SubmitScoresPayload;
import com.example.tetrisapp.model.remote.response.DefaultPayload;
import com.example.tetrisapp.model.remote.response.ResponseSubmitScore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class LeaderboardUtil {
    public static final String TAG = "LeaderboardUtil";

    private final FirebaseUser user;
    private final LeaderboardDao leaderboardDao;
    private final LeaderboardService leaderboardService;
    private final String token;

    public LeaderboardUtil(LeaderboardDao leaderboardDao) {
        this.leaderboardDao = leaderboardDao;
        this.leaderboardService = null;
        this.token = null;
        this.user = FirebaseAuth.getInstance().getCurrentUser();
    }

    public LeaderboardUtil(String token, LeaderboardDao leaderboardDao, LeaderboardService leaderboardService) {
        this.leaderboardDao = leaderboardDao;
        this.leaderboardService = leaderboardService;
        this.token = token;
        this.user = FirebaseAuth.getInstance().getCurrentUser();
    }

    public void insert(LeaderboardEntry entry) {
        if (user != null) {
            String ISODate = DateTimeUtil.toISOString(entry.date);
            String uid = user.getUid();
            entry.hash = HashUtil.sha256(String.format("%s_%s", uid, ISODate));
        }

        leaderboardDao
                .insert(entry)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        (data) -> {}, throwable -> Log.e(TAG, throwable.getLocalizedMessage()));
    }

    public void synchronise() {
        if (leaderboardService == null && token == null) return;

        generateHashes(() -> leaderboardDao
                .getByUploaded(false)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        (data) -> {
                            if (data.size() == 0) return;
                            
                            List<ScorePayload> scores = data.stream()
                                    .map(entry -> new ScorePayload(entry.score, entry.lines, entry.level, entry.date.getTime()))
                                    .collect(Collectors.toList());

                            leaderboardService
                                    .submitScores(new SubmitScoresPayload(token, scores))
                                    .enqueue(new SimpleCallback.Builder<List<ResponseSubmitScore>>().setOnSuccessCallback((call, res) -> {
                                        res.body().forEach(result -> leaderboardDao.getByHash(result.hash)
                                                .subscribeOn(Schedulers.io())
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribe(entry -> {
                                                    entry.uploaded = result.completed;
                                                    update(entry);
                                                }));
                                    }).build());
                        }, throwable -> Log.e(TAG, throwable.getLocalizedMessage())));
    }

    private void update(LeaderboardEntry entry) {
        leaderboardDao.update(entry).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {}, (t) -> {});
    }

    private void generateHashes(Callback callback) {
        if (leaderboardService == null) return;

        leaderboardDao
                .getWithoutHash()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        (data) -> {
                            if (user == null) return;
                            String uid = user.getUid();

                            data.forEach(entry -> {
                                String ISODate = DateTimeUtil.toISOString(entry.date);
                                entry.hash = HashUtil.sha256(String.format("%s_%s", uid, ISODate));
                                update(entry);
                            });

                            callback.call();
                        }, throwable -> {
                            Log.e(TAG, throwable.getLocalizedMessage());
                        });
    }
}

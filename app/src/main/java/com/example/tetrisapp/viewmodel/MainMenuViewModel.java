package com.example.tetrisapp.viewmodel;

import android.view.View;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

public class MainMenuViewModel extends ViewModel {
    private Callback startCallback = null;
    private Callback quitCallback = null;
    private Callback resetScoreCallback = null;
    private MutableLiveData<Integer> currentHighScore = new MutableLiveData<>(0);

    public void onClickStart(View v) {
        if (startCallback != null) startCallback.call();
    }

    public void onClickQuit(View v) {
        if (quitCallback != null) quitCallback.call();
    }

    public void onClickReset(View v) {
        if (resetScoreCallback != null) resetScoreCallback.call();
    }

    public void setStartCallback(Callback startCallback) {
        this.startCallback = startCallback;
    }

    public void setQuitCallback(Callback quitCallback) {
        this.quitCallback = quitCallback;
    }

    public void setResetScoreCallback(Callback resetScoreCallback) {
        this.resetScoreCallback = resetScoreCallback;
    }

    public LiveData<String> getCurrentHighScore() {
        return Transformations.map(currentHighScore, Object::toString);
    }

    public void setCurrentHighScore(int highScore) {
        this.currentHighScore.postValue(highScore);
    }

    public interface Callback {
        void call();
    }
}

package com.example.tetrisapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

public class GameOverViewModel extends ViewModel {
    private MutableLiveData<Integer> score = new MutableLiveData<>(0);
    private MutableLiveData<Integer> highScore = new MutableLiveData<>(0);
    private MutableLiveData<Boolean> isNewHighScore = new MutableLiveData<>(false);

    public LiveData<Boolean> getIsNewHighScore() {
        return isNewHighScore;
    }

    public LiveData<String> getScore() {
        return Transformations.map(score, Object::toString);
    }

    public LiveData<String> getHighScore() {
        return Transformations.map(highScore, Object::toString);
    }

    public void setIsNewHighScore(boolean isTrue) {
        isNewHighScore.postValue(isTrue);
    }

    public void setScore(int score) {
        this.score.postValue(score);
    }

    public void setHighScore(int highScore) {
        this.highScore.postValue(highScore);
    }
}

package com.example.tetris.viewmodel;

import android.graphics.drawable.Drawable;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.example.tetris.R;

public class GameViewModel extends ViewModel {
    private MutableLiveData<Integer> score = new MutableLiveData(0);
    private MutableLiveData<Integer> lines = new MutableLiveData(0);
    private MutableLiveData<Integer> level = new MutableLiveData(0);
    private MutableLiveData<Drawable> nextTetromino = new MutableLiveData(null);

    public LiveData<String> getScore() {
        return Transformations.map(score, Object::toString);
    }

    public LiveData<String> getLines() {
        return Transformations.map(lines, Object::toString);
    }

    public LiveData<String> getLevel() {
        return Transformations.map(level, Object::toString);
    }

    public LiveData<Drawable> getNextTetromino() {
        return nextTetromino;
    }

    public void setScore(int score) {
        this.score.postValue(score);
    }

    public void setLines(int lines) {
        this.lines.postValue(lines);
    }

    public void setLevel(int level) {
        this.level.postValue(level);
    }

    public void setNextTetromino(Drawable nextTetromino) {
        this.nextTetromino.postValue(nextTetromino);
    }
}

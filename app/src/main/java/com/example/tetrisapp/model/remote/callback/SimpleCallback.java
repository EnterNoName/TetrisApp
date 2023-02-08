package com.example.tetrisapp.model.remote.callback;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.tetrisapp.model.remote.response.DefaultPayload;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SimpleCallback<T> implements Callback<T> {
    public static final String TAG = "SimpleCallback";

    private OnSuccessCallback<T> onSuccessCallback = null;
    private OnFailureCallback<T> onFailureCallback = null;

    public SimpleCallback() {
    }

    public static class Builder<T> {
        SimpleCallback<T> simpleCallback;

        public Builder() {
            this.simpleCallback = new SimpleCallback<T>();
        }

        public Builder<T> setOnSuccessCallback(OnSuccessCallback<T> onSuccessCallback) {
            simpleCallback.onSuccessCallback = onSuccessCallback;
            return this;
        }

        public Builder<T> setOnFailureCallback(OnFailureCallback<T> onFailureCallback) {
            simpleCallback.onFailureCallback = onFailureCallback;
            return this;
        }

        public SimpleCallback<T> build() {
            return this.simpleCallback;
        }
    }

    @Override
    public void onResponse(Call<T> call, @NonNull Response<T> response) {
        if (call.isCanceled()) return;
        if(response.body() == null && !response.isSuccessful()) return;

        if (onSuccessCallback == null) return;
        onSuccessCallback.call(call, response);
    }

    @Override
    public void onFailure(Call<T> call, @NonNull Throwable t) {
        if (call.isCanceled()) return;
        Log.e(TAG, t.getLocalizedMessage());

        if (onFailureCallback == null) return;
        onFailureCallback.call(call, t);
    }

    public interface OnSuccessCallback<T> {
        void call(Call<T> call, Response<T> response);
    }

    public interface OnFailureCallback<T> {
        void call(Call<T> call, Throwable t);
    }
}

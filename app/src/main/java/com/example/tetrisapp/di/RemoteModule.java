package com.example.tetrisapp.di;

import android.app.Application;

import com.example.tetrisapp.R;
import com.example.tetrisapp.data.remote.GameService;
import com.example.tetrisapp.data.remote.LobbyService;
import com.example.tetrisapp.data.remote.UpdateService;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
@InstallIn(SingletonComponent.class)
public class RemoteModule {

    @Provides
    @Singleton
    static Retrofit provideRetrofitClient(Application app) {
        return new Retrofit.Builder()
                .baseUrl(app.getString(R.string.update_url))
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    @Provides
    @Singleton
    static UpdateService provideUpdateService(Retrofit retrofit) {
        return retrofit.create(UpdateService.class);
    }

    @Provides
    @Singleton
    static LobbyService provideLobbyService(Retrofit retrofit) {
        return retrofit.create(LobbyService.class);
    }

    @Provides
    @Singleton
    static GameService provideGameService(Retrofit retrofit) {
        return retrofit.create(GameService.class);
    }
}

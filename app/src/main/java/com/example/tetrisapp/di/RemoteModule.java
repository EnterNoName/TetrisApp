package com.example.tetrisapp.di;

import android.app.Application;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.tetrisapp.R;
import com.example.tetrisapp.data.remote.GameService;
import com.example.tetrisapp.data.remote.LeaderboardService;
import com.example.tetrisapp.data.remote.LobbyService;
import com.example.tetrisapp.data.remote.UpdateService;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.pusher.client.Pusher;
import com.pusher.client.PusherOptions;
import com.pusher.client.connection.ConnectionEventListener;
import com.pusher.client.connection.ConnectionState;
import com.pusher.client.connection.ConnectionStateChange;
import com.pusher.client.util.HttpChannelAuthorizer;
import com.pusher.client.util.UrlEncodedConnectionFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.concurrent.ExecutionException;

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

    @Provides
    @Singleton
    static LeaderboardService provideScoreboardService(Retrofit retrofit) {
        return retrofit.create(LeaderboardService.class);
    }

    @Provides
    @Singleton
    static FirebaseApp provideFirebaseApp(Application app) {
        return FirebaseApp.initializeApp(app);
    }

    @Provides
    static FirebaseAuth provideFirebaseAuth(FirebaseApp app) {
        return FirebaseAuth.getInstance(app);
    }

    @Nullable
    @Provides
    static FirebaseUser provideFirebaseUser(FirebaseAuth auth) {
        return auth.getCurrentUser();
    }

    @Provides
    @Singleton
    static Pusher providePusher(Application app, @Nullable FirebaseUser user) {
        PusherOptions options = new PusherOptions();

        options.setCluster("ap1");

        options.setChannelAuthorizer(new HttpChannelAuthorizer(app.getString(R.string.update_url) + "auth", new UrlEncodedConnectionFactory() {
            @Override
            public String getBody() {
                final StringBuilder urlParameters = new StringBuilder(super.getBody());

                try {
                    assert user != null;
                    String idToken = Tasks.await(user.getIdToken(true)).getToken();
                    urlParameters.append("&idToken=").append(URLEncoder.encode(idToken, getCharset()));
                } catch (ExecutionException | InterruptedException | UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                return urlParameters.toString();
            }
        }));

        Pusher pusher = new Pusher("754b90f0401e477f519a", options);

        pusher.connect(new ConnectionEventListener() {
            @Override
            public void onConnectionStateChange(ConnectionStateChange change) {
                Log.i("Pusher", "State changed from " + change.getPreviousState() +
                        " to " + change.getCurrentState());
            }

            @Override
            public void onError(String message, String code, Exception e) {
                Log.i("Pusher", "There was a problem connecting! " +
                        "\ncode: " + code +
                        "\nmessage: " + message +
                        "\nException: " + e
                );
            }
        }, ConnectionState.ALL);

        return pusher;
    }
}

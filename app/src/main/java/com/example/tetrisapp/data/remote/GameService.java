package com.example.tetrisapp.data.remote;

import com.example.tetrisapp.model.remote.ChangePlayfieldPayload;
import com.example.tetrisapp.model.remote.DefaultResponse;
import com.example.tetrisapp.model.remote.MovePiecePayload;
import com.example.tetrisapp.model.remote.StartGamePayload;
import com.example.tetrisapp.model.remote.TokenPayload;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface GameService {
    @POST("game/start")
    Call<DefaultResponse> startGame(@Body StartGamePayload body);

    @POST("game/piecemoved")
    Call<DefaultResponse> movePiece(@Body MovePiecePayload body);

    @POST("game/playfieldchanged")
    Call<DefaultResponse> changePlayfield(@Body ChangePlayfieldPayload body);

    @POST("game/gameended")
    Call<DefaultResponse> endGame(@Body TokenPayload body);
}

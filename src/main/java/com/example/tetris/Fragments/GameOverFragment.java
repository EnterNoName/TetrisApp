package com.example.tetris.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.tetris.R;
import com.example.tetris.Utils.JSON;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GameOverFragment extends DialogFragment {

    private final int score, lines, level;
    private int highScore = 0;

    public GameOverFragment(int score, int lines, int level) {
        this.score = score;
        this.lines = lines;
        this.level = level;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savesInstanceState) {
        super.onCreateView(inflater, container, savesInstanceState);
        View view = inflater.inflate(R.layout.fragment_game_over, container, false);
        TextView tvScore = view.findViewById(R.id.currentScore);
        TextView tvHighScore = view.findViewById(R.id.highScore);

        tvScore.setText("Your score: " + score);

        try {
            JSONObject data = JSON.read(requireContext(), "save.json");

            if (!data.isNull("bestAttempts")) {
                highScore = data.getJSONArray("bestAttempts").getJSONObject(0).getInt("score");

                JSONArray attempts = data.getJSONArray("bestAttempts");
                for (int i = 0; i < 5; i++) {
                    if (!attempts.isNull(i)) {
                        JSONObject attemptData = attempts.getJSONObject(i);
                        if (score > attemptData.getInt("score")) {
                            attemptData.put("score", score);
                            attemptData.put("lines", lines);
                            attemptData.put("level", level);
                            data.getJSONArray("bestAttempts").put(i, attemptData);
                            break;
                        }
                    } else {
                        JSONObject attemptData = new JSONObject();
                        attemptData.put("score", score);
                        attemptData.put("lines", lines);
                        attemptData.put("level", level);
                        data.getJSONArray("bestAttempts").put(attemptData);
                        break;
                    }
                }
            } else {
                data.put("bestAttempts", new JSONArray());
                JSONObject attemptData = new JSONObject();
                attemptData.put("score", score);
                attemptData.put("lines", lines);
                attemptData.put("level", level);
                data.getJSONArray("bestAttempts").put(attemptData);
            }

            JSON.write(getContext(), "save.json", data, false);

            if (score > highScore) {
                tvHighScore.setText(R.string.new_high_score);
            } else {
                tvHighScore.setText(getResources().getString(R.string.current_high_score, highScore));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().finish();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
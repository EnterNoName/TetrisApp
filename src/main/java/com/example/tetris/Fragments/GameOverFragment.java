package com.example.tetris.Fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.tetris.R;
import com.example.tetris.Utils.JSON;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GameOverFragment extends Fragment implements View.OnClickListener {
    public GameOverFragment() {
        super(R.layout.fragment_game_over);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        int score = requireArguments().getInt("score"),
                lines = requireArguments().getInt("lines"),
                level = requireArguments().getInt("level"),
                highScore = 0;

        TextView tvScore = view.findViewById(R.id.currentScore);
        TextView tvHighScore = view.findViewById(R.id.highScore);

        tvScore.setText(getString(R.string.final_score, score));

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
                tvHighScore.setText(getString(R.string.current_high_score, highScore));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        view.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        requireActivity().finish();
    }
}
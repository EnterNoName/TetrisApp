package com.example.tetrisapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.tetrisapp.R;
import com.example.tetrisapp.databinding.GameFragmentBinding;
import com.example.tetrisapp.view.Tetris;
import com.example.tetrisapp.model.Tetromino;
import com.example.tetrisapp.util.GameEvent;
import com.example.tetrisapp.viewmodel.GameViewModel;

public class GameFragment extends Fragment {
    private GameFragmentBinding binding;
    private GameViewModel viewModel;
    private Tetris game;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(GameViewModel.class);
        binding = DataBindingUtil.inflate(inflater, R.layout.game_fragment, container, false);
        binding.setViewModel(viewModel);
        binding.setLifecycleOwner(this);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        game = new Tetris(requireContext());
        binding.layout.addView(game, 0);

        game.setGameEventListener(event -> {
            switch (event.getType()) {
                case LEVEL_UPDATE:
                    int level = ((GameEvent.LevelUpdate) event).getPayload();
                    viewModel.setLevel(level);
                    break;
                case SCORE_UPDATE:
                    int score = ((GameEvent.ScoreUpdate) event).getPayload();
                    viewModel.setScore(score);
                    break;
                case LINES_UPDATE:
                    int lines = ((GameEvent.LinesUpdate) event).getPayload();
                    viewModel.setLines(lines);
                    break;
                case NEXT_PIECE:
                    Tetromino.Shape nextPiece = ((GameEvent.NewNextPiece) event).getPayload();
                    switch (nextPiece) {
                        case O: // O-Shape
                            viewModel.setNextTetromino(AppCompatResources.getDrawable(requireContext(), R.drawable.o_tetromino));
                            break;
                        case I: // I-Shape
                            viewModel.setNextTetromino(AppCompatResources.getDrawable(requireContext(), R.drawable.i_tetromino));
                            break;
                        case T: // T-Shape
                            viewModel.setNextTetromino(AppCompatResources.getDrawable(requireContext(), R.drawable.t_tetromino));
                            break;
                        case J: // J-Shape
                            viewModel.setNextTetromino(AppCompatResources.getDrawable(requireContext(), R.drawable.j_tetromino));
                            break;
                        case L: // L-Shape
                            viewModel.setNextTetromino(AppCompatResources.getDrawable(requireContext(), R.drawable.l_tetromino));
                            break;
                        case Z: // Z-Shape
                            viewModel.setNextTetromino(AppCompatResources.getDrawable(requireContext(), R.drawable.z_tetromino));
                            break;
                        case S: // S-Shape
                            viewModel.setNextTetromino(AppCompatResources.getDrawable(requireContext(), R.drawable.s_tetromino));
                            break;
                    }
                    break;
                case GAME_PAUSE: {
                    Tetris.GameStatistics stats = ((GameEvent.GamePause) event).getPayload();
                    Bundle args = new Bundle();
                    args.putParcelable("statistics", stats);
                    getChildFragmentManager().beginTransaction()
                            .setReorderingAllowed(true)
                            .replace(R.id.fragment_container_view, PauseFragment.class, args)
                            .commit();
                    break;
                }
                case GAME_OVER: {
                    Tetris.GameStatistics stats = ((GameEvent.GameOver) event).getPayload();
                    Bundle args = new Bundle();
                    args.putParcelable("statistics", stats);
                    getChildFragmentManager().beginTransaction()
                            .setReorderingAllowed(true)
                            .replace(R.id.fragment_container_view, GameOverFragment.class, args)
                            .commit();
                    break;
                }
            }
        });

        // In-game buttons click handler
        binding.btnRotateLeft.setOnClickListener(v -> game.rotateActivePieceLeft());
        binding.btnRotateRight.setOnClickListener(v -> game.rotateActivePieceRight());
        binding.btnMoveLeft.setOnClickListener(v -> game.moveActivePieceLeft());
        binding.btnMoveRight.setOnClickListener(v -> game.moveActivePieceRight());
    }

    public void onGameResume() {
        game.resumeThreads();
    }

    public void onStopGame() {
        game.stopThreads();
    }
}

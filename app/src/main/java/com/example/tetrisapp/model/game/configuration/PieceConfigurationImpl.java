package com.example.tetrisapp.model.game.configuration;

import com.example.tetrisapp.R;
import com.example.tetrisapp.model.game.Piece;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class PieceConfigurationImpl implements PieceConfiguration {
    Map<String, Piece> pieceMap = new HashMap<>();

    {
        Iterator<Piece> iterator = Arrays.stream(new Piece[]{
                new Piece(
                        "I",
                        new byte[][]{
                                {0, 0, 0, 0},
                                {1, 1, 1, 1},
                                {0, 0, 0, 0},
                                {0, 0, 0, 0}
                        },
                        0xFF00FFFF,
                        R.drawable.point_overlay
                ),
                new Piece(
                        "J",
                        new byte[][]{
                                {1, 0, 0},
                                {1, 1, 1},
                                {0, 0, 0}
                        },
                        0xFF0000FF,
                        R.drawable.point_overlay
                ),
                new Piece(
                        "L",
                        new byte[][]{
                                {0, 0, 1},
                                {1, 1, 1},
                                {0, 0, 0}
                        },
                        0xFFFF7F00,
                        R.drawable.point_overlay
                ),
                new Piece(
                        "O",
                        new byte[][]{
                                {1, 1},
                                {1, 1}
                        },
                        0xFFFFFF00,
                        R.drawable.point_overlay
                ),
                new Piece(
                        "S",
                        new byte[][]{
                                {0, 1, 1},
                                {1, 1, 0},
                                {0, 0, 0}
                        },
                        0xFF00FF00,
                        R.drawable.point_overlay
                ),
                new Piece(
                        "Z",
                        new byte[][]{
                                {1, 1, 0},
                                {0, 1, 1},
                                {0, 0, 0}
                        },
                        0xFFFF0000,
                        R.drawable.point_overlay
                ),
                new Piece(
                        "T",
                        new byte[][]{
                                {0, 1, 0},
                                {1, 1, 1},
                                {0, 0, 0}
                        },
                        0xFF800080,
                        R.drawable.point_overlay
                ),
        }).iterator();

        while(iterator.hasNext()) {
            Piece piece = iterator.next();
            pieceMap.put(piece.getName(), piece);
        }
    }

    @Override
    public Piece[] getPieces() {
        return pieceMap.values().toArray(new Piece[0]);
    }

    @Override
    public String[] getNames() {
        return pieceMap.keySet().toArray(new String[0]);
    }

    @Override
    public Piece get(String name) {
        return pieceMap.getOrDefault(name, null);
    }
}

package com.example.tetrisapp.data;

import static com.example.tetrisapp.util.ArrayHelper.concat;

import com.example.tetrisapp.util.MathHelper;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;

public class TetrominoRandomizer implements Iterator<String> {
    private final String[] firstPieces;
    private final String[] initialHistory;
    private final String[] pool;

    private final LinkedList<String> order = new LinkedList<>();
    private final LinkedList<String> history = new LinkedList<>();

    private boolean isFirst = true;

    public TetrominoRandomizer(String[] pieces, String[] firstPieces, String[] initialHistory) {
        this.firstPieces = firstPieces;
        this.initialHistory = initialHistory;
        this.pool = concat(pieces, pieces, pieces, pieces, pieces);
    }

    @Override
    public boolean hasNext() {
        return true;
    }

    @Override
    public String next() {
        if (isFirst) {
            isFirst = false;

            String firstPiece = firstPieces[MathHelper.getRandomInt(0, firstPieces.length - 1)];
            history.addAll(Arrays.asList(initialHistory));
            history.add(firstPiece);

            return firstPiece;
        }

        String piece = null;
        int ind = 0;

        for (int roll = 0; roll < 6; roll++) {
            ind = MathHelper.getRandomInt(0, pool.length - 1);
            piece = pool[ind];

            if (!history.contains(piece) || roll == 5) {
                break;
            }

            if (order.size() != 0) pool[ind] = order.get(0);
        }

        order.remove(piece);
        order.add(piece);

        pool[ind] = order.get(0);

        history.removeFirst();
        history.add(piece);

        return piece;
    }
}
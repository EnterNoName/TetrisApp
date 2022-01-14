package com.example.tetris.Models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.stream.Collectors;

public class Playfield {
    public final int[][] playfieldState = new int[22][10];

    public Playfield() {
        for (int y = 0; y < playfieldState.length; y++) {
            Arrays.fill(playfieldState[y], 0xff000000);
        }
    }

    public List<Integer> lineClear() {
        int currentCombo = -1;
        boolean prevLineFilled = false;

        List<Integer> lineData = new ArrayList<>();

        for (int y = 0; y < playfieldState.length; y++) {
            boolean isFilled = true;
            for (int x = 0; x < playfieldState[y].length; x++) {
                if (playfieldState[y][x] == 0xff000000) {
                    isFilled = false;
                    break;
                }
            }
            if (isFilled) {
                Arrays.fill(playfieldState[y], 0xff000000);

                if (prevLineFilled) {
                    int prevValue = lineData.get(currentCombo);
                    lineData.add(currentCombo, prevValue + 1);
                } else {
                    currentCombo++;
                    lineData.add(0);
                    int prevValue = lineData.get(currentCombo);
                    lineData.add(currentCombo, prevValue + 1);
                    prevLineFilled = true;
                }
            } else prevLineFilled = false;
        }

        return lineData;
    }

    private void processNode(int x, int y, List<Point> region, int[][] playfieldCopy, Queue<Point> queue) {
        region.add(new Point(x, y, playfieldCopy[y][x]));
        playfieldCopy[y][x] = 0xff000000;
        queue.add(region.get(region.size() - 1));
    }

    private List<Point> floodFill(Point firstNode, Callback callback) {
        Queue<Point> queue = new LinkedList();
        queue.add(firstNode);

        int[][] playfieldCopy = playfieldState;
        playfieldCopy[firstNode.y][firstNode.x] = 0xff000000;

        List<Point> region = new ArrayList<>();
        region.add(firstNode);

        while (queue.size() > 0) { // Process surroundings add new nodes to the queue
            Point node = queue.remove();
            if (node.x - 1 > 0 &&
                    callback.func(playfieldCopy[node.y][node.x - 1]))
                processNode(node.x - 1, node.y, region, playfieldCopy, queue); // Process node to the West

            if (node.x + 1 < playfieldCopy[node.y].length &&
                    callback.func(playfieldCopy[node.y][node.x + 1]))
                processNode(node.x + 1, node.y, region, playfieldCopy, queue); // Process node to the East

            if (node.y - 1 > 0 &&
                    callback.func(playfieldCopy[node.y - 1][node.x]))
                processNode(node.x, node.y - 1, region, playfieldCopy, queue); // Process node to the North

            if (node.y + 1 < playfieldCopy.length &&
                    callback.func(playfieldCopy[node.y + 1][node.x]))
                processNode(node.x, node.y + 1, region, playfieldCopy, queue); // Process node to the South
        }
        return region;
    }

    public void settleLines() {
        List<List<Point>> coloredRegions = new ArrayList<>();
        // Finds all the colored regions and puts their points into separate arrays
        for (int y = 0; y < playfieldState.length; y++) {
            for (int x = 0; x < playfieldState[y].length; x++) {
                if (playfieldState[y][x] != 0xff000000) {
                    int finalY = y, finalX = x;
                    List<Point> flat = coloredRegions.stream()
                            .flatMap(List::stream)
                            .collect(Collectors.toList());
                    if (flat.stream().anyMatch(point -> (point.y == finalY && point.x == finalX)))
                        continue;
                    coloredRegions.add(floodFill(new Point(x, y, playfieldState[y][x]), color -> color != 0xff000000));
                }
            }
        }

        for (int i = coloredRegions.size() - 1; i >= 0; i--) {
            Map<Integer, Integer> lowestPoints = new HashMap<>();
            List<Point> region = coloredRegions.get(i);
            int count = 1;

            for (Point point : region) { // Erase region calculate lowest point for every x coordinate
                if (lowestPoints.get(point.x) == null || lowestPoints.get(point.x) < point.y) {
                    lowestPoints.put(point.x, point.y);
                }
                playfieldState[point.y][point.x] = 0xff000000;
            }

            outer:
            // Checking for collisions
            while (true) {
                for (Map.Entry<Integer, Integer> entry : lowestPoints.entrySet()) {
                    int x = entry.getKey(), y = entry.getValue() + count;
                    if (y >= playfieldState.length || playfieldState[y][x] != 0xff000000)
                        break outer;
                }
                count++;
            }

            count--; // Color the region in it's new\same place
            for (Point point : region) {
                playfieldState[point.y + count][point.x] = point.color;
            }
        }
    }

    public void solidifyPiece(Tetromino t) {
        int pieceX = t.getX(), pieceY = t.getY();
        for (int pointY = 0; pointY < t.getShapeMatrix().length; pointY++) {
            for (int pointX = 0; pointX < t.getShapeMatrix()[pointY].length; pointX++) {
                if (t.getShapeMatrix()[pointY][pointX] == 1) {
                    playfieldState[pieceY + pointY][pieceX + pointX] = t.getColor();
                }
            }
        }
    }

    public boolean hasCollision(Tetromino t) {
        int pieceX = t.getX(), pieceY = t.getY();
        for (int pointY = 0; pointY < t.getShapeMatrix().length; pointY++) {
            for (int pointX = 0; pointX < t.getShapeMatrix()[pointY].length; pointX++) {
                if (t.getShapeMatrix()[pointY][pointX] == 1) {
                    if ((pieceY + pointY) < 0 || (pieceY + pointY) > (playfieldState.length - 1) ||
                            (pieceX + pointX) < 0 || (pieceX + pointX) > (playfieldState[pieceY + pointY].length - 1) ||
                            playfieldState[pieceY + pointY][pieceX + pointX] != 0xff000000) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private interface Callback {
        boolean func(int color);
    }

    private class Point {
        int x, y, color;

        Point(int x, int y, int color) {
            this.x = x;
            this.y = y;
            this.color = color;
        }
    }
}

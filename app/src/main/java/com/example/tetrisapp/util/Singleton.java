package com.example.tetrisapp.util;

import java.util.Random;

public class Singleton {
    public static final Singleton INSTANCE = new Singleton();

    private Singleton() {}

    public Random random = new Random();
}
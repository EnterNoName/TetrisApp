package com.example.tetrisapp.util;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.stream.Stream;

public class ArrayUtil {
    @SafeVarargs
    public static <T> T[] concat(T[]... arrays) {
        int totalLen = 0;
        for (T[] arr: arrays) {
            totalLen += arr.length;
        }
        T[] result = (T[]) Array.newInstance(arrays.getClass().getComponentType().getComponentType(), totalLen);

        int copied = 0;
        for (T[] arr: arrays) {
            System.arraycopy(arr, 0, result, copied, arr.length);
            copied += arr.length;
        }

        return result;
    }

    public static <T> boolean includes(T[] array, T item) {
        return Arrays.asList(array).contains(item);
    }

    public static byte[][] deepCopy(byte[][] matrix) {
        return Arrays.stream(matrix).map(byte[]::clone).toArray($ -> matrix.clone());
    }

    public static <T> T[][] deepCopy(T[][] matrix) {
        return Arrays.stream(matrix).map(el -> el.clone()).toArray($ -> matrix.clone());
    }
}

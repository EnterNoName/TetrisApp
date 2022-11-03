package com.example.tetrisapp.util;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.stream.Stream;

public class ArrayHelper {
    @SafeVarargs
    public static <T> T[] concat(T[] array1, T[] array2, T[]... args) {
        T[] result = Stream.concat(Arrays.stream(array1), Arrays.stream(array2))
                .toArray(size -> (T[]) Array.newInstance(array1.getClass().getComponentType(), size));

        for (T[] array : args) {
            result = Stream.concat(Arrays.stream(result), Arrays.stream(array))
                    .toArray(size -> (T[]) Array.newInstance(array1.getClass().getComponentType(), size));
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

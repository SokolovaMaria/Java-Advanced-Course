package ru.ifmo.ctddev.sokolova.concurrent;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Comparator.naturalOrder;

/**
 * Created by maria on 27.03.17.
 */
public class Main {
    public static void main(String[] args) throws InterruptedException {
        ParallelMapper mapper = new ParallelMapperImpl(4);
        IterativeParallelism executor = new IterativeParallelism(mapper);
        ArrayList list = new ArrayList<>(Arrays.asList(56, 7, -89, 34, 67, 12, 34, 7));
        //int res = executor.maximum(4, list, Comparator.naturalOrder());
        //Comparator Evenfirst = Comparator.<Integer>comparingInt(i -> i % 2).thenComparing(Integer::intValue);

    }



}

package ru.ifmo.ctddev.sokolova.arrayset;

import java.util.Arrays;
import java.util.Comparator;

/**
 * Created by maria on 19.02.17.
 */

/**java -classpath ./src:./lib/hamcrest-core-1.3.jar:./lib/junit-4.11.jar:./lib/ArraySetTest.jar info.kgeorgiy.java.advanced.arrayset.MyTester NavigableSet ru.ifmo.ctddev.sokolova.arrayset.ArraySetTest1**/

public class Main {
    public static void main(String[] args) {
        /*ArraySetTest1<Integer> set = new ArraySetTest1<>(
                Arrays.asList(511265961, 1877706961, 1151093223), comparingInt(i -> i/100)
        );
        System.out.println(set.lower(1877706961));*/
        Comparator Evenfirst = Comparator.<Integer>comparingInt(i -> i % 2).thenComparing(Integer::intValue);
        Comparator ReversedOrder = Comparator.comparingInt(Integer::intValue).reversed();
        Comparator AllEqual = Comparator.comparingInt(i -> 0);
        //System.out.println(set.floor(640711405));
        ArraySet<Integer> set = new ArraySet<>(
                Arrays.asList(-887442106, -1199287824, 2091233388), Evenfirst);
        set.tailSet(-887442106);
        //System.out.println(set.subSet(2, false,  3, false));

        /*ArraySetTest1<Integer> set1 = new ArraySetTest1<>(
                Arrays.asList(-1372293290, -534455834, 101529898, -1655579227, -922955689, -78996226, -1385543947, 1019065251, -1214988999, -968888429), AllEqual
        );*/
        //System.out.println(set1.lower(-1372293290));
        /*ArraySetTest1<Integer> set = new ArraySetTest1<>(Arrays.asList(1, 2, 4, 5, 6));
        System.out.println(set.subSet(1, true, 3, true).toArray());*/
    }
}

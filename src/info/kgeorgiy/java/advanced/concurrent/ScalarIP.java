//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package info.kgeorgiy.java.advanced.concurrent;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

public interface ScalarIP {
    <T> T maximum(int var1, List<? extends T> var2, Comparator<? super T> var3) throws InterruptedException;

    <T> T minimum(int var1, List<? extends T> var2, Comparator<? super T> var3) throws InterruptedException;

    <T> boolean all(int var1, List<? extends T> var2, Predicate<? super T> var3) throws InterruptedException;

    <T> boolean any(int var1, List<? extends T> var2, Predicate<? super T> var3) throws InterruptedException;
}

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package info.kgeorgiy.java.advanced.concurrent;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public interface ListIP extends ScalarIP {
    String join(int var1, List<?> var2) throws InterruptedException;

    <T> List<T> filter(int var1, List<? extends T> var2, Predicate<? super T> var3) throws InterruptedException;

    <T, U> List<U> map(int var1, List<? extends T> var2, Function<? super T, ? extends U> var3) throws InterruptedException;
}

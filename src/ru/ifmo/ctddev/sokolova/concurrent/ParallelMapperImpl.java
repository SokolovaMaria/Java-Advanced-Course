package ru.ifmo.ctddev.sokolova.concurrent;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;

/**
 * Created by maria on 26.03.17.
 */
public class ParallelMapperImpl implements ParallelMapper {

    /**
     * Synchronized queue of {@link Runnable} tasks, extends {@link LinkedList}
     * {@link Override} on method add : add a task and notify at one method call
     */
    private final SynchronizedQueue<Runnable> queue = new SynchronizedQueue<>();

    /**
     * List of working threads
     */
    private final List<Thread> threads = new ArrayList<>();

    /**
     * Constructor of {@link ParallelMapperImpl } class, that implements {@link ParallelMapper} interface
     *
     * @param n - the number of threads, that we can use to parallel
     * Create n threads, each running one task from the queue
     */
    public ParallelMapperImpl(int n) {
        Runnable runnable = () -> {
            try {
                while (!Thread.interrupted()) {
                    Runnable nextTask;
                    synchronized (queue) {
                        while (queue.isEmpty()) {
                            queue.wait();
                        }
                        nextTask = queue.remove();
                    }
                    nextTask.run();
                }
            } catch (InterruptedException ignored) {

            } finally {
                Thread.currentThread().interrupt();
            }
        };
        for (int i = 0; i < n; i++) {
            Thread t = new Thread(runnable);
            threads.add(t);
            t.start();
        }
    }

    /**
     * Apply f to each element in the list, and put every single application into the global queue of tasks
     *
     * @param f - function to be mapped onto the elements of the list args
     * @param args - list of arguments to count the function
     * @param <T> - type of the elements in the list
     * @param <R> - type of the function return result
     * @return - resulting list, with f applied to each element
     * @throws InterruptedException
     */
    public <T, R> List<R> map(Function<? super T, ? extends R> f, List<? extends T> args) throws InterruptedException {
        Applier<R> applier = new Applier<>(args.size());
        synchronized (queue) {
            for (int i = 0; i < args.size(); i++) {
                final int fi = i;
                queue.add(() -> applier.setResult(fi, f.apply(args.get(fi))));
            }
        }
        return applier.getResults();
    }


    private class SynchronizedQueue<T> extends LinkedList<T> {
        @Override
        public boolean add(T task) throws IllegalMonitorStateException {
            boolean add = super.add(task);
            notify();
            return add;
        }
    }

    /**
     * Interrupts all working threads from threads
     * @throws InterruptedException
     */
    @Override
    public void close() throws InterruptedException {
        for (Thread thread : threads) {
            thread.interrupt();
            thread.join();
        }
    }
}

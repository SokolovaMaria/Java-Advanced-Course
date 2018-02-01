package ru.ifmo.ctddev.sokolova.concurrent;

import info.kgeorgiy.java.advanced.concurrent.ListIP;
import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import javax.imageio.ImageTranscoder;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by maria on 19.03.17.
 */
public class IterativeParallelism implements ListIP {

    private ParallelMapper mapper = null;

    /**
     * Default constructor for {@link IterativeParallelism}
     */
    public IterativeParallelism() {}

    /**
     * Constructor of {@link IterativeParallelism} that takes an instance of {@link ParallelMapperImpl} as an argument
     * @param mapper an instance of {@link ParallelMapperImpl}
     */
    public IterativeParallelism(ParallelMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * Divides {@link List} allWork into n parts
     *
     * @param n the number of parts, that we should divide our list to
     * @param allWork the given {@link List}, that should be processed
     * @param <T> type of elements in allWork
     * @return List of n sublists of allWork
     */
    private <T> List<List<? extends T>> divideWork(int n, List<? extends T> allWork) {
        List<List<? extends T>> parts = new ArrayList<>();
        int fullSize = allWork.size();
        int partSize = fullSize / n;
        int tail = fullSize % n;
        int partsNumber = n;
        if (n > fullSize) {
            partSize = 1;
            partsNumber = allWork.size();
            tail = 0;
        }
        int tailPiecesCounter = 0;
        int a = 0, b = partSize;
        for (int i = 0; i < partsNumber; i++) {
            if (tailPiecesCounter < tail) {
                tailPiecesCounter++;
                b++;
            }
            parts.add(allWork.subList(a, b));
            a = b;
            b += partSize;
        }
        return parts;
    }

    /**
     * Given n threads to apply {@link Function} operation to the whole {@link List} list
     * Divides list into n equal sublists
     * Shares processing of each sublist among different threads
     *
     * @param n the number of threads to be created to process every sublist
     * @param list the whole {@link List} of elements, that we apply {@link Function} operation to
     * @param operation {@link Function} that we aply to elemts of the list
     * @param mergeResults {@link Function} that gets the final result
     * @param <T> type of processed elements
     * @param <R> type of result
     * @return the result of function application to the list
     * @throws InterruptedException
     */
    private <T, R> R parallelFactory(int n, List<? extends T> list,
                                 Function<List<? extends T>, R> operation,
                                 Function<List<R>, R> mergeResults) throws InterruptedException {

        List<List<? extends T>> parts = divideWork(n, list);
        if (mapper != null) {
            return mergeResults.apply(mapper.map(operation, parts));
        } else {
            List<Thread> threads = new ArrayList<>();
            List<R> result = new ArrayList<>(Collections.nCopies(parts.size(), null));
            for (int i = 0; i < parts.size(); i++) {
                final int index = i;
                List<? extends T> part = parts.get(index);
                Thread thread = new Thread(() -> result.set(index, operation.apply(part)));
                threads.add(thread);
                thread.start();
            }
            for (Thread thread : threads) {
                thread.join();
            }
            return mergeResults.apply(result);
        }
    }

    /**
     *
     * @param threads the number of threads to process the list
     * @param list the processed list
     * @param comparator {@link Comparator} for getting minimum of the elements in the list
     * @param <T> type of processed elements
     * @return minimum of the elements in he list
     * @throws InterruptedException
     */
    @Override
    public <T> T minimum(int threads, List<? extends T> list, Comparator<? super T> comparator) throws InterruptedException {
        return parallelFactory(threads, list, part -> Collections.min(part, comparator), results -> Collections.min(results, comparator));
    }

    /**
     * @param threads the number of threads to process the list
     * @param list the processed list
     * @param comparator {@link Comparator} for getting maximum of the elements in the list
     * @param <T> type of processed elements
     * @return maximum of the elements in he list
     * @throws InterruptedException
     */
    @Override
    public <T> T maximum(int threads, List<? extends T> list, Comparator<? super T> comparator) throws InterruptedException {
        return parallelFactory(threads, list, part -> Collections.max(part, comparator), results -> Collections.max(results, comparator));
    }

    /**
     * @param threads the number of threads to process the list
     * @param list the processed list
     * @param predicate we check weather all elements of the list satisfy {@link Predicate} predicate
     * @param <T> type f processed elements
     * @return true if all elemensts of the list satisfy predicate, false otherwise
     * @throws InterruptedException
     */
    @Override
    public <T> boolean all(int threads, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        return parallelFactory(threads, list, part -> part.stream().allMatch(predicate), results -> results.stream().allMatch(Predicate.isEqual(true)));
    }

    /**
     * @param threads the number of threads to process the list
     * @param list the processed list
     * @param predicate we check weather there exists at least one element in the list, that satisfy {@link Predicate} predicate
     * @param <T> type f processed elements
     * @return true if there exists such element, false otherwise
     * @throws InterruptedException
     */
    @Override
    public <T> boolean any(int threads, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        return parallelFactory(threads, list, part -> part.stream().anyMatch(predicate), results -> results.stream().anyMatch(Predicate.isEqual(true)));
    }

    /**
     *
     * @param threads the number of threads to process the list
     * @param list the processed list
     * @param predicate filter out which elements of the list satisfy {@link Predicate} predicate
     * @param <T> type of the elements in the list
     * @return the {@link List} f elements, that satisfy the predicate
     * @throws InterruptedException
     */
    @Override
    public <T> List<T> filter(int threads, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        return parallelFactory(threads, list,
                part -> part.stream().filter(predicate).collect(Collectors.toList()),
                results -> results.stream().reduce(new ArrayList<T>(), (acc, niceElements) -> {
                    acc.addAll(niceElements);
                    return acc;
                }));
    }

    /**
     *
     * @param threads the number of threads to process the list
     * @param list the processed list
     * @param function {@link Function} that is applied to the elements in the list
     * @param <T> type of elements in the primary list
     * @param <U> type of the elements after application of the function
     * @return the resulting list after application of the function
     * @throws InterruptedException
     */
    @Override
    public <T, U> List<U> map(int threads, List<? extends T> list, Function<? super T, ? extends U> function) throws InterruptedException {
        return parallelFactory(threads, list,
                part -> part.stream().map(function).collect(Collectors.toList()),
                results -> results.stream().reduce(new ArrayList<U>(), (acc, processedElements) -> {
                    acc.addAll(processedElements);
                    return acc;
                }));
    }

    /**
     *
     * @param threads the number of threads to process the list
     * @param list the processed list
     * @return the concatenation of {@link String} representations of elements in the list
     * @throws InterruptedException
     */
    @Override
    public String join(int threads, List<?> list) throws InterruptedException {
        List<String> representations = map(threads, list, Object::toString);
        StringBuilder result = new StringBuilder();
        representations.forEach(result::append);
        return result.toString();
    }
}

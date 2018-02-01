package ru.ifmo.ctddev.sokolova.concurrent;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.nCopies;

/**
 * Created by maria on 27.03.17.
 */
public class Applier<R> {
    private final List<R> results;
    private int c = 0;

    /**
     * Creates the empty {@link List} results to store the results of function application
     * @param size the size of results
     */
    public Applier(int size) {
        this.results = new ArrayList<>(nCopies(size, null));
    }

    /**
     * Sets value res to element of results at position pos
     * If computation of results is completed then notify()
     *
     * @param pos - the position of the element
     * @param res - the value to be set to that element
     */
    synchronized void setResult(int pos, R res) {
        synchronized (this) {
            results.set(pos, res);
            c++;
            if (c == results.size()) {
                this.notify();
            }
        }
    }

    /**
     * Passive wait until computation of results is completed and we can get them (waiting for the notify, that can be sent from method setResult)
     *
     * @return processed {@link List} when ready
     * @throws InterruptedException
     */
    synchronized List<R> getResults() throws InterruptedException {
        synchronized (this) {
            while (c < results.size()) {
                this.wait();
            }
            return results;
        }
    }
}

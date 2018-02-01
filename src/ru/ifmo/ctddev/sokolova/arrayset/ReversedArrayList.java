package ru.ifmo.ctddev.sokolova.arrayset;

import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Created by maria on 06.03.17.
 */
public class ReversedArrayList<E> extends AbstractList<E> {
    private List<E> reversedList;

    public ReversedArrayList(List<E> reversedList) {
        this.reversedList = reversedList;
    }

    @Override
    public int size() {
        return reversedList.size();
    }

    @Override
    public E get(int index) {
        return reversedList.get(size() - index - 1);
    }

    @Override
    public Iterator<E> iterator() {
        return new Iterator<E>() {
            int it = 0;

            @Override
            public boolean hasNext() {
                return it != size();
            }

            @Override
            public E next() {
                if (it >= size()) {
                    throw new NoSuchElementException();
                }
                return get(it++);
            }
        };
    }
}

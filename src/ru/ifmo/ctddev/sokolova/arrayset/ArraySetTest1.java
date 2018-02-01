package ru.ifmo.ctddev.sokolova.arrayset;

import java.util.*;

/**
 * Created by maria on 18.02.17.
 */
public class ArraySetTest1<E> extends AbstractSet<E> implements java.util.NavigableSet<E> {

    private List<E> objects;
    private final Comparator<? super E> comparator;
    boolean reversed = false;

    public ArraySetTest1() {
        this.comparator = null;
        this.objects = new ArrayList<>();
    }

    public ArraySetTest1(Collection<E> collection) {
        comparator = null;
        removeDuplicates(collection);
    }

    public ArraySetTest1(final Collection<E> collection, Comparator<? super E> comparator) {
        this.comparator = comparator;
        removeDuplicates(collection);
    }

    private void removeDuplicates(Collection<E> collection) {
        this.objects = new ArrayList<>();
        for (E e : collection) {
            if (objects.size() == 0) {
                objects.add(e);
            } else {
                if (compare(objects.get(objects.size() - 1), e) != 0) {
                    objects.add(e);
                }
            }
        }
    }

    private ArraySetTest1(List<E> list, boolean reversed, Comparator<? super E> comparator) {
        this.objects = list;
        this.reversed = reversed;
        this.comparator = comparator;
    }

    private ArraySetTest1<E> reverse() {
        if (comparator == null) {
            return new ArraySetTest1<>(objects, !reversed, (Comparator<E>) Comparator.naturalOrder().reversed());
        }
        return new ArraySetTest1<>(objects, !reversed, comparator.reversed());
    }

    @SuppressWarnings("unchecked")
    private int compare(E e1, E e2) {
        if (comparator == null) {
            return ((Comparable<E>) e1).compareTo(e2);
        } else {
            return comparator.compare(e1, e2);
        }
    }

    @Override
    public Comparator<? super E> comparator() {
        return comparator;
    }

    private E get(int pos) {
        if (pos >= size()) {
            throw new IndexOutOfBoundsException();
        }
        return objects.get(pos);
    }

    private int equalOrGreater(E e) throws NullPointerException {
        if (e == null) {
            throw new NullPointerException();
        }
        /**
         * Returns the index of the search key, if it is contained in the list
         * otherwise, (-(insertion point) - 1)
         * insertion point - the index of the first element greater than the key, or list.size()
         */
        int pos = Collections.binarySearch(objects, e, comparator);
        return (pos >= 0) ? pos : (-pos - 1);
    }

    @Override
    public E first() {
        try {
            return get(0);
        }
        catch (IndexOutOfBoundsException e) {
            throw new NoSuchElementException();
        }
    }

    @Override
    public E last() {
        try {
            return get(size() - 1);
        }
        catch (IndexOutOfBoundsException e) {
            throw new NoSuchElementException();
        }
    }

    @Override
    public int size() {
        return objects == null ? 0 : objects.size();
    }

    @SuppressWarnings("unchecked")
    public boolean contains(Object o) {
        return (Collections.binarySearch(objects, (E)o, comparator) >= 0);
    }

    /**
     * Returns the greatest element in this set strictly less than the given element,
     * or null if there is no such element.
     */
    @Override
    public E lower(E e) throws NullPointerException {
        int pos  = equalOrGreater(e);
        return (pos > 0) ? objects.get(pos - 1) : null;
    }

    /**
     * Returns the greatest element in this set less than or equal to the given element,
     * or null if there is no such element.
     */
    @Override
    public E floor(E e) throws NullPointerException {
        int pos  = equalOrGreater(e);
        if (pos < objects.size() && compare(objects.get(pos), e) == 0) {
            return objects.get(pos);
        }
        if (pos > 0) return objects.get(pos - 1);
        return null;
    }

    /**
     * Returns the least element in this set greater than or equal to the given element,
     * or null if there is no such element.
     */
    @Override
    public E ceiling(E e) throws NullPointerException {
        int pos  = equalOrGreater(e);
        return (pos == objects.size()) ? null : objects.get(pos);
    }

    /**
     * Returns the least element in this set strictly greater than the given element,
     * or null if there is no such element.
     */
    @Override
    public E higher(E e) throws NullPointerException {
        int pos  = equalOrGreater(e);
        if (pos < objects.size() && compare(objects.get(pos), e) == 0) {
            pos++;
        }
        return (pos == objects.size()) ? null : objects.get(pos);
    }

    @Override
    public E pollFirst() {
        throw new  UnsupportedOperationException();
    }

    @Override
    public E pollLast() {
        throw new  UnsupportedOperationException();
    }


    @Override
    public Iterator<E> iterator() {
        return new Iterator<E>() {
            private int pos = 0;

            @Override
            public boolean hasNext() {
                return pos < objects.size();
            }

            @Override
            public E next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return objects.get(pos++);
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }


    @Override
    @SuppressWarnings("unchecked")
    public java.util.NavigableSet<E> descendingSet() {
        return this.reverse();
    }

    @Override
    public Iterator<E> descendingIterator() {
        return new Iterator<E>() {
            int pos = objects.size();

            @Override
            public boolean hasNext() {
                return (pos > 0);
            }

            @Override
            public E next() {
                return objects.get(--pos);
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
    public java.util.NavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) throws NullPointerException {
        return headSet(toElement, toInclusive).tailSet(fromElement, fromInclusive);
    }

    /**
     * Returns a view of the portion of this set whose elements are less than
     * (or equal to, if inclusive is true) toElement.
     */
    @Override
    public java.util.NavigableSet<E> headSet(E toElement, boolean toInclusive) throws NullPointerException {
      int to = equalOrGreater(toElement);
      if (toInclusive && (to < objects.size()) && (compare(objects.get(to), toElement) == 0)) to++;
      return new ArraySetTest1<>(objects.subList(0, to), comparator);
    }

    /**
     * Returns a view of the portion of this set whose elements are greater than
     * (or equal to, if inclusive is true) fromElement.
     */
    @Override
    public java.util.NavigableSet<E> tailSet(E fromElement, boolean fromInclusive) throws NullPointerException {
      int from = equalOrGreater(fromElement);
      if (!fromInclusive && (from < objects.size()) && (compare(objects.get(from), fromElement) == 0)) {
          from++;
      }
      return new ArraySetTest1<>(objects.subList(from, objects.size()), comparator);
    }

    @Override
    public SortedSet<E> subSet(E fromElement, E toElement) {
        return subSet(fromElement, true, toElement, false);
    }

    @Override
    public SortedSet<E> headSet(E toElement) {
        return headSet(toElement, false);
    }

    @Override
    public SortedSet<E> tailSet(E fromElement) {
        return tailSet(fromElement, true);
    }
}

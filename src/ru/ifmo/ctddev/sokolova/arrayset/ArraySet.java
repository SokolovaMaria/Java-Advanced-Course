package ru.ifmo.ctddev.sokolova.arrayset;

import java.util.*;


public class ArraySet<E> extends AbstractSet<E> implements NavigableSet<E> {

    private List<E> elements;
    private Comparator<? super E> comparator;


    public ArraySet(final Collection<? extends E> collection, Comparator<? super E> comparator) {
        this.comparator = comparator;
        removeDuplicates(collection);
    }

    private void removeDuplicates(Collection<? extends E> collection) {
        this.elements = new ArrayList<>();
        ArrayList<E> list = new ArrayList<>(collection);
        Collections.sort(list, comparator);
        for (E e : list) {
            if (elements.size() == 0) {
                elements.add(e);
            } else {
                if (!equal(elements.get(elements.size() - 1), e)) {
                    elements.add(e);
                }
            }
        }
    }

    private boolean equal (E o1, E o2) {
        if (o1 == null || o2 == null) {
            return (o1 == null && o2 == null);
        }
        if (comparator == null) {
            return o1.equals(o2);
        }
        return comparator.compare(o1, o2) == 0;
    }

    public ArraySet() {
        this(Collections.emptyList(), null);
    }

    public ArraySet(Collection<? extends E> elements) {
        this(elements, null);
    }

    public ArraySet(Comparator<? super E> comparator) {
        this(Collections.emptyList(), comparator);
    }

    private ArraySet(List<E> elements, Comparator<? super E> comparator) {
        this.elements = elements;
        this.comparator = comparator;
    }

    public int getPos(E e, int signType, boolean canEqual) {
        boolean contained = true;
        int ind = Collections.binarySearch(elements, e, comparator);
        if (ind < 0) {
            contained = false;
            ind = -ind - 1;
        }

        if (signType == -1) {
            if (canEqual) {
                if (!contained) {
                    ind--;
                }
            } else {
                ind--;
            }
        } else {
            if (!canEqual) {
                if (contained) {
                    ind++;
                }
            }
        }
        return ind;
    }

    @Override
    public E lower(E e) {
        int ind = getPos(e, -1, false);
        return (ind >= 0 && ind < elements.size() ? elements.get(ind) : null);
    }

    @Override
    public E floor(E e) {
        int ind = getPos(e, -1, true);
        return (ind >= 0 && ind < elements.size() ? elements.get(ind) : null);
    }

    @Override
    public E ceiling(E e) {
        int ind = getPos(e, 1, true);
        return (ind >= 0 && ind < elements.size() ? elements.get(ind) : null);
    }

    @Override
    public E higher(E e) {
        int ind = getPos(e, 1, false);
        return (ind >= 0 && ind < elements.size() ? elements.get(ind) : null);
    }

    @Override
    public int size() {
        return elements.size();
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean contains(Object o) {
        return Collections.binarySearch(elements, (E)o, comparator) >= 0;
    }

    @Override
    public Iterator<E> iterator() {
        return new Iterator<E>() {
            Iterator<E> it = elements.iterator();

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public E next() {
                return it.next();
            }
        };
    }

    @Override
    public NavigableSet<E> descendingSet() {
        List<E> descendingSet = new ReversedArrayList<>(elements);
        return new ArraySet<>(descendingSet, comparator.reversed());
    }

    @Override
    public Iterator<E> descendingIterator() {
        return descendingSet().iterator();
    }

    @Override
    public NavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
        if (size() == 0) {
            return new ArraySet<>(comparator);
        }

        int from = getPos(fromElement, 1, fromInclusive);
        int to = getPos(toElement, -1, toInclusive);

        if (from == -1 || to == -1 || from > to) {
            return new ArraySet<>(comparator);
        }
        return new ArraySet<>(elements.subList(from, to + 1), comparator);
    }

    @Override
    public NavigableSet<E> headSet(E toElement, boolean inclusive) {
        if (size() == 0) {
            return new ArraySet<>(comparator);
        }
        return subSet(first(), true, toElement, inclusive);
    }

    @Override
    public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
        if (size() == 0) {
            return new ArraySet<>(comparator);
        }
        return subSet(fromElement, inclusive, last(), true);
    }

    @Override
    public Comparator<? super E> comparator() {
        return comparator;
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

    @Override
    public E first() {
        if (elements.size() == 0) {
            throw new NoSuchElementException();
        }
        return elements.get(0);
    }

    @Override
    public E last() {
        if (elements.size() == 0) {
            throw new NoSuchElementException();
        }
        return elements.get(elements.size() - 1);
    }

    // Unsupported operations
    @Override
    public E pollFirst() {
        throw new UnsupportedOperationException();
    }

    @Override
    public E pollLast() {
        throw new UnsupportedOperationException();
    }
}
package com.crotaplague.torture.Files.ServerStorage.ArbitraryClasses;

import java.util.LinkedList;
import java.util.NoSuchElementException;

public class CQueue<T> {
    private LinkedList<T> list;

    public CQueue() {
        list = new LinkedList<>();
    }

    /**
     * Adds an element to the end of the queue.
     * @param item the element to add
     */
    public void offer(T item) {
        list.addLast(item);
    }

    /**
     * Removes and returns the head of the queue.
     * @return the head element
     * @throws NoSuchElementException if the queue is empty
     */
    public T poll() {
        if (isEmpty()) {
            throw new NoSuchElementException("Queue is empty");
        }
        return list.removeFirst();
    }

    /**
     * Retrieves, but does not remove, the head of the queue.
     * @return the head element
     * @throws NoSuchElementException if the queue is empty
     */
    public T peek() {
        if (isEmpty()) {
            throw new NoSuchElementException("Queue is empty");
        }
        return list.getFirst();
    }

    /**
     * Returns the number of elements in the queue.
     * @return size of the queue
     */
    public int size() {
        return list.size();
    }

    /**
     * Returns true if the queue is empty.
     * @return true if empty, false otherwise
     */
    public boolean isEmpty() {
        return list.isEmpty();
    }

    /**
     * clears the queue
     */
    public void clear() {
        list.clear();
    }

}

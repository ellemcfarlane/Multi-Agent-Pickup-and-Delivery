package com.smac.mas.mapf;

public class DoubleLinkList<T> {

    private class Node {
        T data;
        Node prev;
        Node next;

        public Node(T data) {
            this.data = data;
            this.prev = null;
            this.next = null;
        }
    }

    private Node head;
    private Node tail;

    private Node current; // is used in iterations
    private int currentIndex;

    private int size;

    public DoubleLinkList() {
        this.head = null;
        this.tail = null;
        this.current = null;
        this.currentIndex = -1;
        this.size = 0;
    }

    public boolean hasFirst() {
        return head != null;
    }

    public boolean hasNext() {
        return current != null && current.next != null;
    }

    /*
     * This function sets current on this link list to head. This can be then
     * iterated using next
     * function.
     */
    public T getFirst() throws Exception {

        if (head == null) {
            throw new Exception("Cannot get element on an empty array!");
        }

        current = head;
        return current.data;

    }

    public T next() throws Exception {

        if (current.next == null) {
            throw new Exception("Cannot get next element because he does not exist!");
        }

        current = current.next;
        return current.data;

    }

    public void addFirst(T data) {
        Node newNode = new Node(data);

        if (head == null) {
            head = newNode;
            tail = newNode;
        } else {
            newNode.next = head;
            head.prev = newNode;
            head = newNode;
        }

        size++;
    }

    /*
     * Equiv. to addLast
     */
    public void add(T data) {
        Node newNode = new Node(data);

        if (tail == null) {
            head = newNode;
            tail = newNode;
        } else {
            newNode.prev = tail;
            tail.next = newNode;
            tail = newNode;
        }

        size++;
    }

    // public void add(int index, T data) {
    // if (index < 0 || index > size) {
    // throw new IndexOutOfBoundsException();
    // }

    // if (index == 0) {
    // addFirst(data);
    // } else if (index == size) {
    // addLast(data);
    // } else {
    // Node current = head;

    // for (int i = 0; i < index - 1; i++) {
    // current = current.next;
    // }

    // Node newNode = new Node(data);
    // newNode.prev = current;
    // newNode.next = current.next;
    // current.next.prev = newNode;
    // current.next = newNode;

    // size++;
    // }
    // }

    public T removeFirst() {
        if (head == null) {
            throw new IndexOutOfBoundsException("Cannot remove element on an empty array!");
        }

        T data = head.data;
        head = head.next;

        if (head != null) {
            head.prev = null;
        } else {
            tail = null;
        }

        size--;

        return data;
    }

    public T removeLast() {
        if (tail == null) {
            throw new IndexOutOfBoundsException("Cannot remove element on an empty array!");
        }

        T data = tail.data;
        tail = tail.prev;

        if (tail != null) {
            tail.next = null;
        } else {
            head = null;
        }

        size--;

        return data;
    }

    public T remove(int index) throws Exception {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException();
        }

        if (index == 0) {
            return removeFirst();
        } else if (index == size - 1) {
            return removeLast();
        } else {
            Node current = head;

            for (int i = 0; i < index; i++) {
                current = current.next;
            }

            T data = current.data;
            current.prev.next = current.next;
            current.next.prev = current.prev;

            size--;

            return data;
        }
    }

    public T getCurrent() {
        if (current != null)
            return current.data;

        return null;
    }

    /*
     * This function operates in O(1) in case of index being 1 larger than previous
     * call OR in case
     * of index == 0. If not then O(n) is called.
     * 
     */
    public T get(int index) {

        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException();
        }

        if (index == 0) { // head - set its current to head

            current = head;
            currentIndex = 0;
            return current.data;
        } else if (index == currentIndex + 1) { // is +1 by previous

            current = current.next;
            currentIndex += 1;
            return current.data;
        }

        throw new IndexOutOfBoundsException(
                "Warning, this call takes O(n) (call getSlow() function) and might have O(1)! Look at DoubleLinkList.");

    }

    public T getSlow(int index) {

        Node current = head;

        for (int i = 0; i < index; i++) {
            current = current.next;
        }

        return current.data;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void clear() {
        head = null;
        tail = null;
        size = 0;
    }

    // create clone() method
    public DoubleLinkList<T> clone() {
        DoubleLinkList<T> clone = new DoubleLinkList<T>();
        if (head == null) {
            return clone;
        }
        clone.addFirst(head.data);
        Node current = head.next;

        while (current != null) {
            clone.add(current.data);
            current = current.next;
        }

        return clone;

    }
}

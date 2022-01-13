package com.nowandfuture.translation.core;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.locks.ReentrantLock;

public class FixedSizeBlockingDeque<E> extends LinkedBlockingDeque<E> {

    public FixedSizeBlockingDeque(int i) {
        super(i);
    }

    @Override
    public boolean offerLast(E e) {
        while (remainingCapacity() <= 0){
            removeFirst();
        }
        return super.offerLast(e);
    }
}

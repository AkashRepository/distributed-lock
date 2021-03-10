package com.akash.lock;

public abstract class AbstractLockTemplate implements Lock {

    @Override
    public final void lock() throws Exception {
        if(tryLock()){
            System.out.println("Lock acquired by "+Thread.currentThread().getName());
        } else {
            waitForLock();
            lock();
        }
    }

    protected abstract void waitForLock();

    protected abstract boolean tryLock();

    @Override
    public abstract void unlock() throws Exception;
}

package com.akash.lock;

public interface Lock {

    void lock() throws Exception;
    void unlock() throws Exception;

}

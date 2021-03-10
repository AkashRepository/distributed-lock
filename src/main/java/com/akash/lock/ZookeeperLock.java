package com.akash.lock;

import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class ZookeeperLock extends AbstractLockTemplate {

    private ZkClient zkClient;
    private final String lockPath = "/lock";
    private String currentPath;
    private String previous;

    public ZookeeperLock(final String zkserver, final int connectionTimeout) {
        this.zkClient = new ZkClient(zkserver, connectionTimeout);
        if (!zkClient.exists(lockPath)) {
            zkClient.createPersistent(lockPath);
        }
    }

    @Override
    protected boolean tryLock() {
        if (currentPath == null) {
            currentPath = zkClient.createEphemeralSequential(lockPath + "./", "data");
        }

        List<String> children = zkClient.getChildren(lockPath);
        Collections.sort(children);
        if (currentPath.equals(lockPath + "/" + children.get(0))) {
            // check whether I am the latest one
            return true;
        } else {
            // if not the latest one then which number am I ?
            int currentPosition = children.indexOf(currentPath.substring(lockPath.length() + 1));
            previous = lockPath + "/" + children.get(currentPosition - 1);
        }
        return false;
    }


    @Override
    protected void waitForLock() {
        CountDownLatch lock = new CountDownLatch(1);
        // look at the previous path for deletion

        IZkDataListener listener = new IZkDataListener() {
            @Override
            public void handleDataChange(String s, Object o) throws Exception {

            }

            @Override
            public void handleDataDeleted(String s) throws Exception {
                System.out.println("Listening to data being deleted.");
                lock.countDown();
            }
        };
        zkClient.subscribeDataChanges(previous, listener);

        if (zkClient.exists(previous)) {
            try {
                lock.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        zkClient.unsubscribeDataChanges(previous, listener);
    }

    @Override
    public void unlock() throws Exception {
        zkClient.delete(currentPath);
    }
}

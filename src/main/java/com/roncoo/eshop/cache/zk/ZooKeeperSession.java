package com.roncoo.eshop.cache.zk;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * @author yangfan
 * @date 2018/02/21
 */
public class ZooKeeperSession {

    private static CountDownLatch connectedSemaphore = new CountDownLatch(1);
    private ZooKeeper zookeeper;

    public ZooKeeperSession() {
        // 去连接zookeeper server，是异步创建会话的，所以要给一个监听器告诉我们什么时候才真正的完成了zk serve的连接
        try {
            this.zookeeper = new ZooKeeper(
                    "192.168.2.201:2181,192.168.2.202:2181,192.168.2.203:2181",
                    50000,
                    event -> {
                        System.out.println("Receive watched event: " + event.getState());
                        if (Watcher.Event.KeeperState.SyncConnected == event.getState()) {
                            connectedSemaphore.countDown();
                        }
                    }
            );

            // 给一个状态CONNECTING，连接中
            System.out.println(zookeeper.getState());

            try {
                // 等待zookeeper连接
                // java多线程并发同步的一个工具类
                // 会传递进去一些数字，比如说1,2 ，3 都可以
                // 然后await()，如果数字不是0，那么久卡住，等待

                // 其他的线程可以调用coutnDown()，减1
                // 如果数字减到0，那么之前所有在await的线程，都会逃出阻塞的状态
                // 继续向下运行
                connectedSemaphore.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


            System.out.println("ZooKeeper session established......");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 获取分布式锁
     * <p>
     * 自旋加锁
     *
     * @param productId
     */
    public void acquireDistributedLock(Long productId) {
        String path = "/product-lock-" + productId;

        try {
            // 临时节点，空数据
            zookeeper.create(path, "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
            System.out.println("success to acquire lock for product [id=" + productId + "]");
        } catch (Exception e) {
            // 如果那个商品对应的锁的node，已经存在了，就是已经被别人加锁了，那么就这里就会报错
            // NodeExistsException
//            e.printStackTrace();

            int count = 0;
            while (true) {
                try {
                    Thread.sleep(1000);

                    zookeeper.create(path, "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
                } catch (Exception e1) {
//                    e1.printStackTrace();
                    count++;
                    System.out.println("the " + count + " times try to acquire lock for product[id=" + productId + "]....");
                    continue;
                }

                System.out.println("success to acquire lock for product [id=" + productId + "] after " + count + " times try....");
                break;
            }
        }
    }


    /**
     * 释放分布式锁
     *
     * @param productId
     */
    public void releaseDistributedLock(Long productId) {
        String path = "/product-lock-" + productId;

        try {
            zookeeper.delete(path, -1);
            System.out.println("release the lock for product[id=" + productId + "]");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 封装单例的静态内部类
     */
    private static class Singleton {
        private static ZooKeeperSession instance;

        static {
            instance = new ZooKeeperSession();
        }

        public static ZooKeeperSession getInstance() {
            return instance;
        }
    }


    /**
     * 获取单例
     *
     * @return
     */
    public static ZooKeeperSession getInstance() {
        return Singleton.getInstance();
    }

    /**
     * 初始单例的便捷方法
     */
    public static void init() {
        getInstance();
    }

}

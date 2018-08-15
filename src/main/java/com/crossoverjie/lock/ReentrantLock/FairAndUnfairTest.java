package com.crossoverjie.lock.ReentrantLock;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

//ReentrantLock 公平锁与非公平所的效率问题
public class FairAndUnfairTest {
    private static Lock fairLock = new Reentrantlock2(true);
    private static Lock unfairLock  = new Reentrantlock2(false);

    @Test
    public void fair(){
        System.out.println("fair");
        testLock(fairLock);
    }

    @Test
    public void unfair(){
        System.out.println("unfair");
        testLock(unfairLock);
    }

    private static void testLock(Lock lock){
        //启动五个线程
        for(int i = 0;i<5;i++){
            Job job = new Job(lock);
            job.start();
        }
        try {
            Thread.sleep(11000);  //JUNIT测试 所以给五个线程足够的时间来执行完毕
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static class Job extends  Thread{
        private Lock lock;
        public  Job(Lock lock){
            this.lock = lock;
        }
        public void run(){
            for (int i = 0; i < 2; i++) {
                lock.lock();
                try {
                    Thread.sleep(1000);
                    System.out.println("获取锁的当前线程[" + Thread.currentThread().getName() + "], 同步队列中的线程" + ((Reentrantlock2)lock).getQueuedThreads() + "");
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    lock.unlock();
                }
            }
        }
    }

    private static class Reentrantlock2 extends ReentrantLock {
        public Reentrantlock2(boolean fair){
            super(fair);
        }
        @Override
        public Collection<Thread> getQueuedThreads(){
            List<Thread> arrayList = new ArrayList<>(super.getQueuedThreads());
            Collections.reverse(arrayList);
            return arrayList;
        }
    }
}

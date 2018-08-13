package com.crossoverjie.lock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 *  Mutex
 *  一个自定义同步组件，在同一时刻只允许一个线程占有锁
 */
public class Mutex implements Lock {
    //静态内部类
    private static class Sync extends AbstractQueuedSynchronizer{
        //是否处于占用状态
        protected boolean isHeldExclusively(){
            return getState() ==1;
        }
        //当状态为0时获取锁
        public boolean tryAcquire(int acquires){
            if(compareAndSetState(0,1)){
                setExclusiveOwnerThread(Thread.currentThread());
                return true;
            }
            return false;
        }

        //释放锁，将状态设置为0
        protected boolean tryRelease(int release){
            if(getState()==0) throw new IllegalMonitorStateException();
            setExclusiveOwnerThread(null);
            setState(0);
            return true;
        }
        //返回一个Condition，每个condition都包含一个Condition队列
        Condition newCondition(){return new ConditionObject();}
    }

    //仅需要将操作代理到Sync上即可
    private final Sync sync = new Sync();
    public void lock(){sync.acquire(1);}
    public boolean tryLock(){return sync.tryAcquire(1);}
    public void unlock(){sync.release(1);}
    public Condition newCondition(){return sync.newCondition();}
    public boolean isLocked(){return sync.isHeldExclusively();}
    public boolean hasQueuedThreads(){return sync.hasQueuedThreads();}
    public void lockInterruptibly() throws  InterruptedException{
        sync.acquireInterruptibly(1);
    }
    public boolean tryLock(long timeout, TimeUnit unit) throws InterruptedException {
        return sync.tryAcquireSharedNanos(1,unit.toNanos(timeout));
    }
}

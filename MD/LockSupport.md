# 线程阻塞工具类：LockSupport

LockSupport是一个非常方便实用的线程阻塞工具，他可以在线程内任意位置让线程阻塞。。和 Thread.suspend()相比，它弥补了由于 resume() 在前发生，导致线程无法继续执行的情况。和 Object.wait() 相比，他不需要先获得某个对象的锁，也不会抛出InterruptedException异常。

LockSupport的静态方法 park() 可以阻塞当前线程，类似的还有parkNanos(),parkUntil()等方法。他们实现了一个限时的等待。

LockSupport类使用类似信号量机制。它为每一个线程准备了一个许可。如果许可可用，那么 park() 函数会立即返回，并且消费这个许可（也就是将许可变为不可用），如果许可不可用，就会阻塞。而 unpark() 则使得一个许可变为可用（但是和信号量不同的是，许可不能累加，你不可能拥有超过一个许可，他永远只有一个）。

这个使得：即使 unpark() 操作发生在 park() 之前，他也可以使下一次的 park() 操作立即返回。

除了有定时阻塞的功能外，LockSupport.park()还能支持中断影响。但是和其他接收中断的函数很不一样，LockSupport.park()不会抛出InterruptException异常。他只是会默默的返回，但是我们可以从Thread.interruptd()等方法获得中断标记。 

```java
public class LockSupportDemo {
    public static Object u = new Object();
    static ChangeObjectThread t1 = new ChangeObjectThread("t1");
    static ChangeObjectThread t2 = new ChangeObjectThread("t2");

    public static class ChangeObjectThread extends Thread{
        public ChangeObjectThread(String name){
            super.setName(name);
        }
        @Override
        public void run(){
            synchronized (u){
                System.out.println("in "+getName());
                LockSupport.park();
                if(Thread.interrupted()){
                    System.out.println(getName()+" 被中断了");
                }
            }
            System.out.println(getName()+" 执行结束");
        }
    }

    public static void main(String args[]) throws InterruptedException {
        t1.start();;
        Thread.sleep(100);
        t2.start();
        t1.interrupt();
        LockSupport.unpark(t2);
    }
}
```

```
in t1
t1 被中断了
t1 执行结束
in t2
t2 执行结束
```
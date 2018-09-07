# 深入理解线程通信

## 前言

在并发编程中，需要处理两个关键问题：线程之间如何通信及线程之间如何同步（这里的线程是指并发执行的活动实体）。

>同步是指程序中用于控制不同线程之间操作发生相对顺序的机制。

线程之间的通信机制有两种：共享内存和消息传递。

java的并发采用的是共享内存模型，java线程之间的通信都是隐式进行的。

所有的实例域，静态域和数组元素都存储在堆内存当中，堆内存在线程之间共享。局部变量，方法定义参数和异常处理参数不会再线程之间共享，他们不会有内存可见性问题。

java线程之间的通信室友Java内存模型JMM控制的。

开发中不免会遇到需要所有子线程执行完毕通知主线程处理某些逻辑的场景。

或者是线程 A 在执行到某个条件通知线程 B 执行某个操作。

可以通过以下几种方式实现：


## 等待通知机制
> 等待通知模式是 Java 中比较经典的线程通信方式。

两个线程通过对同一对象调用等待 wait() 和通知 notify() 方法来进行通讯。

如两个线程交替打印奇偶数：

```java
public class TwoThreadWaitNotify {

    private int start = 1;

    private boolean flag = false;

    public static void main(String[] args) {
        TwoThreadWaitNotify twoThread = new TwoThreadWaitNotify();

        Thread t1 = new Thread(new OuNum(twoThread));
        t1.setName("A");


        Thread t2 = new Thread(new JiNum(twoThread));
        t2.setName("B");

        t1.start();
        t2.start();
    }

    /**
     * 偶数线程
     */
    public static class OuNum implements Runnable {
        private TwoThreadWaitNotify number;

        public OuNum(TwoThreadWaitNotify number) {
            this.number = number;
        }

        @Override
        public void run() {

            while (number.start <= 100) {
                synchronized (TwoThreadWaitNotify.class) {
                    System.out.println("偶数线程抢到锁了");
                    if (number.flag) {
                        System.out.println(Thread.currentThread().getName() + "+-+偶数" + number.start);
                        number.start++;

                        number.flag = false;
                        TwoThreadWaitNotify.class.notify();

                    }else {
                        try {
                            TwoThreadWaitNotify.class.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }
        }
    }


    /**
     * 奇数线程
     */
    public static class JiNum implements Runnable {
        private TwoThreadWaitNotify number;

        public JiNum(TwoThreadWaitNotify number) {
            this.number = number;
        }

        @Override
        public void run() {
            while (number.start <= 100) {
                synchronized (TwoThreadWaitNotify.class) {
                    System.out.println("奇数线程抢到锁了");
                    if (!number.flag) {
                        System.out.println(Thread.currentThread().getName() + "+-+奇数" + number.start);
                        number.start++;

                        number.flag = true;

                        TwoThreadWaitNotify.class.notify();
                    }else {
                        try {
                            TwoThreadWaitNotify.class.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
}
```

输出结果：

```
t2+-+奇数93
t1+-+偶数94
t2+-+奇数95
t1+-+偶数96
t2+-+奇数97
t1+-+偶数98
t2+-+奇数99
t1+-+偶数100
```

这里的线程 A 和线程 B 都对同一个对象 `TwoThreadWaitNotify.class` 获取锁，A 线程调用了同步对象的 wait() 方法释放了锁并进入 `WAITING` 状态。

B 线程调用了 notify() 方法，这样 A 线程收到通知之后就可以从 wait() 方法中返回。

这里利用了 `TwoThreadWaitNotify.class` 对象完成了通信。

有一些需要注意:

- wait() 、nofify() 、nofityAll() 调用的前提都是获得了对象的锁(也可称为对象监视器)。
- 调用 wait() 方法后线程会释放锁，进入 `WAITING` 状态，该线程也会被移动到**等待队列**中。
- 调用 notify() 方法会将**等待队列**中的线程移动到**同步队列**中，线程状态也会更新为 `BLOCKED`
- 从 wait() 方法返回的前提是调用 notify() 方法的线程释放锁，wait() 方法的线程获得锁。

等待通知有着一个经典范式：

线程 A 作为消费者：

1. 获取对象的锁。
2. 进入 while(判断条件)，并调用 wait() 方法。
3. 当条件满足跳出循环执行具体处理逻辑。

线程 B 作为生产者:

1. 获取对象锁。
2. 更改与线程 A 共用的判断条件。
3. 调用 notify() 方法。

伪代码如下:

```
//Thread A

synchronized(Object){
    while(条件){
        Object.wait();
    }
    //do something
}

//Thread B
synchronized(Object){
    条件=false;//改变条件
    Object.notify();
}

```


## join() 方法

```java
    private static void join() throws InterruptedException {
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                LOGGER.info("running");
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }) ;
        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                LOGGER.info("running2");
                try {
                    Thread.sleep(4000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }) ;

        t1.start();
        t2.start();

        //等待线程1终止
        t1.join();

        //等待线程2终止
        t2.join();

        LOGGER.info("main over");
    }
```

输出结果:

```
2018-03-16 20:21:30.967 [Thread-1] INFO  c.c.actual.ThreadCommunication - running2
2018-03-16 20:21:30.967 [Thread-0] INFO  c.c.actual.ThreadCommunication - running
2018-03-16 20:21:34.972 [main] INFO  c.c.actual.ThreadCommunication - main over

```

在  `t1.join()` 时会一直阻塞到 t1 执行完毕，所以最终主线程会等待 t1 和 t2 线程执行完毕。

其实从源码可以看出，join() 也是利用的等待通知机制：

核心逻辑:

```java
    while (isAlive()) {
        wait(0);
    }
```

在 join 线程完成后会调用 notifyAll() 方法，是在 JVM 实现中调用，所以这里看不出来。

## volatile 共享内存

因为 Java 是采用共享内存的方式进行线程通信的，所以可以采用以下方式用主线程关闭 A 线程:

```java
public class Volatile implements Runnable{

    private static volatile boolean flag = true ;

    @Override
    public void run() {
        while (flag){
            System.out.println(Thread.currentThread().getName() + "正在运行。。。");
        }
        System.out.println(Thread.currentThread().getName() +"执行完毕");
    }

    public static void main(String[] args) throws InterruptedException {
        Volatile aVolatile = new Volatile();
        new Thread(aVolatile,"thread A").start();


        System.out.println("main 线程正在运行") ;

        TimeUnit.MILLISECONDS.sleep(100) ;

        aVolatile.stopThread();

    }

    private void stopThread(){
        flag = false ;
    }
}
```

输出结果：
```
thread A正在运行。。。
thread A正在运行。。。
thread A正在运行。。。
thread A正在运行。。。
thread A执行完毕
```

这里的 flag 存放于主内存中，所以主线程和线程 A 都可以看到。

flag 采用 volatile 修饰主要是为了内存可见性，更多内容可以查看[这里](http://crossoverjie.top/2018/03/09/volatile/)。

## Semaphore(信号量)

信号量为多线程协作提供了更加强大的控制方法。广义上来说，信号量是对锁的扩展。他可以指定多个线程，同时访问某一个资源。

其内部维护了一组虚拟的许可，许可的数量可以通过构造函数的参数指定。

```java
public Semaphore(int permits);         //默认使用非公平策略
public Semaphore(int permits,boolean fair);  //第二个参数可以指定是否公平
```

- 访问特定资源之前，必须使用acquire方法获得许可，如果许可数量为0，该线程一直阻塞，直到有可用许可。
- 访问资源后，使用release释放许可。

### 应用场景
Semaphore可以用来做流量分流，特别是对公共资源有限的场景，比如数据库连接。假设有这个需求，读取几万个文件的数据到数据库中，由于文件读取是IO密集型任务，可以启动几十个线程并发读取，但是数据库连接数只有10个，这是就必须控制最多只有10个线程能够拿到数据库连接进行操作。这个时候就可以使用Semaphore做流量控制。

```java
public class SemaphoreTest{
    private static final int COUNT = 40;
    private static Executor executor = Executors.newFixedThreadPool(COUNT);
    private static Semaphore semaphore = new Semaphore(10);
    public static void main(String args[]){
        for(int i = 0;i<COUNT;i++){
            executor.execute(new SemaphoreTest.Task());
        }
        executor.shutdown();
    }

    static class Task implements Runnable{
        @Override
        public void run(){
            try{
                //文件读取操作
                semaphore.acquire();
                System.out.prinln("Save data");
                semaphore.release();
            }catch(InterruptException e){
                e.printStackTrace();
            }finally{

            }
        }
    }
}
```

### 非公平策略
1. acquire实现，核心代码如下：
```java
final int nofairTryAcquireShared(int acquires){
    for(;;){
        int available = getState();
        int remaining = acailable-acquires;
        if(remaining<0||comapreAndSetState(available,remaining))
            return remaining;
    }
}
```
acquires默认值为1，表示尝试获取1个许可，remaining代表剩余的许可数。
 - 如果remaining<0,表示目前没有剩余的许可。
 - 当前线程进入AQS中的doAcquireInterrutibly方法等待可用许可并挂起，知道被唤醒。

2. release实现，核心代码如下：
```java
protected final boolean tryReleaseShared(int releases){
    for(;;){
        int current = getState();
        int next = current+releases;
        if(next<current)   //overflow
            throw new Error("Maximum permit count exceeded");
        if(compareAndSetState(current,next))
            return true;
    }
}
```

releases值默认为1，表示尝试释放1个许可，next表示如果许可释放成功，可用许可的数量。
 - 通过unsafe.compareAndSwapInt修改state的值，确报同一时刻只有一个线程可以释放成功。
 - 许可释放成功，当前线程进入到AQS的doReleaseShared方法，唤醒队列中等待许可的线程。

>非公平性：当一个线程执行acquire方法时，会直接尝试获取许可，而不管同一时刻阻塞队里中是否有线程也在等待许可，如果恰好有线程C执行release释放许可，并唤醒阻塞队列中第一个等待的线程B，这个时候，线程A和线程B是共同竞争可用许可，不公平性就是这么体现出爱的，线程A一点时间都没等待和线程B同等对待。 

### 公平策略
1.acquire实现，核心代码如下：
```
protected int tryAcquireShared(int acquires){
    for(;;){
        if(hasQueuedPredecessors()
            return -1;
        int available  = getState();
        int remaining = available -acquires;
        
    }
}
```

acquires值默认为1，表示尝试获取1个许可，remaining代表剩余的许可数。
可以看到和非公平策略相比，就多了一个阻塞队列的检查。
- 如果阻塞队列没有等待的线程，则参与许可的竞争。
- 否则直接插入到阻塞队列尾节点并挂起，等待被唤醒。

2. release实现和非公平策略一样

### 例子
```java
public class SempDemo implements Runnable{
    final Semphore semp = new Semphore(5);
    @Override
    public void run(){
        try{
            semp.acquire();
            //模拟耗时操作
            Thread.sleep(2000);
            System.out.println(Thread.currentThread().getId()+":done!");
            semp.release();
        }catch(InterruptedException e){
            e.printStackTrace();
        }
    }
    public static void main(String args[]){
        ExecutorService es = Executors.newFixedThreadPool(20);
        final SemapDemo = new SemapDemo();
        for(int i=0;i<20;i++){
            es.execute();
        }
        es.shutdown();
    }
}
```

本例中，同时开启20个线程，系统以5个线程为单位，依次输出带有线程ID的提示文本。

## CountDownLatch 并发工具

CountDownLatch 可以实现 join 相同的功能，但是更加的灵活。允许一个或多个线程等待其他线程完成操作后再执行。

CountDownLatch内部会维护一个初始值为线程数量的计数器，主线程执行await方法，如果计数器大于0，则阻塞等待。当一个线程完成任务后，计数器值减一。当计数器为0时，表示所有的线程已经完成任务，等待的主线程被唤醒继续执行。

![](https://upload-images.jianshu.io/upload_images/2184951-8a570622b8297310.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/353/format/webp)

```java
    private static void countDownLatch() throws Exception{
        int thread = 3 ;
        long start = System.currentTimeMillis();
        final CountDownLatch countDown = new CountDownLatch(thread);
        for (int i= 0 ;i<thread ; i++){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    LOGGER.info("thread run");
                    try {
                        Thread.sleep(2000);
                        countDown.countDown();

                        LOGGER.info("thread end");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }

        countDown.await();
        long stop = System.currentTimeMillis();
        LOGGER.info("main over total time={}",stop-start);
    }
```

输出结果:

```
2018-03-16 20:19:44.126 [Thread-0] INFO  c.c.actual.ThreadCommunication - thread run
2018-03-16 20:19:44.126 [Thread-2] INFO  c.c.actual.ThreadCommunication - thread run
2018-03-16 20:19:44.126 [Thread-1] INFO  c.c.actual.ThreadCommunication - thread run
2018-03-16 20:19:46.136 [Thread-2] INFO  c.c.actual.ThreadCommunication - thread end
2018-03-16 20:19:46.136 [Thread-1] INFO  c.c.actual.ThreadCommunication - thread end
2018-03-16 20:19:46.136 [Thread-0] INFO  c.c.actual.ThreadCommunication - thread end
2018-03-16 20:19:46.136 [main] INFO  c.c.actual.ThreadCommunication - main over total time=2012
```

CountDownLatch 也是基于 AQS(AbstractQueuedSynchronizer) 实现的，更多实现参考 [ReentrantLock 实现原理](http://crossoverjie.top/2018/01/25/ReentrantLock/)

- 初始化一个 CountDownLatch 时告诉并发的线程，然后在每个线程处理完毕之后调用 countDown() 方法。
- 该方法会将 AQS 内置的一个 state 状态 -1 。
- 最终在主线程调用 await() 方法，它会阻塞直到 `state == 0` 的时候返回。

### 实现原理

其内部维护了一个AQS子类，并重写了相关方法。

```java
private static final class Sync extends AbstractQueuedSynchronizer{
    private static final long serialVersionUID = 4982264981922014374L;

        Sync(int count) {
            setState(count);
        }

        int getCount() {
            return getState();
        }

        protected int tryAcquireShared(int acquires) {
            return (getState() == 0) ? 1 : -1;
        }

        protected boolean tryReleaseShared(int releases) {
            // Decrement count; signal when transition to zero
            for (;;) {
                int c = getState();
                if (c == 0)
                    return false;
                int nextc = c-1;
                if (compareAndSetState(c, nextc))
                    return nextc == 0;
            }
        }
}
```

### await实现
主线程执行await方法，tryAcquireShared方法中如果state不等于0，返回-1，则加入到等待队列中，主线程通过LockSupport.park(this)被挂起。

```java
private void doAcquireSharedInterruptibly(int arg)
    throws InterruptedException {
    final Node node = addWaiter(Node.SHARED);
    boolean failed = true;
    try {
        for (;;) {
            final Node p = node.predecessor();
            if (p == head) {
                int r = tryAcquireShared(arg);
                if (r >= 0) {
                    setHeadAndPropagate(node, r);
                    p.next = null; // help GC
                    failed = false;
                    return;
                }
            }
            if (shouldParkAfterFailedAcquire(p, node) &&
                parkAndCheckInterrupt())
                throw new InterruptedException();
        }
    } finally {
        if (failed)
            cancelAcquire(node);
    }
}
```

### countDown实现
countDown方法委托sync实现state的减1操作，即通过unsafe.compareAndSwapInt方法设置state值。

```java
public void countDown(){sync.releaseShared(1);}
```

如果state为0，通过LockSupport.unpark唤醒await方法中挂起的主线程。

```java
private void doReleaseShared(){
    for (;;) {
        Node h = head;
        if (h != null && h != tail) {
            int ws = h.waitStatus;
            if (ws == Node.SIGNAL) {
                if (!compareAndSetWaitStatus(h, Node.SIGNAL, 0))
                    continue;            // loop to recheck cases
                unparkSuccessor(h);
            }
            else if (ws == 0 &&
                     !compareAndSetWaitStatus(h, 0, Node.PROPAGATE))
                continue;                // loop on failed CAS
        }
        if (h == head)                   // loop if head changed
            break;
    }
}
```

### 和CyclicBarrier的区别
1. CyclicBarrier允许一系列线程相互等待对方到达一个点，正如barrier表示的意思，该点就像是一个栅栏，先到达的线程被阻塞在栅栏前，必须等到所有线程都到达了才能够通过栅栏；
2. CyclicBarrier持有一个变量parties，表示需要全部到达的线程数量；先到达的线程调用barrier.await方法进行等待，一旦到达的线程数达到parties变量所指定的数，栅栏打开，所有线程都可以通过。
3. CyclicBarrier构造方法接受另一个Runnable类型参数barrierAction，该参数表明在栅栏被打开的时候需要采取的动作，null表示不采取任何动作，*注意该动作会在栅栏被打开而所有线程接着运行前被执行*。
4. CyclicBarrier是可重用的，当最后一个线程到达的时候，栅栏被打开，所有线程通过之后栅栏重新关闭，进入下一代；
5. CyclicBarrier.reset方法能够手动重置栅栏，此时正在等待的线程会收到BrokenBarrierException异常。


## CyclicBarrier 并发工具

可以让一组线程达到一个屏障时被阻塞，知道最后一个线程达到屏障时，所有被阻塞的线程才能继续执行。

CyclicBarrier好比一扇门，默认情况下是关闭状态，堵住了线程执行的道路，知道所有线程都就位，门才打开，让所有的线程一起通过。这个计数器可以反复使用。

### 构造方法

1. 默认的构造方法是CyclicBarrier(int parties),其参数拜师屏障拦截的线程数量，每个线程调用await方法告诉CyclicBarrier已经到达屏障位置，线程被阻塞。
2. 另外一个构造方法CyclicBarrier(int parties,Runnable barrierAction),其中barrierAction任务会在所有线程到达屏障后执行。也就是一次计数完成之后，系统会执行的动作。

![](https://upload-images.jianshu.io/upload_images/2184951-b972911b7debef14.png?imageMogr2/auto-orient/)


```java
    private static void cyclicBarrier() throws Exception {
        CyclicBarrier cyclicBarrier = new CyclicBarrier(3) ;

        new Thread(new Runnable() {
            @Override
            public void run() {
                LOGGER.info("thread run");
                try {
                    cyclicBarrier.await() ;
                } catch (Exception e) {
                    e.printStackTrace();
                }

                LOGGER.info("thread end do something");
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                LOGGER.info("thread run");
                try {
                    cyclicBarrier.await() ;
                } catch (Exception e) {
                    e.printStackTrace();
                }

                LOGGER.info("thread end do something");
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                LOGGER.info("thread run");
                try {
                    Thread.sleep(5000);
                    cyclicBarrier.await() ;
                } catch (Exception e) {
                    e.printStackTrace();
                }

                LOGGER.info("thread end do something");
            }
        }).start();

        LOGGER.info("main thread");
    }
```

CyclicBarrier 中文名叫做屏障或者是栅栏，也可以用于线程间通信。

它可以等待 N 个线程都达到某个状态后继续运行的效果。

1. 首先初始化线程参与者。
2. 调用 `await()` 将会在所有参与者线程都调用之前等待。
3. 直到所有参与者都调用了 `await()` 后，所有线程从 `await()` 返回继续后续逻辑。

运行结果:

```
2018-03-18 22:40:00.731 [Thread-0] INFO  c.c.actual.ThreadCommunication - thread run
2018-03-18 22:40:00.731 [Thread-1] INFO  c.c.actual.ThreadCommunication - thread run
2018-03-18 22:40:00.731 [Thread-2] INFO  c.c.actual.ThreadCommunication - thread run
2018-03-18 22:40:00.731 [main] INFO  c.c.actual.ThreadCommunication - main thread
2018-03-18 22:40:05.741 [Thread-0] INFO  c.c.actual.ThreadCommunication - thread end do something
2018-03-18 22:40:05.741 [Thread-1] INFO  c.c.actual.ThreadCommunication - thread end do something
2018-03-18 22:40:05.741 [Thread-2] INFO  c.c.actual.ThreadCommunication - thread end do something
```

可以看出由于其中一个线程休眠了五秒，所有其余所有的线程都得等待这个线程调用 `await()` 。

该工具可以实现 CountDownLatch 同样的功能，但是要更加灵活。甚至可以调用 `reset()` 方法重置 CyclicBarrier (需要自行捕获 BrokenBarrierException 处理) 然后重新执行。

下一个例子：

```java
public class CyclicBarrierDemo {
    public static class Soldier implements Runnable{
        private String solider;
        private final CyclicBarrier cyclicBarrier;

        public Soldier(CyclicBarrier cyclicBarrier,String soliderName) {
            this.cyclicBarrier = cyclicBarrier;
            this.solider = soliderName;
        }

        @Override
        public void run() {
            try{
                //等待所有士兵到齐
                cyclicBarrier.await();  //这里设置一个屏障 ，当所有线程到达之后执行barrierAction也就是BarrierRun
                doWorker();
                //等待所有士兵完成工作
                cyclicBarrier.await();   //这里又设置了一个屏障，当所有线程到达之后再次执行BarrierRun
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
            }
        }

        void doWorker(){
            try {
                Thread.sleep(Math.abs(new Random().nextInt()%10000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(solider+" 任务完成");
        }
    }

    public static class BarrierRun implements  Runnable{
        boolean flag;
        int N;
        public BarrierRun(boolean flag,int N){
            this.flag =flag;
            this.N  =N;
        }
        @Override
        public void run() {
            if(flag){
                System.out.println("士兵"+N+"个，任务完成");
            }else{
                System.out.println("士兵"+N+"个,集合完成");
                flag =true;
            }
        }
    }


    public static void main(String args[]){
        final  int N =10;
        Thread[] allSolider = new Thread[N];
        boolean flag = false;
        CyclicBarrier cyclicBarrier =new CyclicBarrier(N,new BarrierRun(flag,N));
        //设置屏障点，主要是为了执行这个方法
        System.out.println("集合队伍！");
        for(int i = 0;i<N;++i){
            System.out.println("士兵"+i+"报告！");
            allSolider[i] = new Thread(new Soldier(cyclicBarrier,"士兵"+i));
            allSolider[i].start();
        }
    }
}

```


```
集合队伍！
士兵0报告！
…………
士兵9报告！
士兵10个,集合完成
士兵3 任务完成
……
士兵6 任务完成
士兵10个，任务完成
```

这里CyclicBarrier经历了两次计数。调用了两次cyclicBarrier.await() 设立两个屏障。

CyclicBarrier实现主要基于ReentrantLock。

1. 每当线程执行await，内部变量count减一，如果count!=0,说明还有线程还未到屏障处，则在锁条件变量trip上等待。
2. 当count=0时，说明所有线程都已经到屏障处，执行条件变量的signalAll方法唤醒等待的线程。其中nextGeneration方法可以实现屏障的循环使用。
   - 重新生成Generation对象（Generation用来控制屏障的循环使用）
   - 恢复count值

### CountDownLatch区别

1. CountDownLatch允许一个或多个线程等待一些特定的操作完成，而这些操作是在其他的线程中进行的，也就是说会出现***等待的线程***和***被等的线程***这样分明的角色；
2. CountDownLatch构造函数中与一个count参数，表示有多少个线程需要被等待，对这个变量的修改实在其他线程中调用countDown方法，每一个不同的线程调用一次countDown方法就表示有一个被等待的线程到达，count变为0时，latch就会被打开，处于等待状态的那些线程接着可以执行；
3. CountDownLatch是一次性使用的，也就是说latch只能只用一，一旦被打开就不能再次关闭，将会一直保持打开状态，因此CountDownLatch类也没有为count变量提供set方法。

## 线程响应中断

```java
public class StopThread implements Runnable {
    @Override
    public void run() {

        while ( !Thread.currentThread().isInterrupted()) {
            // 线程执行具体逻辑
            System.out.println(Thread.currentThread().getName() + "运行中。。");
        }

        System.out.println(Thread.currentThread().getName() + "退出。。");

    }

    public static void main(String[] args) throws InterruptedException {
        Thread thread = new Thread(new StopThread(), "thread A");
        thread.start();

        System.out.println("main 线程正在运行") ;

        TimeUnit.MILLISECONDS.sleep(10) ;
        thread.interrupt();
    }


}
```

输出结果:

```
thread A运行中。。
thread A运行中。。
thread A退出。。
```

可以采用中断线程的方式来通信，调用了 `thread.interrupt()` 方法其实就是将 thread 中的一个标志属性置为了 true。

并不是说调用了该方法就可以中断线程，如果不对这个标志进行响应其实是没有什么作用(这里对这个标志进行了判断)。

**但是如果抛出了 InterruptedException 异常，该标志就会被 JVM 重置为 false。**

## 线程池 awaitTermination() 方法

如果是用线程池来管理线程，可以使用以下方式来让主线程等待线程池中所有任务执行完毕:

```java
    private static void executorService() throws Exception{
        BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>(10) ;
        ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(5,5,1, TimeUnit.MILLISECONDS,queue) ;
        poolExecutor.execute(new Runnable() {
            @Override
            public void run() {
                LOGGER.info("running");
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        poolExecutor.execute(new Runnable() {
            @Override
            public void run() {
                LOGGER.info("running2");
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        poolExecutor.shutdown();
        while (!poolExecutor.awaitTermination(1,TimeUnit.SECONDS)){
            LOGGER.info("线程还在执行。。。");
        }
        LOGGER.info("main over");
    }
```

输出结果:

```
2018-03-16 20:18:01.273 [pool-1-thread-2] INFO  c.c.actual.ThreadCommunication - running2
2018-03-16 20:18:01.273 [pool-1-thread-1] INFO  c.c.actual.ThreadCommunication - running
2018-03-16 20:18:02.273 [main] INFO  c.c.actual.ThreadCommunication - 线程还在执行。。。
2018-03-16 20:18:03.278 [main] INFO  c.c.actual.ThreadCommunication - 线程还在执行。。。
2018-03-16 20:18:04.278 [main] INFO  c.c.actual.ThreadCommunication - main over
```

使用这个 `awaitTermination()` 方法的前提需要关闭线程池，如调用了 `shutdown()` 方法。

调用了 `shutdown()` 之后线程池会停止接受新任务，并且会平滑的关闭线程池中现有的任务。


## 管道通信

```java
    public static void piped() throws IOException {
        //面向于字符 PipedInputStream 面向于字节
        PipedWriter writer = new PipedWriter();
        PipedReader reader = new PipedReader();

        //输入输出流建立连接
        writer.connect(reader);


        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                LOGGER.info("running");
                try {
                    for (int i = 0; i < 10; i++) {

                        writer.write(i+"");
                        Thread.sleep(10);
                    }
                } catch (Exception e) {

                } finally {
                    try {
                        writer.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        });
        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                LOGGER.info("running2");
                int msg = 0;
                try {
                    while ((msg = reader.read()) != -1) {
                        LOGGER.info("msg={}", (char) msg);
                    }

                } catch (Exception e) {

                }
            }
        });
        t1.start();
        t2.start();
    }
```

输出结果:

```
2018-03-16 19:56:43.014 [Thread-0] INFO  c.c.actual.ThreadCommunication - running
2018-03-16 19:56:43.014 [Thread-1] INFO  c.c.actual.ThreadCommunication - running2
2018-03-16 19:56:43.130 [Thread-1] INFO  c.c.actual.ThreadCommunication - msg=0
2018-03-16 19:56:43.132 [Thread-1] INFO  c.c.actual.ThreadCommunication - msg=1
2018-03-16 19:56:43.132 [Thread-1] INFO  c.c.actual.ThreadCommunication - msg=2
2018-03-16 19:56:43.133 [Thread-1] INFO  c.c.actual.ThreadCommunication - msg=3
2018-03-16 19:56:43.133 [Thread-1] INFO  c.c.actual.ThreadCommunication - msg=4
2018-03-16 19:56:43.133 [Thread-1] INFO  c.c.actual.ThreadCommunication - msg=5
2018-03-16 19:56:43.133 [Thread-1] INFO  c.c.actual.ThreadCommunication - msg=6
2018-03-16 19:56:43.134 [Thread-1] INFO  c.c.actual.ThreadCommunication - msg=7
2018-03-16 19:56:43.134 [Thread-1] INFO  c.c.actual.ThreadCommunication - msg=8
2018-03-16 19:56:43.134 [Thread-1] INFO  c.c.actual.ThreadCommunication - msg=9
```

Java 虽说是基于内存通信的，但也可以使用管道通信。

需要注意的是，输入流和输出流需要首先建立连接。这样线程 B 就可以收到线程 A 发出的消息了。


实际开发中可以灵活根据需求选择最适合的线程通信方式。
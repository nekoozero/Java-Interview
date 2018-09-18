# 锁的优化及注意事项

## 提高锁性能

“锁”的竞争必然会导致程序的整体性能下降。为了将这种副作用降到最低，以下有几种建议。

### 减少所持有的时间

对于使用锁进行并发控制的应用程序而言，在锁的竞争过程中，单个线程对锁的持有时间与系统性能有着直接的关系。如果线程持有锁的时间很长，那么相对的，锁的竞争程度也就越激烈。以下面代码为例：

```java
public synchronized void syncMethod(){
    othercode1();
    mutexMethod();
    othercode2();
}
```

syncMethod()方法中，假设只有mutexMethod()方法是有同步需求的，而othercode1()和othercode2()并不需要做同步控制。如果othercode1()和othercode2()分别是重量级的方法，则会花费较长时间的CPU时间。此时，如果并发量较大，使用这种对整个方法做同步的方案，会导致等待线程大量增加。在进入该方法时获得内部锁，只有在所有任务都执行完成后，才会释放锁。

一个较为优化的解决方案是，只在必要时进行同步，这样就能明显减少线程持有锁的时间，提高系统的吞吐量。

```java
public void syncMehtod(){
    othercode1();
    synchronized(this){
        mutexMethod();
    }
    othercode2();
}
```

只针对mutexMethod()方法做了同步，锁占用的时间相对较短，因此能有更高的并行度。

### 减小锁粒度

减小锁粒度也是一种削弱多线程竞争的有效手段。这种技术典型的使用场景就是ConcurrentHashMap（jdk1.7，下面都是）类的实现。

对于HashMap来说，最重要的两个方法get()和put()。一种最自然的想法就是对整个HashMap加锁，必然可以得到一个线程安全的对象。但是这样做，我们就认为加锁粒度太大。对于ConcurrentHashMap,它内部进一步戏份了若干个小的HashMap,称之为段（SEGMENT）。默认的情况下，一个ConcurrentHashMap被进一步细分为16段。

如果需要在ConcurrentHashMap中增加一个新的表项，并不是将整个HashMap加锁，而是首先根据hashcode得到该表项应该被存放到哪个段中，然后对该段加锁，并完成put()操作。在多线程环境中，如果多个线程同时进行put()操作，，只要被加入的表项不存放在同一个段中，则线程间便可以做到真正的并行。

但是减少锁粒度会引入一个新的问题，即：当系统需要获取全局锁时，其消耗的资源会比较多。仍然以ConcurrentHashMap类为例，虽然其put()方法很好地分离了锁，但是当尝试访问ConcurrentHashMap的size()方法，他将返回ConcurrentHashMap的有效表项的数量，即ConcurrentHashMap的全部有效表项之和。要获取这个信息需要取得所有子段的锁，因此，其size()方法的部分代码如下：

```java
sum =0;
for(int i=0;i<segments.length;++i)
    segments[i].lock();
for(int i=0;i<segments.length;++i)
    sum+=segments[i].count;
for(int i=0;i<segments.length;++i)
    segments[i].unlock();
```

可以看到在计算总数时，先要获得所有段的锁，然后再求和。但是，ConcurrentHashMap的size()方法并不是总是这样进行的，事实上，size()方法会先使用无锁的方式求和，如果失败才会尝试这样的加锁的方法。但不管怎么说，在高并发场合ConcurrentHashMap的size()的性能依然要差于HashMap。

`因此，只有在类似于size()获取全局信息的方法调用并不频繁时，这种减少锁粒度的方法才能真正意义上提高系统吞吐量。`

<strong>注意：所谓减少锁粒度，就是指缩小锁定对象的范围，从而减少锁冲突的可能性，进而提高系统的并发能力。</strong>

### 读写分离锁来替换独占锁

使用读写分离锁来替代独占锁是减小锁粒度的一种特殊情况。减少锁粒度是通过分割数据结构实现的，那么读写锁则是对系统功能点的分割。

在读多写少的场合，读写锁是对系统性能是很有好处的。因为如果系统在读写数据时均只使用独占锁，那么读操作和写操作、读操作和读操作间、写操作和写操作间均不能做到真正的并发，并且需要相互等待。而读操作本身不会影响到数据完整性和一致性。incident，理论上讲，在大部分情况下，应该可以允许多线程同时读，读写锁正是实现了这种功能。

<strong>注意：在读多写少的场合，使用读写锁可以有效提升系统的并发能力。</strong>

### 锁分离

如果将读写锁的思想进一步的延伸，就是锁分离。读写锁根据读写操作功能上的不同，进行了有效的锁分离。依据应用程序的功能特点，使用类似的分离思想，也可以对独占锁进行分离。一个典型的案例就是java.util.concurrent.LinkedBlockingQueue的实现。

在LinkedBlockingQueue的实现中，take()函数和put()函数分别实现了从队列中去的数据和往队列中增加数据的功能。虽然两个函数都对当前队列进行了修改操作，但由于LinkedBlockingQueue是基于链表的，因此，两个操作分别作用于队列的前端和尾端，从理论上来说，两者并不冲突。

如果使用独占锁，则要求在两个操作进行时获取当前队列的独占锁，那么take()和put()操作就不可能真正的并发，在运行时，他们会彼此等待对方释放锁资源。在这种情况下，锁竞争会相对比较激烈，从而影响程序在高并发时的性能。

因此，在JDK的实现中，并没有采用这样的方式，取而代之的是两把不同的锁，分离了take()和put()操作。

```java
/**Lock held by take,poll,etc  */
private final ReentrantLock takeLock = new ReentrantLock();   //take()函数需要持有takeLock
/**Wait queue for waiting takes */
private final Condition notEmpty = takeLock.newCondition();
/**Lock held by put,offer,etc*/
private final ReentrantLock putLock =new ReentrantLock(); //put()函数需要持有putLock()
/**Wait queue for waiting puts*/
private final Condition notFull = putLock.newCondition();
```

以上片段代码，定义了takeLock和putLock，他们分别在take操作和put操作中使用。因此，take()函数和put()函数就此互相独立，他们之间不存在锁竞争关系，只需要在take()和take()间、put()和put()间分别对takeLock和putLock进行竞争。从而，削弱了锁竞争的可能性。

函数take()的实现如下：

```java
public E take() throws InterruptException {
    E x;
    int c = -1;
    final AtomicInteger count = this.count;
    final ReentrantLock takeLock  = this.takeLock;
    takeLock.lockInterruptibly();     //不能有两个线程同时取数据
    try{
        try{
            while(count.get()==0)       //如果当前没有可用数据，则一直等待
               notEmpty.await();       //等待，put()操作的通知
        }catch(InterruptedException e){
            notEmpty.signal();         //通知其他未中断的线程
            throw e;
        }
        x = extract();               //取得第一个数据
        c = count.getAndDecrement();  //数量减一，原子操作，因为会和put()函数同时访问count。注意：变量c是count-1的值
        if(c>1)
            notEmpty.signal();       //通知其他take()操作
    }finall{
        takeLock.unlock();           //释放锁
    }
    if(c==capacity)
        signalNotFull();             //通知put()操作，已有空余空间
    return x;
}
```

函数put()的实现如下：
```java
public void put(E e) throws InterruptedException{
    if(e==null) throw new NullPointerException();
    int c = -1;
    final ReentranLock putLock = this.putLock;
    final AtomicInteger count = this.count;
    putLock.lockInterruptibly();   //不能有两个线程同时进行put()
    try{
        try{
            while(count.get()==capacity)   //如果队列已经满了
                notFull.await();           //等待
        }catch(InterruptedException ie){
            notFull.signal();              //通知未中断的线程
            throw ie;
        }
        insert(e);                      //插入数据
        c=count.getAndIncrement();      //更新总数，变量c是count加1前的值
        if(c+1<capacity)
            notFull.signal();           //有足够的空间，通知其他线程
    }finally{
        putLock.unlokc();                //释放锁
    }
    if(c==0)
        signalNotEmpty();               //插入成功后，通知take()操作取数据
}
```

通过takeLock和putLock两把锁，LinkedBlockingQueue实现了取数据和写数据的分离，是两者成为真正意义上可并发的操作。

### 锁粗化

通常情况下，为了保证多线程之间的有效并发，会要求每个线程持有锁的时间尽量短，即在使用完公共资源后，应该立即释放锁。只有这样，等待在这个锁上的其他线程才能尽早的获得资源执行任务。但是，范式都有一个度，如果对一个锁不停地进行请求、同步和释放，其本身也会消耗系统宝贵的资源，反而不利于性能的优化。

为此，虚拟机在遇到一连串连续地对同一锁不断进行请求和释放的操作时，便会把所有的锁操作整合成对锁的一次请求，从而减少对锁的请求同步次数，这个操作叫做锁的粗化。比如：

```java
public void demoMethod() {
    synchronized(lock){
        //do something
    }
    //做其他不需要的同步的工作，但能很快执行完毕
    synchronized（lock）{
        //do something
    }
}
```

会被整合成下面这个形式：

```java
public void demoMethod(){
    //整合成一次锁请求
    synchronized(lock){
        //do something
        //做其他不需要的同步的工作，但能很快执行完毕
    }
}
```

在开发过程中，大家应该有意识地在河里的场合进行锁粗化，尤其当在循环内请求锁时。以下是一个循环内请求锁的列子，在这种情况下，意味着每次驯化都有申请锁和释放锁的操作。但在这种情况下，显然是没有必要的。

```java
for(int i=0;i<CIRCLE;i++){
    synchronized(lock){

    }
}
```

所以，一种更加合理的做法应该是在外层只请求一次锁：

```java
synchronized(lock){
    for(int i=0;i<CIRCLE;i++){
        //...
    }
}
```

<strong>注意：性能优化就是根据运行时的真实情况对各个资源点进行权衡折中的过程。锁粗化的思想和减少锁持有时间是相反的，但在不同的场合，他们的效果并不相同。所以大家需要根据实际情况，进行权衡。</strong>
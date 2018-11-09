# 内存分配与回收策略

Java体系中的自动内存管理最终可以归结为自动化地解决两个问题：<strong>给对象分配内存以及回收分配给对象的内存</strong>。

对象的内存分配，就是在堆上分配（但也可能经过 JIT 编译后被拆散为标量类型并间接地在栈上分配），对象主要分配在新生代的 Eden 区上，如果启动了本地线程分配缓冲，将按线程优先在 TLAB(Thread Local Allocation Buffer，即线程本地分配缓存区，这是一个线程专用的内存分配区域)上分配。少数情况下也可能会直接分配在老年代中，分配的规则并不是百分之百固定的，其细节取决于*当前使用的是哪一种垃圾收集器组合，还有虚拟机终于内存相关的参数设置。*

## 对象优先在 Eden 分配

`新生代分为一个 Eden 区和两个 Survivor 区（一个 from，一个 to），通过 SurivivorRatio 参数设置 Eden 与 Survivor 的比值，默认为 8，当有一次 Minor GC 时，会将 Eden 和 from 区里存活的对象放到 to 中（通过复制算法），然后将Eden 和 from 区里的对象清空，把 to 区变为 from 区，把 from 区变为 to 区，这样存活的对象就又在 from 区了。`

大多数情况下，对象在新生代 Eden 区中分配。当 Eden 区没有足够空间进行分配时，虚拟机会进行一次 Minor GC。

> <strong>新生代 GC（Minor GC）</strong>：指发生在新生代的垃圾收集动作，因为 Java 对象大都具备朝生夕灭的特性，所以 Minor GC 非常频繁，一般回收速度也比较快。<br>
> <strong>老年代 GC（Major GC/Full GC）</strong>：指发生在老年代的 GC，出现了 Major GC，经常会伴随至少一次的 Minor GC（但并非绝对的，在 Parallel Scavenge 收集起的收集策略里九幽直接进行 Major GC 的策略选择过程）。Major GC 的速度一般会比 Minor GC 慢十倍以上。
> Full GC(Ergonomics)，由于 HotSpot 自动选择和调优引发的 GC。
> Full FC(Metadata GC Threshold):元空间引发的 Full GC。

## 大对象直接进入老年代

大对象是指，需要大量连续内存空间的 Java 对象，最典型的大对象就是那种很长的字符串以及数组。大对象对虚拟机的内存分配来说就是一个坏消息，经常出现 大对象容易导致内存还有不少空间时就提前触发垃圾收集以获取足够的连续空间来“安置”它们。

例子：

```java
public class TestGClog {
    private static final int _1MB = 1024*1024;
    public static  void testAllocation() {
    	byte[] allocation1,allocation2,allocation3,allocation4,allocation5;
    	allocation1 = new byte[2*_1MB];
    	allocation2 = new byte[2*_1MB];
    	allocation3 = new byte[2*_1MB];
    	allocation4 = new byte[5*_1MB];
    }
    public static void main(String[] args) {
    	testAllocation();
	}
}
```

设置 jvm 运行参数：

```
-verbose:gc
-Xms20M
-Xmx20M
-Xmn10M
-XX:+PrintGC
-XX:+PrintGCDateStamps -XX:+PrintHeapAtGC -Xloggc:E:/gclogs/gc.log
-XX:+PrintGCDetails
-XX:SurvivorRatio=8
```

简单解释一下：
堆内存一共约 20M，新生代约 10M，老年代约 10M，新生代中的 Eden：Survivor=8，也就是 Eden 约 8M，两个 Survivor 各约 1M。

输入的日志：
```
Java HotSpot(TM) 64-Bit Server VM (25.121-b13) for windows-amd64 JRE (1.8.0_121-b13), built on Dec 12 2016 18:21:36 by "java_re" with MS VC++ 10.0 (VS2010)
Memory: 4k page, physical 12439556k(8084076k free), swap 24877248k(20503548k free)
CommandLine flags: -XX:InitialHeapSize=20971520 -XX:MaxHeapSize=20971520 -XX:MaxNewSize=10485760 -XX:NewSize=10485760 -XX:+PrintGC -XX:+PrintGCDateStamps -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+PrintHeapAtGC -XX:SurvivorRatio=8 -XX:+UseCompressedClassPointers -XX:+UseCompressedOops -XX:-UseLargePagesIndividualAllocation -XX:+UseParallelGC 
Heap
 PSYoungGen      total 9216K, used 7456K [0x00000000ff600000, 0x0000000100000000, 0x0000000100000000)
  eden space 8192K, 91% used [0x00000000ff600000,0x00000000ffd48358,0x00000000ffe00000)
  from space 1024K, 0% used [0x00000000fff00000,0x00000000fff00000,0x0000000100000000)
  to   space 1024K, 0% used [0x00000000ffe00000,0x00000000ffe00000,0x00000000fff00000)
 ParOldGen       total 10240K, used 5120K [0x00000000fec00000, 0x00000000ff600000, 0x00000000ff600000)
  object space 10240K, 50% used [0x00000000fec00000,0x00000000ff100010,0x00000000ff600000)
 Metaspace       used 2772K, capacity 4486K, committed 4864K, reserved 1056768K
  class space    used 299K, capacity 386K, committed 512K, reserved 1048576K
```

可以看到，使用的是 Parallel Scavenge 收集器 + Parallel Old 收集器，期间没有发生 Minor GC 和 Full GC,5M 的对象直接分配到了老年代，3个 2M
的对象仍然处于新生代（存在 1M 左右的差距，可能是 TLAB 所占用的内存，不清楚），大对象直接进入老年代。大对象的大小判定是由 Parallel Scavenge 收集器动态设置的（自适应调节策略），不需要人为手动设置。

## 长期存活的对象将进入老年代

既然虚拟机采用了分代收集的思想来管理内存，那么内存回收时就必须能识别哪些对象应放在新生代，哪些对象应放在老年代。为了做到这一点，虚拟机给每个对象定义了一个对象年龄（Age）计数器。如果对象在 Eden 出生并经过第一次 Minor GC 后仍然存活，并且能被 Survivor 容纳的话，将被移动到 Survivor 空间中，并且对象年龄设为 1。对象在 Survivor 区中每“熬过”一次 Minor GC，年龄就增加 1 岁，当他的年林增加到一定成都（默认 15 岁），就将会被晋升到老年代中。对象晋升老年代的年龄阈值，可以通过参数 -XX:MaxTenuringThreshold 设置。

## 动态对象年龄判定

为了更好地适应不同程序的内存状况，虚拟机并不是永远的要求对象年龄必须达到 MaxTenuringThreshold 才能晋升到老年代，如果在 Survivor 空间中相同年龄所有对象大小的总和大于 Suvivor 空间的一半，年龄大于或等于该年龄的对象就可以直接进入老年代，无须等到 MaxTenuringThreshold 中要求的年龄。

## 空间分配担保

在发生 Minor GC 之前，虚拟机会检查老年代最大可用的连续空间是否大于新生代所有对象总空间，如果这个条件成立，那么 Minor GC 可以确保是安全的。如果不成立，则虚拟机会查看 HandlePromotionFailure 设置值是否允许担保失败。如果允许，那么会继续检查老年代最大可用的连续空间是否大于历次晋升到老年代对象的平均大小，如果大于，将尝试着进行一次 Minor GC，尽管这次 GC 是由风险的；如果小于 ，或者 HandlePromotionFailure 设置不允许冒险，那这时也要改为一次 Full GC。

为什么会冒险？

新生代使用的是复制-收集算法，但为了内存利用率，只是用其中一个 Survivor 区来作为轮换备份，因此当出现大量对象在 Minor GC 后仍然存活的情况（最极端的情况就是内存回收后新生代中所有对象都存活），就需要老年代进行分配担保，老年代要进行这样的担保，前提是老年代本身还有容纳这些对象的剩余空间，一共多少对象会活下来在实际完成回收之前是无法明确知道的，所以只好去之前每一次回收晋升到老年代对象容量的平均值大小作为经验值，与老年代的剩余空间进行比较，决定是否进行 Full GC 来让老年代腾出更多空间。

取平均值比较其实仍然是一种动态概率的手段，也就是说，如果某次 Minor GC 存活后的对象突增，远远高于平局值的话，依然会导致担保失败（Handle Promotion Failure）。如果出现了 HandlePromotionFailure 失败，那就只好在失败后重新发起一次 Full GC。虽然担保失败时绕的圈子是最大的，但大部分情况下都还是会将HandlePromotionFailure 开关打开，避免 Full GC 过于频繁。
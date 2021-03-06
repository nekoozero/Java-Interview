# 回收方法区

Java 虚拟机规范正确实说过可以不需要虚拟机在方法区实现垃圾收集，而且方法区中进行垃圾收集的“性价比”一般比较低：`在堆中，尤其是在新生代中，常规应用进行一次垃圾收集一般可以回收70%~95%的空间，而永久代的垃圾收集效率远低于此。`

永久代的垃圾收集主要回收两部分内容：<strong>废弃常量和无用的类</strong>。回收废弃常量与回收 Java 堆中的对象非常相似。判定一个常量是否是“废弃常量”比较简单，而要判定一个类是否是“无用的类”的条件则苛刻许多，类需要同时满足下面 3 个条件才能算是“无用的类”：

- 该类所有实力都已经被回收，也就是 Java 堆中不存在该类的任何实例。
- 加载该类的 ClassLoader 已经被回收。
- 该类对应的 java.lang.Class 对象没有在任何地方被引用，无法在任何地方通过反射访问该类的方法。

虚拟机可以对满足上述 3 个条件的无用类进行回收，这里说的仅仅是“可以”，而并不是和对象一样，不适用了就必然会回收。是否对类进行回收，HotSpot 虚拟机提供了参数进行控制。

在大量使用反射、动态代理、CGLib 等 ByteCode 框架、动态生成 JSP 以及 OSGI 这类频繁自定义 ClassLoader 的场景都需要虚拟机具备类卸载的功能，以保证永久代不会溢出。
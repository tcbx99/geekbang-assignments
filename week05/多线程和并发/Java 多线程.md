# synchronized 和 volatile

1. 不是一个东西，volatile 要求所有工作中的值都是不可信的，要求从主存读取，并及时刷入主存；synchronized 直接锁定了变量，并行变串行了
2. volatile 只能保证可见性（但不会给你阻塞了），synchronized 三个全保证了，但是变串行了（甚至比单线程慢）
3. synchronized 可能被编译器（主要是 JIT 的那个）优化掉

# JMM 内存模型

[Chapter 17. Threads and Locks (oracle.com)](https://docs.oracle.com/javase/specs/jls/se8/html/jls-17.html#jls-17.4)

调用栈和本地变量存放在线程栈上，对象存放在堆上。同时，对多线程的缓存，有如下定义：

* 线程之间的共享变量存储在主内存（Main Memory）中
* 每个线程都有一个私有的本地内存（Local Memory），本地内存是一个抽象概念，并不真实存在，它涵盖了缓存、写缓冲区、寄存器以及其他的硬件和编译器优化。本地内存中存储了该线程以读/写共享变量的拷贝副本。
* 线程间通信必须要经过主内存（共享变量的刷新和更新）

## Happens-before 原则

如果一个操作执行的结果需要对另一个操作可见（两个操作既可以是在一个线程之内，也可以是在不同线程之间），那么这两个操作之间必须要存在 happens-before 关系：

* 对管程锁的解锁，必须在随后对这个锁的加锁之前完成
* 对 volatile 的写入必须在随后对这个变量的度之前完成
* 对线程 start() 的调用必须在已开始的线程的任意操作前完成
* 某线程的所有动作必须在其他线程对此线程的 join() 操作之前完成
* 任意对象的默认初始化必须在其他操作之前完成

对于程序员而言，不需要知道 JVM 如何实现这个原则（毕竟不同 JVM 实现，不同处理器都是不同的）

# ThreadLocal 的天坑

## 弱引用

ThreadLocalMap 中，Key 为 ThreadLocal 的弱引用，Value 为 ThreadLocal 的值。

设想一个情形，我们这个 ThreadLocal 只用一下，但是 Thread 是很长命的。这种情形，ThreadLocal 在 ThreadLocalMap 中是强引用的情形，就会导致实际上这个 ThreadLocal 用不到了，却无法释放的问题（内存泄漏）。如果使用了弱引用，那么当弱引用的被引对象 GC 之后，就会在 Key 处留下一个 NULL，可以进行内存回收（注意，这个内存回收是主动回收！）

## 为啥是个天坑

ThreadLocal 内存泄漏的根源是：由于 ThreadLocalMap 的生命周期跟 Thread 一样长，如果没有手动删除对应 key 就会导致内存泄漏。弱引用只是一种类似于守门员一样的保险措施，实际在不进行 ThreadLocalMap 的操作时（也就是，长时间不使用任意 ThreadLocal 的情形）对应的 Value 仍然不会被回收的（当然，OOM 了的时候，一看 ThreadLocalMap 里面一堆空 Key，也会驱使程序员去排查问题）。

‍

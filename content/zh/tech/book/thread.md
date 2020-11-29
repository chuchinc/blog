---
title: "《Java并发编程之美》"
date: 2020-11-29T16:07:26+08:00
draft: false
tags: ["Java","并发编程"]
---

![thread](/img/thread.jpg)

该书在微信读书中上架，可以免费阅读，配套代码：[https://github.com/chuchinc/kl-book-resource](https://github.com/chuchinc/kl-book-resource)

## 并发编程线程基础

### 什么是线程

* 进程是代码在数据集合上的一次运行活动，是系统进行资源分配和调度的基本单位

* 线程则是进程的一个执行路径，一个进程中至少有一个线程，进程中的多个线程共享进程的资源,是CPU分配资源和调度的基本单位

在Java中，当我们启动main函数时其实就启动了一个JVM的进程，而main函数所在的线程就是这个进程中的一个线程，也称主线程。

进程和线程的关系如图：

![thread](/img/thread1.jpg)

### 线程的创建与运行

*继承Thread方式*

```java
public class ThreadWay {

    public static class MyThread extends Thread {
        @Override
        public void run() {
            System.out.println("I am a child thread");
        }
    }

    public static void main(String[] args) {
        MyThread myThread = new MyThread();
        myThread.start();
    }
}
```

调用了start方法后才真正启动了线程

调用了start方法线程处于就绪状态，就绪状态是指该线程获取到了除CPU资源外的其他资源，等待获取CPU资源后才会真正处于运行状态，run方法执行完毕，该线程就处于终止状态

* 优点

  在run()方法内获取当前线程直接使用this就行了，无须使用Thread.currentThread()方法

* 缺点

  如果继承了Thread类，那么就不能再继承其他类

  任务和代码没有分离，当多个线程执行一样的任务时需要多份任务代码

  任务没有返回值

*实现Runnable接口的run方法方式*

```java
public class RunnableWay implements Runnable {
    @Override
    public void run() {
        System.out.println("I am a child thread");
    }

    public static void main(String[] args) {
        RunnableWay runnableWay = new RunnableWay();
        new Thread(runnableWay).start();
        new Thread(runnableWay).start();
    }
}
```

* 优点

  共用代码逻辑

* 缺点

  任务没有返回值

*FutureTask方式*

```java
public class FutureTaskWay implements Callable<String> {
    @Override
    public String call() throws Exception {
        return "hello";
    }

    public static void main(String[] args) {
        //创建异步任务
        FutureTask<String> stringFutureTask = new FutureTask<>(new FutureTaskWay());
        //启动线程
        new Thread(stringFutureTask).start();
        try {
            String result = stringFutureTask.get();
            System.out.println(result);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }
}
```

* 优点

  能获取返回结果

*小结*

使用继承方式的好处是方便传参，你可以在子类里面添加成员变量，通过set方式设置参数或者通过构造函数进行传递，而如果使用Runnable方式，则只能使用主线程里面声明为final的变量。不好的地方是Java不支持多继承，如果继承了Thread类，那么子类就不能再继承其他类，而Runnable则没有这个限制。前两种方法都没办法拿到任务的返回结果，但是Futuretask方式可以。

### 线程通知与等待

#### wait()函数

当线程调用一个共享变量的wait()方法时，该调用线程会被阻塞挂起，直到发生下面几件事情之一才返回：

1. 其他线程调用了该共享对象的notify()或者notifyAll()方法
2. 其他线程调用了该线程的interrupt()方法，该线程抛出InterruptedException异常返回

如果调用wait()方法的线程没有事先获取该对象的监视器锁，则调用wait()方法时调用线程会抛出illeagalMonitorStateException异常

*如何获取一个共享变量的监视器锁*

* 执行synchronized同步代码块时，使用该共享变量作为参数

```java
synchronized (共享变量) {
    //doSomething
}
```

* 调用该共享变量的方法，并且该方法使用了synchronized修饰

```java
synchronized void add(int a, int b) {
    //doSomething
}
```

一个线程可以从挂起状态变为可运行状态（也就是被唤醒），即使该线程没有被其他线程调用notify()、notifyAll()方法进行通知，或者被中断，或者等待超时，这就是所谓的“虚假唤醒”。

虽然虚假唤醒在应用实践很少发生，但需要防患于未然，做法是不停测试该线程被唤醒的条件是否满足,看下面经典的调用共享变量wait()方法的实例：

```java
synchronized(obj) {
    while (条件不满足) {
        obj.wait();
    }
}
```

*生产者和消费者例子*

```java
/**
     * 生产者线程
     */
    public static class ProducerThread extends Thread {
        @Override
        public void run() {
            synchronized (queue) {
                //消费队列满，则等待队列空闲
                while (queue.size() == MAX_SIZE) {
                    //挂起当前线程，并释放通过同步块获取的queue上的锁，让消费者线程可以获取该锁，然后获取队列里的元素
                    try {
                        queue.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                //空闲则生成元素，并通知消费者消费
                queue.add(ele);
                queue.notifyAll();
            }
        }

    }

    /**
     * 消费者线程
     */
    public static class ConsumerThread extends Thread {
        @Override
        public void run() {
            synchronized (queue) {
                //消费队列为空
                while (queue.size() == 0) {
                    try {
                        //挂起当前线程，并释放通过同步块获取的queue上的锁，让生产者线程可以获取该锁，将生产元素放进队列
                        queue.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                //消费元素，并唤醒生产者线程
                queue.take();
                queue.notifyAll();
            }
        }
    }
```

当前线程调用共享变量的wait()方法后指挥释放当前共享变量上的锁，如果当前线程还持有其他共享变量的锁，则这些锁是不会被释放的

```java
public class WaitDemoLock {

    //创建资源
    private static volatile Object resourceA = new Object();
    private static volatile Object resourceB = new Object();

    public static void main(String[] args) throws InterruptedException {
        //创建线程
        Thread threadA = new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (resourceA) {
                    System.out.println("ThreadA get resourceA lock");
                    //获取resourceB共享资源的监视器锁
                    synchronized (resourceB) {
                        System.out.println("ThreadA get resourceB lock");
                        //线程A阻塞，并释放获取到的resourceA的锁
                        System.out.println("ThreadA release resourceA lock");
                        try {
                            resourceA.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });

        //创建线程
        Thread threadB = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //休眠一秒
                    Thread.sleep(1000);
                    //获取resourceA共享资源的监视器锁
                    synchronized (resourceA) {
                        System.out.println("ThreadB get resourceA lock");
                        System.out.println("ThreadB try get resourceB lock...");
                        //获取resourceA共享资源的监视器锁
                        synchronized (resourceB) {
                            System.out.println("ThreadB get resourceB lock");
                            //线程B阻塞，并释放获取到的resourceA的锁
                            System.out.println("ThreadA release resourceA lock");
                            resourceA.wait();
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        //启动线程
        threadA.start();
        threadB.start();
        //等待两个线程执行完
        threadA.join();
        threadB.join();
        System.out.println("main over");
    }
    
}


ThreadA get resourceA lock
ThreadA get resourceB lock
ThreadA release resourceA lock
ThreadB get resourceA lock
ThreadB try get resourceB lock...
```

当一个线程调用共享对象的wait()方法被阻塞挂起后，如果其他线程中断了该线程，则该线程会抛出InterruptedException异常并返回 

```java
public class WaitNotifyInterrupt {

    static Object obj = new Object();

    public static void main(String[] args) throws InterruptedException {
        //创建线程
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    System.out.println("begin");
                    //阻塞当前线程
                    synchronized (obj) {
                        obj.wait();
                    }
                    System.out.println("end");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
        Thread.sleep(1000);
        System.out.println("bengin interrupt thread");
        thread.interrupt();
        System.out.println("end interrupt");
    }
}
```

#### wait(long timeout)函数

* 与wait()相比多了个超时参数，如果一个线程调用共享对象该方法挂起后，没有在指定的timeout ms时间内被其他线程调用该共享变量的notify()或者notifyAll()方法唤醒，那么该函数还是会因为超时而返回。
* wait()内部方法调用了wait(0)。
* 如果传入负数的timeout，则会抛出IllrgalArgumentExcetion异常

#### wait(long timeout, int nanos)函数

wait(long timeout, int nanos)方法提供比wait(long timeout)更好的时间控制
1ms= 1000 000ns ，其中wait的timeout都是ms单位

 ```java
public final void wait(long timeout, int nanos) throws nterruptedException {
        if (timeout < 0) {
            throw new IllegalArgumentException("timeout value is negative");
        }
	//nanos 单位为纳秒,  1毫秒 = 1000 微秒 = 1000 000 纳秒
        if (nanos < 0 || nanos > 999999) {
            throw new IllegalArgumentException(
                                "nanosecond timeout value out of range");
        }
		
       //  nanos 大于 500000 即半毫秒  就timout 加1毫秒
       //  特殊情况下: 如果timeout为0且nanos大于0,则timout加1毫秒
        if (nanos >= 500000 || (nanos != 0 && timeout == 0)) {
            timeout++;
        }

        wait(timeout);
 ```

#### notify()函数



#### notifyAll函数



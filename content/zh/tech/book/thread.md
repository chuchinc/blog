---
title: "Java并发编程之美"
date: 2020-11-29T16:07:26+08:00
draft: false
tags: ["Java","并发编程"]
---

![20201209-214025-0939.jpg](https://gitee.com/chuchin/img/raw/master/20201209-214025-0939.jpg)

该书在微信读书中上架，可以免费阅读，代码：[https://github.com/chuchinc/kl-book-resource](https://github.com/chuchinc/kl-book-resource)

## 并发编程线程基础

### 什么是线程

* 进程是代码在数据集合上的一次运行活动，是系统进行资源分配和调度的基本单位

* 线程则是进程的一个执行路径，一个进程中至少有一个线程，进程中的多个线程共享进程的资源,是CPU分配资源和调度的基本单位

在Java中，当我们启动main函数时其实就启动了一个JVM的进程，而main函数所在的线程就是这个进程中的一个线程，也称主线程。

进程和线程的关系如图：

![20201209-212626-0292.jpg](https://gitee.com/chuchin/img/raw/master/20201209-212626-0292.jpg)

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

一个线程调用共享对象的notify()方法后，会唤醒一个在该共享变量上调用wait系列方法后被挂起的线程。一个共享变量上可能会有多个线程在等待，具体唤醒哪个等待线程是随机的。

#### notifyAll函数

notifyAll()方法会唤醒所有在该共享变量上由于被wait()方法而被挂起的线程

下面举个例子来说明notify()和notifyAll()方法的具体含义及一些需要注意的地方

```java
public class NotifyAndNotifyAll {
    //创建资源
    private static volatile Object resourceA = new Object();

    public static void main(String[] args) throws InterruptedException {
        //创建线程
        Thread threadA = new Thread(new Runnable() {
            @Override
            public void run() {
                //获取resourceA共享的监视器锁
                synchronized (resourceA) {
                    try {
                        System.out.println("ThreadA get resourceA lock");
                        System.out.println("ThreadA begin wait");
                        resourceA.wait();
                        System.out.println("ThreadA end wait");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        Thread threadB = new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (resourceA) {
                    try {
                        System.out.println("ThreadB get resourceA lock");
                        System.out.println("ThreadB begin wait");
                        resourceA.wait();
                        System.out.println("ThreadB end wait");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        Thread threadC = new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (resourceA) {
                    System.out.println("threadC begin notify");
                    resourceA.notify();
                }
            }
        });
        //启动线程
        threadA.start();
        threadB.start();
        Thread.sleep(1000);
        threadC.start();
        //等待线程结束
        threadA.join();
        threadB.join();
        threadC.join();
        System.out.println("main over");
    }
}

ThreadA get resourceA lock
ThreadA begin wait
ThreadB get resourceA lock
ThreadB begin wait
threadC begin notify
ThreadA end wait
```

把notify()改成notifyAll()后，输出结果变为：

```java
ThreadA get resourceA lock
ThreadA begin wait
ThreadB get resourceA lock
ThreadB begin wait
threadC begin notify
ThreadB end wait
ThreadA end wait
main over
```

### 等待线程执行终止的join方法

在项目实战中经常遇到一个场景，就是需要等待某几件事情完成之后才能继续往下执行，比如多个线程全部加载完毕再汇总处理

* join方法是Thread提供的
* join是无参且返回值为void的方法

```java
public class JoinDemo {

    public static void main(String[] args) throws InterruptedException {
        Thread threadA = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("ThreadA over");
            }
        });

        Thread threadB = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("ThreadB over");
            }
        });
        //启动子线程
        threadA.start();
        threadB.start();
        System.out.println("wait all child thread over");
        //等待子线程执行完毕，返回
        threadA.join();
        threadB.join();
        System.out.println("all child thread over");
    }
}


wait all child thread over
ThreadB over
ThreadA over
all child thread over
```

### 让线程休眠的sleep方法

Thread类中有一个静态的sleep方法，当一个执行中的线程调用了Thread的sleep方法后，调用线程会暂时让出指定时间的执行权，也就是在这期间不参与CPU的调度，但是该线程所拥有的监视器资源，比如锁还是持有不让出的。指定的睡眠时间到了后该函数会正常返回，线程就处于就绪状态，然后参与CPU的调度，获取到CPU资源后就可以继续运行了。如果在睡眠期间其他线程调用了该线程的interrupt（）方法中断了该线程，则该线程会在调用sleep方法的地方抛出InterruptedException异常而返回。

线程在睡眠时拥有的监视器资源不会被释放

```java
public class SleepNotReleaseLock {
    //创建一个独占锁
    private static final Lock lock = new ReentrantLock();

    public static void main(String[] args) {
        Thread threadA = new Thread(new Runnable() {
            @Override
            public void run() {
                //获取独占锁
                lock.lock();
                try {
                    System.out.println("child threadA is in sleep");
                    Thread.sleep(10000);
                    System.out.println("child threadA is in awaked");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    lock.unlock();
                }
            }
        });

        Thread threadB = new Thread(new Runnable() {
            @Override
            public void run() {
                //获取独占锁
                lock.lock();
                try {
                    System.out.println("child threadB is in sleep");
                    Thread.sleep(10000);
                    System.out.println("child threadB is in awaked");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    lock.unlock();
                }
            }
        });
        threadA.start();
        threadB.start();
    }
}

child threadA is in sleep
child threadA is in awaked
child threadB is in sleep
child threadB is in awaked
```

如上代码首先创建了一个独占锁，然后创建了两个线程，每个线程在内部先获取锁，然后睡眠，睡眠结束后会释放锁。首先，无论你执行多少遍上面的代码都是线程A先输出或者线程B先输出，不会出现线程A和线程B交叉输出的情况。从执行结果来看，线程A先获取了锁，那么线程A会先输出一行，然后调用sleep方法让自己睡眠10s，在线程A睡眠的这10s内那个独占锁lock还是线程A自己持有，线程B会一直阻塞直到线程A醒来后执行unlock释放锁。

下面再来看一下，当一个线程处于睡眠状态时，如果另外一个线程中断了它，会不会在调用sleep方法处抛出异常。

```java
public class SleepInterrupted {

    public static void main(String[] args) throws InterruptedException {
        Thread threadA = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    System.out.println("child threadA is in sleep");
                    Thread.sleep(10000);
                    System.out.println("child threadA is in awaked");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        threadA.start();
        Thread.sleep(2000);
        //主线程中断子线程
        threadA.interrupt();
    }
}
```

子线程在睡眠期间，主线程中断了它，所以子线程在调用sleep方法处抛出了InterruptedException异常。

### 让出CPU执行权的yield方法

当一个线程调用yield方法时，当前线程会让出CPU使用权，然后处于就绪状态，线程调度器会从线程就绪队列里面获取一个线程优先级最高的线程，当然也有可能会调度到刚刚让出CPU的那个线程来获取CPU执行权。

```java
public class YieldTest implements Runnable{

    public static void main(String[] args) {
        new YieldTest();
        new YieldTest();
        new YieldTest();
    }

    public YieldTest() {
        Thread thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        for (int i = 0; i < 5; i++) {
            //当i=0时让出CPU执行权，放弃时间片，进行下一轮调度
            if ((i%5) == 0) {
                System.out.println(Thread.currentThread() + "yield cpu...");
                //当前线程让出CPU执行权，放弃时间片，进行下一轮调度
//                        Thread.yield();
            }
        }
        System.out.println(Thread.currentThread() + "is over");
    }
}

Thread[Thread-0,5,main]yield cpu...
Thread[Thread-0,5,main]is over
Thread[Thread-1,5,main]yield cpu...
Thread[Thread-1,5,main]is over
Thread[Thread-2,5,main]yield cpu...
Thread[Thread-2,5,main]is over
```

代码开启了三个线程，每个线程的功能都一样，都是在for循环中执行5次打印。解开Thread.yield（）注释再执行，结果如下：

```java
Thread[Thread-0,5,main]yield cpu...
Thread[Thread-2,5,main]yield cpu...
Thread[Thread-1,5,main]yield cpu...
Thread[Thread-2,5,main]is over
Thread[Thread-0,5,main]is over
Thread[Thread-1,5,main]is over
```

从结果可知，Thread.yield（）方法生效了，三个线程分别在i=0时调用了Thread.yield（）方法，所以三个线程自己的两行输出没有在一起，因为输出了第一行后当前线程让出了CPU执行权。

一般很少使用这个方法，在调试或者测试时这个方法或许可以帮助复现由于并发竞争条件导致的问题，其在设计并发控制时或许会有用途.

总结：sleep与yield方法的区别在于，当线程调用sleep方法时调用线程会被阻塞挂起指定的时间，在这期间线程调度器不会去调度该线程。而调用yield方法时，线程只是让出自己剩余的时间片，并没有被阻塞挂起，而是处于就绪状态，线程调度器下一次调度时就有可能调度到当前线程执行。

### 线程中断

Java中的线程中断是一种线程间的协作模式，通过设置线程的中断标志并不能直接终止该线程的执行，而是被中断的线程根据中断状态自行处理。

* void interrupt（） 如果线程A因为调用了wait系列函数、join方法或者sleep方法而被阻塞挂起，这时候若线程B调用线程A的interrupt（）方法，线程A会在调用这些方法的地方抛出InterruptedException异常而返回。
* boolean isInterrupted（）检测当前线程是否被中断，如果是返回true，否则返回false。
* boolean interrupted（）检测当前线程是否被中断，如果是返回true，否则返回false。与isInterrupted不同的是，该方法如果发现当前线程被中断，则会清除中断标志，并且该方法是static方法，可以通过Thread类直接调用

线程使用Interrupted优雅退出的经典例子:

```java
@Override
public void run() {
    try {
        //线程退出的条件
         while(! Thread.currentThread().isInterrupted() && more work to do) {
                  Thread.sleep(1000);
                //do more work
           }
         }catch (InterruptedException e) {
           e.printStackTrace();
           } finally {
                    //cleanup,if required
           
}
```

根据中断标志判断线程是否终止的例子:

```java
public class InterruptedJudge {
    public static void main(String[] args) throws InterruptedException {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                //如果当前线程被中断则退出循环
                while (! Thread.currentThread().isInterrupted()) {
                    System.out.println(Thread.currentThread() + "hello");
                }
            }
        });
        thread.start();
        Thread.sleep(1000);
        System.out.println("main thread interrupt thread");
        thread.interrupt();
        thread.join();
        System.out.println("main is over");
    }
}



Thread[Thread-0,5,main]hello
main thread interrupt thread
Thread[Thread-0,5,main]hello
Thread[Thread-0,5,main]hello
Thread[Thread-0,5,main]hello
Thread[Thread-0,5,main]hello
main is over
```

打断休眠

```java
public class InterruptedSleep {
    public static void main(String[] args) throws InterruptedException {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    System.out.println("begin sleep");
                    Thread.sleep(2000000000);
                    System.out.println("awake");
                } catch (InterruptedException e) {
                    System.out.println("thread is interrupted while sleep");
                    return;
                }
                System.out.println("thread-leaving normally");
            }
        });
        thread.start();
        Thread.sleep(1000);
        thread.interrupt();
        thread.join();
        System.out.println("main is over");
    }
}


begin sleep
thread is interrupted while sleep
main is over
```

interrupted（）与isInterrupted（）方法的不同之处：

```java
public class InterruptedAndIsInterrupted {
    public static void main(String[] args) throws InterruptedException {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                for (; ; ) {

                }
            }
        });
        //启动线程
        thread.start();
        //设置中断标志
        thread.interrupt();
        //获取中断标志
        System.out.println("isInterrupted:" + thread.isInterrupted());
        //获取中断标志并重置
        System.out.println("isInterrupted:" + thread.interrupted());
        //获取中断标志并重置
        System.out.println("isInterrupted:" + Thread.interrupted());
        //获取中断标志
        System.out.println("isInterrupted:" + thread.isInterrupted());
        thread.join();
        System.out.println("main thread is over");
    }
}

isInterrupted:true
isInterrupted:false
isInterrupted:false
isInterrupted:true
```

这里虽然调用了threadOne的interrupted（）方法，但是获取的是主线程的中断标志，因为主线程是当前线程。threadOne.interrupted（）和Thread.interrupted（）方法的作用是一样的，目的都是获取当前线程的中断标志。修改上面的例子为如下：

```java
public class InterruptedRemove {
    public static void main(String[] args) throws InterruptedException {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                //中断标志为true时会退出循环，并且清除中断标志
                while (!Thread.currentThread().isInterrupted()){

                }
                System.out.println("thread is interrupted :" + Thread.currentThread().isInterrupted());
            }
        });
        //启动线程
        thread.start();
        //设置中断标志
        thread.interrupt();
        thread.join();
        System.out.println("main thread is over");
    }
}

thread is interrupted :true
main thread is over
```

由输出结果可知，调用interrupted（）方法后中断标志被清除了

### 理解线程上下文切换

在多线程编程中，线程个数一般都大于CPU个数，而每个CPU同一时刻只能被一个线程使用，为了让用户感觉多个线程是在同时执行的，CPU资源的分配采用了时间片轮转的策略，也就是给每个线程分配一个时间片，线程在时间片内占用CPU执行任务。**当前线程使用完时间片后，就会处于就绪状态并让出CPU让其他线程占用**，这就是上下文切换。

从当前线程的上下文切换到了其他线程。那么就有一个问题，让出CPU的线程等下次轮到自己占有CPU时如何知道自己之前运行到哪里了？所以在切换线程上下文时需要保存当前线程的执行现场，当再次执行时根据保存的执行现场信息恢复执行现场。

线程上下文切换时机有：当前线程的CPU时间片使用完处于就绪状态时，当前线程被其他线程中断时。

### 线程死锁

#### 什么是线程死锁

死锁是指两个或两个以上的线程在执行过程中，因争夺资源而造成的互相等待的现象，在无外力作用的情况下，这些线程会一直相互等待而无法继续运行下去

死锁的产生必须具备以下四个条件：

* **互斥条件**：指线程对已经获取到的资源进行排它性使用，即该资源同时只由一个线程占用。如果此时还有其他线程请求获取该资源，则请求者只能等待，直至占有资源的线程释放该资源。
* **请求并持有条件**：指一个线程已经持有了至少一个资源，但又提出了新的资源请求，而新资源已被其他线程占有，所以当前线程会被阻塞，但阻塞的同时并不释放自己已经获取的资源。
* **不可剥夺条件**：指线程获取到的资源在自己使用完之前不能被其他线程抢占，只有在自己使用完毕后才由自己释放该资源。
* **环路等待条件**：指在发生死锁时，必然存在一个线程—资源的环形链，即线程集合{T0, T1, T2,…, Tn}中的T0正在等待一个T1占用的资源，T1正在等待T2占用的资源，……Tn正在等待已被T0占用的资源。

通过一个例子来说明线程死锁：

```java
public class Deadlock {
    //创建资源
    private static Object resourceA = new Object();
    private static Object resourceB = new Object();

    public static void main(String[] args) {
        Thread threadA = new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (resourceA) {
                    System.out.println(Thread.currentThread() + "get resourceA");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println(Thread.currentThread() + "waiting for resourceB");
                    synchronized (resourceB) {
                        System.out.println(Thread.currentThread() + "get resourceB");
                    }
                }
            }
        });

        Thread threadB = new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (resourceB) {
                    System.out.println(Thread.currentThread() + "get resourceB");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println(Thread.currentThread() + "waiting for resourceA");
                    synchronized (resourceA) {
                        System.out.println(Thread.currentThread() + "get resourceA");
                    }
                }
            }
        });
        threadA.start();
        threadB.start();
    }
}


Thread[Thread-0,5,main]get resourceA
Thread[Thread-1,5,main]get resourceB
Thread[Thread-1,5,main]waiting for resourceA
Thread[Thread-0,5,main]waiting for resourceB
```

本例是如何满足死锁的四个条件的：

首先，resourceA和resourceB都是互斥资源，当线程A调用synchronized（resourceA）方法获取到resourceA上的监视器锁并释放前，线程B再调用synchronized（resourceA）方法尝试获取该资源会被阻塞，只有线程A主动释放该锁，线程B才能获得，这满足了资源互斥条件。线程A首先通过synchronized（resourceA）方法获取到resourceA上的监视器锁资源，然后通过synchronized（resourceB）方法等待获取resourceB上的监视器锁资源，这就构成了请求并持有条件。线程A在获取resourceA上的监视器锁资源后，该资源不会被线程B掠夺走，只有线程A自己主动释放resourceA资源时，它才会放弃对该资源的持有权，这构成了资源的不可剥夺条件。线程A持有objectA资源并等待获取objectB资源，而线程B持有objectB资源并等待objectA资源，这构成了环路等待条件。所以线程A和线程B就进入了死锁状态。

#### 如何避免线程死锁

要想避免死锁，只需要破坏掉至少一个构造死锁的必要条件即可，但是学过操作系统的读者应该都知道，目前只有请求并持有和环路等待条件是可以被破坏的。

造成死锁的原因其实和申请资源的顺序有很大关系，使用资源申请的有序性原则就可以避免死锁，那么什么是资源申请的有序性呢？我们对上面线程B的代码进行如下修改。

```java
Thread threadB = new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (resourceA) {
                    System.out.println(Thread.currentThread() + "get resourceA");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println(Thread.currentThread() + "waiting for resourceB");
                    synchronized (resourceB) {
                        System.out.println(Thread.currentThread() + "get resourceB");
                    }
                }
            }
        });

Thread[Thread-0,5,main]get resourceA
Thread[Thread-0,5,main]waiting for resourceB
Thread[Thread-0,5,main]get resourceB
Thread[Thread-1,5,main]get resourceA
Thread[Thread-1,5,main]waiting for resourceB
Thread[Thread-1,5,main]get resourceB
```

如上代码让在线程B中获取资源的顺序和在线程A中获取资源的顺序保持一致，其实资源分配有序性就是指，假如线程A和线程B都需要资源1,2,3, ..., n时，对资源进行排序，线程A和线程B只有在获取了资源n-1时才能去获取资源n。

我们可以简单分析一下为何资源的有序分配会避免死锁，比如上面的代码，假如线程A和线程B同时执行到了synchronized（resourceA），只有一个线程可以获取到resourceA上的监视器锁，假如线程A获取到了，那么线程B就会被阻塞而不会再去获取资源B，线程A获取到resourceA的监视器锁后会去申请resourceB的监视器锁资源，这时候线程A是可以获取到的，线程A获取到resourceB资源并使用后会放弃对资源resourceB的持有，然后再释放对resourceA的持有，释放resourceA后线程B才会被从阻塞状态变为激活状态。所以资源的有序性破坏了资源的请求并持有条件和环路等待条件，因此避免了死锁。

### 守护线程与用户线程

Java中的线程分为两类，分别为daemon线程（守护线程）和user线程（用户线程）。在JVM启动时会调用main函数，main函数所在的线程就是一个用户线程，其实在JVM内部同时还启动了好多守护线程，比如垃圾回收线程。

那么守护线程和用户线程有什么区别呢？区别之一是当最后一个非守护线程结束时，JVM会正常退出，而不管当前是否有守护线程，也就是说守护线程是否结束并不影响JVM的退出。言外之意，只要有一个用户线程还没结束，正常情况下JVM就不会退出。

创建守护线程：

```java
public class DaemonThread {
    public static void main(String[] args) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
            }
        });
        //设置为守护线程
        thread.setDaemon(true);
        thread.start();
    }
}
```

用户线程与守护线程的区别：

```java
public class DaemonAndUserThread {
    public static void main(String[] args) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                for (;;) {}
            }
        });
        //启动子线程
        thread.start();
        System.out.println("main thread is over ");
    }
}
```

如上代码在main线程中创建了一个thread线程，在thread线程里面是一个无限循环。从运行代码的结果看，main线程已经运行结束了，那么JVM进程已经退出了吗？在IDE的输出结果右上侧的红色方块说明，JVM进程并没有退出。

这个结果说明了当父线程结束后，子线程还是可以继续存在的，也就是子线程的生命周期并不受父线程的影响。

把上面的thread线程设置为守护线程后：

```java
thread.setDaemon(true);
thread.start();

main thread is over
```

在这个例子中，main函数是唯一的用户线程，thread线程是守护线程，当main线程运行结束后，JVM发现当前已经没有用户线程了，就会终止JVM进程。由于这里的守护线程执行的任务是一个死循环，这也说明了如果当前进程中不存在用户线程，但是还存在正在执行任务的守护线程，则JVM不等守护线程运行完毕就会结束JVM进程。

main线程运行结束后，JVM会自动启动一个叫作DestroyJavaVM的线程，该线程会等待所有用户线程结束后终止JVM进程。

总结：**如果你希望在主线程结束后JVM进程马上结束，那么在创建线程时可以将其设置为守护线程，如果你希望在主线程结束后子线程继续工作，等子线程结束后再让JVM进程结束，那么就将子线程设置为用户线程**。

### ThreadLocal

多线程访问同一个共享变量时特别容易出现并发问题，特别是在多个线程需要对一个共享变量进行写入时。

![20201209-212727-0056.png](https://gitee.com/chuchin/img/raw/master/20201209-212727-0056.png)

ThreadLocal是JDK包提供的，它提供了线程本地变量，也就是如果你创建了一个ThreadLocal变量，那么访问这个变量的每个线程都会有这个变量的一个本地副本。当多个线程操作这个变量时，实际操作的是自己本地内存里面的变量，从而避免了线程安全问题。创建一个ThreadLocal变量后，每个线程都会复制一个变量到自己的本地内存。

![20201209-211228-0911.png](https://gitee.com/chuchin/img/raw/master/20201209-211228-0911.png)

#### ThreadLocal使用示例

```java
public class ThreadLocalTest {

    //创建thradlocal变量
    static ThreadLocal<String> localVariable = new ThreadLocal<>();

    /**
     * print函数
     * @param string
     */
    static void print(String string) {
        //打印当前线程本地内存中localVariable变量的值
        System.out.println(string + ":" + localVariable.get());
        //清除当前线程本地内存中的localVariable变量
        localVariable.remove();
    }

    public static void main(String[] args) {
        Thread threadOne = new Thread(new Runnable() {
            @Override
            public void run() {
                localVariable.set("threadOne local variable");
                print("threadOne");
                System.out.println("threadOne remove after" + ":" + localVariable.get());
            }
        });

        Thread threadTwo = new Thread(new Runnable() {
            @Override
            public void run() {
                localVariable.set("threadTwo local variable");
                print("threadTwo");
                System.out.println("threadTwo remove after" + ":" + localVariable.get());
            }
        });

        threadOne.start();
        threadTwo.start();
    }
}


threadTwo:threadTwo local variable
threadOne:threadOne local variable
threadTwo remove after:threadTwo local variable
threadOne remove after:threadOne local variable
```

线程One中的代码3.1通过set方法设置了localVariable的值，这其实设置的是线程One本地内存中的一个副本，这个副本线程Two是访问不了的。然后代码3.2调用了print函数，代码1.1通过get函数获取了当前线程（线程One）本地内存中localVariable的值。

打开代码1.2的注释后，再次运行，运行结果如下

```java
threadOne:threadOne local variable
threadTwo:threadTwo local variable
threadOne remove after:null
threadTwo remove after:null
```

#### ThreadLocal的实现原理

ThreadLocal相关类的类图结构：

![20201209-215028-0677.png](https://gitee.com/chuchin/img/raw/master/20201209-215028-0677.png)

Thread类中有一个threadLocals和一个inheritableThreadLocals，它们都是ThreadLocalMap类型的变量，而ThreadLocalMap是一个定制化的Hashmap。在默认情况下，每个线程中的这两个变量都为null，只有当前线程第一次调用ThreadLocal的set或者get方法时才会创建它们。其实每个线程的本地变量不是存放在ThreadLocal实例里面，而是存放在调用线程的threadLocals变量里面。也就是说，**ThreadLocal类型的本地变量存放在具体的线程内存空间中。ThreadLocal就是一个工具壳，它通过set方法把value值放入调用线程的threadLocals里面并存放起来，当调用线程调用它的get方法时，再从当前线程的threadLocals变量里面将其拿出来使用。如果调用线程一直不终止，那么这个本地变量会一直存放在调用线程的threadLocals变量里面**，所以当不需要使用本地变量时可以通过调用ThreadLocal变量的remove方法，从当前线程的threadLocals里面删除该本地变量。另外，Thread里面的threadLocals为何被设计为map结构？很明显是因为每个线程可以关联多个ThreadLocal变量。、

1. **void set(T value)**

```java
public void set(T value) {
    //(1)获取当前线程
    Thread t = Thread.currentThread();
    //(2)将当前线程作为key，去查找对应的线程变量，找到则设置
    ThreadLocalMap map = getMap(t);
    if (map != null)
        map.set(this,value);
    else
    //(3)第一次调用就创建当前线程对应的HashMap
        createMap(t, value);
}
```

代码（1）首先获取调用线程，然后使用当前线程作为参数调用getMap（t）方法，getMap（Thread t）的代码如下。

```java
ThreadLocalMap getMap(Thread t) {
    return t.threadLocals;
}
```

可以看到，getMap（t）的作用是获取线程自己的变量threadLocals,threadlocal变量被绑定到了线程的成员变量上。如果getMap（t）的返回值不为空，则把value值设置到threadLocals中，也就是把当前变量值放入当前线程的内存变量threadLocals中。threadLocals是一个HashMap结构，其中key就是当前ThreadLocal的实例对象引用，value是通过set方法传递的值。如果getMap（t）返回空值则说明是第一次调用set方法，这时创建当前线程的threadLocals变量。下面来看createMap（t, value）做什么。

```java
void createMap(Thread t, T firstValue) {
    th.threadLocals = new ThreadLocalMap(this, firstValue);
}
```

它创建当前线程的threadLocals变量。

2. **T get( )**

```java
public T get() {
    //(4)获取当前线程
    Thread t = Thread.currentThread();
    //(5)获取当前线程的threadLocals变量
    ThreadLocalMap map = getMap(t);
    //(6)如果threadLocals不为null,则返回对应本地变量的值
    if (e != null) {
        @SuppressWarnings("unchecked")
        T result = (T)e.value;
        return result;
    }
    //(7)threadLocals为空则初始化当前线程的threadLocals成员变量
    return setInitialValue();
}
```

代码（4）首先获取当前线程实例，如果当前线程的threadLocals变量不为null，则直接返回当前线程绑定的本地变量，否则执行代码（7）进行初始化。setInitialValue（）的代码如下。

```java
 private T setInitialValue() {
     //(8)初始化为null
        T value = initialValue();
        Thread t = Thread.currentThread();
        ThreadLocalMap map = getMap(t);
     //(9)如果当前线程的threadLocals变量不为空
        if (map != null)
            map.set(this, value);
        else
            //(10)如果当前线程的threadLocals变量不为空
            createMap(t, value);
        return value;
    }
```

如果当前线程的threadLocals变量不为空，则设置当前线程的本地变量值为null，否则调用createMap方法创建当前线程的createMap变量。

3. **void remove( )**

```java
public void remove() {
         ThreadLocalMap m = getMap(Thread.currentThread());
         if (m != null)
             m.remove(this);
}
```

如以上代码所示，如果当前线程的threadLocals变量不为空，则删除当前线程中指定ThreadLocal实例的本地变量。

**总结**：在每个线程内部都有一个名为threadLocals的成员变量，该变量的类型为HashMap，其中key为我们定义的ThreadLocal变量的this引用，value则为我们使用set方法设置的值。每个线程的本地变量存放在线程自己的内存变量threadLocals中，如果当前线程一直不消亡，那么这些本地变量会一直存在，所以可能会造成内存溢出，因此使用完毕后要记得调用ThreadLocal的remove方法删除对应线程的threadLocals中的本地变量。

![20201209-215930-0274.png](https://gitee.com/chuchin/img/raw/master/20201209-215930-0274.png)

#### ThreadLocal不支持继承性

```java
public class ThreadLocalExtendsTest {
    //（1）创建线程变量
    public static ThreadLocal<String> threadLocal = new ThreadLocal<String>();

    public static void main(String[] args) {
        //（2）设置线程变量
        threadLocal.set("hello world");
        //(3)启动子线程
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                //(4)子线程输出线程变量的值
                System.out.println("thread:" + threadLocal.get());
            }
        });
        thread.start();
        System.out.println("main:" + threadLocal.get());
    }
}


main:hello world
thread:null
```

也就是说，同一个ThreadLocal变量在父线程中被设置值后，在子线程中是获取不到的。根据上节的介绍，这应该是正常现象，**因为在子线程thread里面调用get方法时当前线程为thread线程，而这里调用set方法设置线程变量的是main线程，两者是不同的线程，自然子线程访问时返回null**。那么有没有办法让子线程能访问到父线程中的值？答案是有。

#### InheritableThreadLocal类

InheritableThreadLocal继承自ThreadLocal，其提供了一个特性，就是让子线程可以访问在父线程中设置的本地变量。

```java
public class InheritableThreadLocal<T> extends ThreadLocal<T> {
    //(1)
    protected T childValue(T parentValue) {
        return parentValue;
    }
    //(2)
    ThreadLocalMap getMap(Thread t) {
       return t.inheritableThreadLocals;
    }
    //(3)
    void createMap(Thread t, T firstValue) {
        t.inheritableThreadLocals = new ThreadLocalMap(this, firstValue);
    }
}
```

InheritableThreadLocal继承了ThreadLocal，并重写了三个方法。由代码（3）可知，InheritableThreadLocal重写了createMap方法，那么现在当第一次调用set方法时，创建的是当前线程的inheritableThreadLocals变量的实例而不再是threadLocals。由代码（2）可知，当调用get方法获取当前线程内部的map变量时，获取的是inheritableThreadLocals而不再是threadLocals。

**总结**：InheritableThreadLocal类通过重写代码（2）和（3）让本地变量保存到了具体线程的inheritableThreadLocals变量里面，那么线程在通过InheritableThreadLocal类实例的set或者get方法设置变量时，就会创建当前线程的inheritableThreadLocals变量。当父线程创建子线程时，构造函数会把父线程中inheritableThreadLocals变量里面的本地变量复制一份保存到子线程的inheritableThreadLocals变量里面。

代码（1）修改为

```java
//（1）创建线程变量
//    public static ThreadLocal<String> threadLocal = new ThreadLocal<String>();
    public static ThreadLocal<String> threadLocal = new InheritableThreadLocal<>();


main:hello world
thread:hello world
```

可见，现在可以从子线程正常获取到线程变量的值了。

那么在什么情况下需要子线程可以获取父线程的threadlocal变量呢？情况还是蛮多的，比如子线程需要使用存放在threadlocal变量中的用户登录信息，再比如一些中间件需要把统一的id追踪的整个调用链路记录下来。其实子线程使用父线程中的threadlocal方法有多种方式，比如创建线程时传入父线程中的变量，并将其复制到子线程中，或者在父线程中构造一个map作为参数传递给子线程，但是这些都改变了我们的使用习惯，所以在这些情况下InheritableThreadLocal就显得比较有用。

## 并发编程的其他基础知识

### 什么是多线程并发编程

* 并发是指同一个时间段内多个任务同时都在执行，并且都没有执行结束
* 并行是说在单位时间内多个任务同时在执行

在单CPU的时代多个任务都是并发执行的，这是因为单个CPU同时只能执行一个任务。在单CPU时代多任务是共享一个CPU的，当一个任务占用CPU运行时，其他任务就会被挂起，当占用CPU的任务时间片用完后，会把CPU让给其他任务来使用，所以在单CPU时代多线程编程是没有太大意义的，并且线程间频繁的上下文切换还会带来额外开销。

单个CPU上运行两个线程，线程A和线程B是轮流使用CPU进行任务处理的，也就是在某个时间内单个CPU只执行一个线程上面的任务。当线程A的时间片用完后会进行线程上下文切换，也就是保存当前线程A的执行上下文，然后切换到线程B来占用CPU运行任务

![20201209-210739-0803.png](https://gitee.com/chuchin/img/raw/master/20201209-210739-0803.png)

双CPU配置，线程A和线程B各自在自己的CPU上执行任务，实现了真正的并行运行

![20201209-214344-0353.png](https://gitee.com/chuchin/img/raw/master/20201209-214344-0353.png)

而在多线程编程实践中，线程的个数往往多于CPU的个数，所以一般都称多线程并发编程而不是多线程并行编程

### 为什么要进行多线程并发编程

多核CPU时代的到来打破了单核CPU对多线程效能的限制。多个CPU意味着每个线程可以使用自己的CPU运行，这减少了线程上下文切换的开销，但随着对应用系统性能和吞吐量要求的提高，出现了处理海量数据和请求的要求，这些都对高并发编程有着迫切的需求

### Java 中的线程安全问题

**共享资源**，就是说该资源被多个线程所持有或者说多个线程都可以去访问该资源

**线程安全问题**是指当多个线程同时读写一个共享资源并且没有任何同步措施时，导致出现脏数据或者其他不可预见的结果的问题

![20201209-213347-0888.png](https://gitee.com/chuchin/img/raw/master/20201209-213347-0888.png)

线程A和线程B可以同时操作主内存中的共享变量，那么线程安全问题和共享资源之间是什么关系呢？是不是说多个线程共享了资源，当它们都去访问这个共享资源时就会产生线程安全问题呢？答案是否定的，如果多个线程都只是**读取**共享资源，而**不去修改**，那么就不会存在线程安全问题，只有当至少一个线程修改共享资源时才会存在线程安全问题。最典型的就是计数器类的实现，计数变量count本身是一个共享变量，多个线程可以对其进行递增操作，如果不使用同步措施，由于递增操作是获取—计算—保存三步操作，因此可能导致计数不准确，如下所示。

![20201209-211149-0817.png](https://gitee.com/chuchin/img/raw/master/20201209-211149-0817.png)

假如当前count=0，在t1时刻线程A读取count值到本地变量countA。然后在t2时刻递增countA的值为1，同时线程B读取count的值0到本地变量countB，此时countB的值为0（因为countA的值还没有被写入主内存）。在t3时刻线程A才把countA的值1写入主内存，至此线程A一次计数完毕，同时线程B递增CountB的值为1。在t4时刻线程B把countB的值1写入内存，至此线程B一次计数完毕。这里先不考虑内存可见性问题，明明是两次计数，为何最后结果是1而不是2呢？其实这就是共享变量的线程安全问题。那么如何来解决这个问题呢？这就需要在线程访问共享变量时进行适当的同步，在Java中最常见的是使用关键字**synchronized**进行同步。

### Java 中共享变量的内存可见性问题

多线程下处理共享变量时Java的内存模型

![20201209-212553-0052.png](https://gitee.com/chuchin/img/raw/master/20201209-212553-0052.png)

Java内存模型规定，将所有的变量都存放在**主内存**中，当线程使用变量时，会把主内存里面的变量**复制**到自己的工作空间或者叫作**工作内存**，线程读写变量时操作的是自己工作内存中的变量。Java内存模型是一个抽象的概念，那么在实际实现中线程的工作内存是什么呢？

![20201209-210455-0627.png](https://gitee.com/chuchin/img/raw/master/20201209-210455-0627.png)

图中所示是一个双核CPU系统架构，每个核有自己的控制器和运算器，其中控制器包含一组寄存器和操作控制器，运算器执行算术逻辑运算。每个核都有自己的一级缓存，在有些架构里面还有一个所有CPU都共享的二级缓存。那么Java内存模型里面的工作内存，就对应这里的L1或者L2缓存或者CPU的寄存器

当一个线程操作共享变量时，它首先从主内存复制共享变量到自己的工作内存，然后对工作内存里的变量进行处理，处理完后将变量值更新到主内存

假设线程A和线程B使用不同CPU执行，并且当前两级Cache都为空，那么这时候由于Cache的存在，将会导致内存不可见问题，具体看下面的分析

* 线程A首先获取共享变量X的值，由于两级Cache都没有命中，所以加载主内存中X的值，假如为0。然后把X=0的值缓存到两级缓存，线程A修改X的值为1，然后将其写入两级Cache，并且刷新到主内存。线程A操作完毕后，线程A所在的CPU的两级Cache内和主内存里面的X的值都是1
* 线程B获取X的值，首先一级缓存没有命中，然后看二级缓存，二级缓存命中了，所以返回X= 1；到这里一切都是正常的，因为这时候主内存中也是X=1。然后线程B修改X的值为2，并将其存放到线程2所在的一级Cache和共享二级Cache中，最后更新主内存中X的值为2；到这里一切都是好的
* 线程A这次又需要修改X的值，获取时一级缓存命中，并且X=1，到这里问题就出现了，明明线程B已经把X的值修改为了2，为何线程A获取的还是1呢？这就是共享变量的内存不可见问题，也就是线程B写入的值对线程A不可见

如何解决共享变量内存不可见问题？使用Java中的volatile关键字就可以解决这个问题

### Java 中的 synchronized 关键字

#### synchronized关键字介绍

synchronized块是Java提供的一种**原子性内置锁**，Java中的每个对象都可以把它当作一个**同步锁**来使用，这些Java内置的使用者看不到的锁被称为内部锁，也叫作**监视器锁**。线程的执行代码在进入synchronized代码块前会**自动获取内部锁**，这时候其他线程访问该同步代码块时会被**阻塞挂起**。拿到内部锁的线程会在正常退出同步代码块或者抛出异常后或者在同步块内调用了该内置锁资源的wait系列方法时释放该内置锁。内置锁是**排它锁**，也就是当一个线程获取这个锁后，其他线程必须等待该线程释放锁后才能获取该锁。另外，由于Java中的线程是与操作系统的原生线程一一对应的，**所以当阻塞一个线程时，需要从用户态切换到内核态执行阻塞操作，这是很耗时的操作，而synchronized的使用就会导致上下文切换**

#### synchronized的内存语义

这个内存语义就可以**解决共享变量内存可见性问题**。进入synchronized块的内存语义是把在synchronized块内使用到的变量从线程的工作内存中**清除**，这样在synchronized块内使用到该变量时就不会从线程的工作内存中获取，而是直接从**主内存**中获取。退出synchronized块的内存语义是把在synchronized块内对共享变量的修改**刷新**到主内存。

其实这也是加锁和释放锁的语义，**当获取锁后会清空锁块内本地内存中将会被用到的共享变量**，在使用这些共享变量时从主内存进行加载，**在释放锁时将本地内存中修改的共享变量刷新到主内存**

除可以解决共享变量内存可见性问题外，synchronized经常被用来实现原子性操作。另外请注意，synchronized关键字会引起线程上下文切换并带来线程调度开销

### Java 中的 volatile 关键字

使用锁的方式可以解决共享变量内存可见性问题，但是使用锁太笨重，因为它会带来线程上下文的切换开销。对于解决内存可见性问题，Java还提供了一种弱形式的同步，也就是使用volatile关键字

该关键字可以确保对一个变量的更新对其他线程马上可见

**当一个变量被声明为volatile时，线程在写入变量时不会把值缓存在寄存器或者其他地方，而是会把值刷新回主内存。当其他线程读取该共享变量时，会从主内存重新获取最新值，而不是使用当前线程的工作内存中的值**

volatile的内存语义和synchronized有相似之处，具体来说就是，当线程写入了volatile变量值时就等价于线程退出synchronized同步块（把写入工作内存的变量值同步到主内存），读取volatile变量值时就相当于进入同步块（先清空本地内存变量值，再从主内存获取最新值）

如下代码中的共享变量value是线程不安全的，因为这里没有使用适当的同步措施

```java
public class ThreadNotSafeInteger {

    private int value;

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
```

使用synchronized关键字进行同步的方式

```java
public class ThreadSafeIntegerUseSynchronized {

    private int value;

    public synchronized int getValue() {
        return value;
    }

    public synchronized void setValue(int value) {
        this.value = value;
    }
}
```

使用volatile进行同步

```java
public class ThreadSafeIntegerUseVolatile {

    private volatile int value;

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
```

在这里使用synchronized和使用volatile是等价的，都解决了共享变量value的内存可见性问题，但是前者是独占锁，同时只能有一个线程调用get（）方法，其他调用线程会被阻塞，同时会存在线程上下文切换和线程重新调度的开销，这也是使用锁方式不好的地方。而后者是非阻塞算法，不会造成线程上下文切换的开销

**但并非在所有情况下使用它们都是等价的，volatile虽然提供了可见性保证，但并不保证操作的原子性**

使用volatile关键字的情况

* 写入变量值不依赖变量的当前值时。因为如果依赖当前值，将是获取—计算—写入三步操作，这三步操作不是原子性的，而volatile不保证原子性
* 读写变量值时没有加锁。因为加锁本身已经保证了内存可见性，这时候不需要把变量声明为volatile的

### Java 中的原子操作

所谓原子性操作，是指执行一系列操作时，这些操作要么全部执行，要么全部不执行，不存在只执行其中一部分的情况。在设计计数器时一般都先读取当前值，然后+1，再更新。这个过程是读—改—写的过程，如果不能保证这个过程是原子性的，那么就会出现线程安全问题如下代码是线程不安全的，因为不能保证++value是原子性操作

```java
public class ThreadNotSafeCount {
    private Long value;

    public Long getValue() {
        return value;
    }

    public void setValue(Long value) {
        this.value = value;
    }
}
```

++value被转换为汇编后就不具有原子性了

如何才能保证多个操作的原子性呢？最简单的方法就是使用synchronized关键字进行同步，修改代码如下

```java
public class ThreadSafeCount {
    private Long value;

    public synchronized Long getValue() {
        return value;
    }

    public synchronized void setValue(Long value) {
        this.value = value;
    }
}
```

使用synchronized关键字的确可以实现线程安全性，即内存可见性和原子性，但是synchronized是独占锁，没有获取内部锁的线程会被阻塞掉，而这里的getCount方法只是读操作，多个线程同时调用不会存在线程安全问题。但是加了关键字synchronized后，同一时间就只能有一个线程可以调用，这显然大大降低了并发性。你也许会问，既然是只读操作，那为何不去掉getCount方法上的synchronized关键字呢？其实是不能去掉的，别忘了这里要靠synchronized来实现value的内存可见性。那么有没有更好的实现呢？答案是肯定的，下面将讲到的在内部使用非阻塞CAS算法实现的原子性操作类AtomicLong就是一个不错的选择

### Java 中的 CAS 操作

在Java中，锁在并发处理中占据了一席之地，但是使用锁有一个不好的地方，就是当一个线程没有获取到锁时会被阻塞挂起，这会导致线程上下文的切换和重新调度开销。Java提供了非阻塞的volatile关键字来解决共享变量的可见性问题，这在一定程度上弥补了锁带来的开销问题，但是volatile只能保证共享变量的可见性，不能解决读—改—写等的原子性问题。CAS即Compare andSwap，其是JDK提供的非阻塞原子性操作，它通过硬件保证了比较—更新操作的原子性。JDK里面的Unsafe类提供了一系列的compareAndSwap*方法，下面以compareAndSwapLong方法为例进行简单介绍

* boolean compareAndSwapLong（Object obj, long valueOffset,long expect, long update）方法：其中compareAndSwap的意思是比较并交换。CAS有四个操作数，分别为：对象内存位置、对象中的变量的偏移量、变量预期值和新的值。其操作含义是，如果对象obj中内存偏移量为valueOffset的变量值为expect，则使用新的值update替换旧的值expect。这是处理器提供的一个原子性指令。

关于CAS操作有个经典的**ABA**问题，具体如下：假如线程I使用CAS修改初始值为A的变量X，那么线程I会首先去获取当前变量X的值（为A），然后使用CAS操作尝试修改X的值为B，如果使用CAS操作成功了，那么程序运行一定是正确的吗？其实未必，这是因为有可能在线程I获取变量X的值A后，在执行CAS前，线程II使用CAS修改了变量X的值为B，然后又使用CAS修改了变量X的值为A。所以虽然线程I执行CAS时X的值是A，但是这个A已经不是线程I获取时的A了。这就是ABA问题。ABA问题的产生是因为变量的状态值产生了环形转换，就是变量的值可以从A到B，然后再从B到A。如果变量的值只能朝着一个方向转换，比如A到B, B到C，不构成环形，就不会存在问题。JDK中的AtomicStampedReference类给每个变量的状态值都配备了一个时间戳，从而避免了ABA问题的产生。

### Unsafe类

#### Unsafe类中的重要方法

JDK的rt.jar包中的Unsafe类提供了硬件级别的原子性操作，Unsafe类中的方法都是native方法，它们使用JNI的方式访问本地C++ 实现库

#### 如何使用Unsafe类

通过反射使用

### Java 指令重排序

Java内存模型允许编译器和处理器对指令重排序以提高运行性能，并且只会对不存在数据依赖性的指令重排序。在单线程下重排序可以保证最终执行的结果与程序顺序执行的结果一致，但是在多线程下就会存在问题

```java
int a = 1;(1)
int b = 2;(2)
int c = a+b;(3)
```

在如上代码中，变量c的值依赖a和b的值，所以重排序后能够保证（3）的操作在（2）（1）之后，但是（1）（2）谁先执行就不一定了，这在单线程下不会存在问题，因为并不影响最终结果

下面看一个多线程的例子

```java
public class Reorder {
    private static int num = 0;
    private static boolean ready = false;
    public static void main(String[] args) throws InterruptedException {
        Thread readThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()) {
                    if (ready) {//(1)
                        System.out.println(num + num);//(2)
                    }
                    System.out.println("read thread");
                }
            }
        });
        Thread writeThread = new Thread(new Runnable() {
            @Override
            public void run() {
                num = 2;//(3)
                ready = true;//(4)
                System.out.println("write thread is over");
            }
        });
        readThread.start();
        writeThread.start();
        Thread.sleep(10);
        readThread.interrupt();
        System.out.println("main exit");
    }
}
```

首先这段代码里面的变量没有被声明为volatile的，也没有使用任何同步措施，所以在多线程下存在共享变量内存可见性问题。这里先不谈内存可见性问题，因为通过把变量声明为volatile的本身就可以避免指令重排序问题

这里先看看指令重排序会造成什么影响，如上代码在不考虑内存可见性问题的情况下一定会输出4？答案是不一定，由于代码（1）（2）（3）（4）之间不存在依赖关系，所以写线程的代码（3）（4）可能被重排序为先执行（4）再执行（3），那么执行（4）后，读线程可能已经执行了（1）操作，并且在（3）执行前开始执行（2）操作，这时候输出结果为0而不是4

重排序在多线程下会导致非预期的程序执行结果，而使用volatile修饰ready就可以避免重排序和内存可见性问题

写volatile变量时，可以确保volatile写之前的操作不会被编译器重排序到volatile写之后。读volatile变量时，可以确保volatile读之后的操作不会被编译器重排序到volatile读之前

### 伪共存

#### 什么是伪共享

为了解决计算机系统中主内存与CPU之间运行速度差问题，会在CPU与主内存之间添加一级或者多级高速缓冲存储器（Cache）。这个Cache一般是被集成到CPU内部的，所以也叫CPU Cache，图2-6所示是两级Cache结构。

![20201209-230938-0550.png](https://gitee.com/chuchin/img/raw/master/20201209-230938-0550.png)

在Cache内部是按行存储的，其中每一行称为一个Cache行。Cache行（如图2-7所示）是Cache与主内存进行数据交换的单位，Cache行的大小一般为2的幂次数字节

![20201209-232847-0480.png](https://gitee.com/chuchin/img/raw/master/20201209-232847-0480.png)

当CPU访问某个变量时，首先会去看CPU Cache内是否有该变量，如果有则直接从中获取，否则就去主内存里面获取该变量，然后把该变量所在内存区域的一个Cache行大小的内存复制到Cache中。由于存放到Cache行的是内存块而不是单个变量，所以可能会把多个变量存放到一个Cache行中。**当多个线程同时修改一个缓存行里面的多个变量时，由于同时只能有一个线程操作缓存行，所以相比将每个变量放到一个缓存行，性能会有所下降，这就是伪共享**，

![f3a59c51fcdb815d6f7ad2b08deffdb](C:\Users\13723\AppData\Local\Temp\WeChat Files\f3a59c51fcdb815d6f7ad2b08deffdb.png)

在该图中，变量x和y同时被放到了CPU的一级和二级缓存，当线程1使用CPU1对变量x进行更新时，首先会修改CPU1的一级缓存变量x所在的缓存行，这时候在缓存一致性协议下，CPU2中变量x对应的缓存行失效。那么线程2在写入变量x时就只能去二级缓存里查找，这就破坏了一级缓存。而一级缓存比二级缓存更快，这也说明了多个线程不可能同时去修改自己所使用的CPU中相同缓存行里面的变量。更坏的情况是，如果CPU只有一级缓存，则会导致频繁地访问主内存。

#### 为何会出现伪共享

伪共享的产生是因为多个变量被放入了一个缓存行中，并且多个线程同时去写入缓存行中不同的变量。那么为何多个变量会被放入一个缓存行呢？其实是因为缓存与内存交换数据的单位就是缓存行，当CPU要访问的变量没有在缓存中找到时，根据程序运行的局部性原理，会把该变量所在内存中大小为缓存行的内存放入缓存行。

单个线程下顺序修改一个缓存行中的多个变量，会充分利用程序运行的局部性原则，从而加速了程序的运行。而在多线程下并发修改一个缓存行中的多个变量时就会竞争缓存行，从而降低程序运行性能。

#### 如何避免伪共享

在JDK 8之前一般都是通过字节填充的方式来避免该问题，也就是创建一个变量时使用填充字段填充该变量所在的缓存行，这样就避免了将多个变量存放在同一个缓存行中

假如缓存行为64字节，那么我们在FilledLong类里面填充了6个long类型的变量，每个long类型变量占用8字节，加上value变量的8字节总共56字节。另外，这里FilledLong是一个类对象，而类对象的字节码的对象头占用8字节，所以一个FilledLong对象实际会占用64字节的内存，这正好可以放入一个缓存行。

JDK 8提供了一个**@sun.misc.Contended注解**，用来解决伪共享问题。需要注意的是，在默认情况下，@Contended注解只用于Java核心类，比如rt包下的类。如果用户类路径下的类需要使用这个注解，则需要添加JVM参数：-XX:-RestrictContended。填充的宽度默认为128，要自定义宽度则可以设置-XX:ContendedPaddingWidth参数

#### 小结

本节讲述了伪共享是如何产生的，以及如何避免，并证明在多线程下访问同一个缓存行的多个变量时才会出现伪共享，在单线程下访问一个缓存行里面的多个变量反而会对程序运行起到加速作用。本节的这些知识为后面高级篇讲解的LongAdder的实现原理奠定了基础。

### 锁的概述

####  乐观锁与悲观锁

**悲观锁**指对数据被外界修改持保守态度，认为数据很容易就会被其他线程修改，所以在数据被处理前先对数据进行加锁，并在整个数据处理过程中，使数据处于锁定状态。悲观锁的实现往往依靠数据库提供的锁机制，即在数据库中，在对数据记录操作前给记录加排它锁。如果获取锁失败，则说明数据正在被其他线程修改，当前线程则等待或者抛出异常。如果获取锁成功，则对记录进行操作，然后提交事务后释放排它锁。

**乐观锁**是相对悲观锁来说的，它认为数据在一般情况下不会造成冲突，所以在访问记录前不会加排它锁，而是在进行数据提交更新时，才会正式对数据冲突与否进行检测。具体来说，根据update返回的行数让用户决定如何去做。

乐观锁并不会使用数据库提供的锁机制，一般在表中添加version字段或者使用业务状态来实现。乐观锁直到提交时才锁定，所以不会产生任何死锁

#### 公平锁与非公平锁

根据线程获取锁的抢占机制，锁可以分为公平锁和非公平锁，**公平锁表示线程获取锁的顺序是按照线程请求锁的时间早晚来决定的，也就是最早请求锁的线程将最早获取到锁**。而**非公平锁则在运行时闯入，也就是先来不一定先得**。

ReentrantLock提供了公平和非公平锁的实现。

* 公平锁：ReentrantLock pairLock = new ReentrantLock（true）。
* 非公平锁：ReentrantLock pairLock = new ReentrantLock（false）。如果构造函数不传递参数，则默认是非公平锁。

例如，假设线程A已经持有了锁，这时候线程B请求该锁其将会被挂起。当线程A释放锁后，假如当前有线程C也需要获取该锁，如果采用非公平锁方式，则根据线程调度策略，线程B和线程C两者之一可能获取锁，这时候不需要任何其他干涉，而如果使用公平锁则需要把C挂起，让B获取当前锁。在没有公平性需求的前提下尽量使用非公平锁，因为公平锁会带来性能开销

#### 独占锁与共享锁

根据锁只能被单个线程持有还是能被多个线程共同持有，锁可以分为独占锁和共享锁。

**独占锁**保证任何时候都只有一个线程能得到锁，ReentrantLock就是以独占方式实现的。共享锁则可以同时由多个线程持有，例如ReadWriteLock读写锁，它允许一个资源可以被多线程同时进行读操作。独占锁是一种悲观锁，由于每次访问资源都先加上互斥锁，这限制了并发性，因为读操作并不会影响数据的一致性，而独占锁只允许在同一时间由一个线程读取数据，其他线程必须等待当前线程释放锁才能进行读取。

**共享锁**则是一种乐观锁，它放宽了加锁的条件，允许多个线程同时进行读操作。

#### 什么是可重入锁

当一个线程要获取一个被其他线程持有的独占锁时，该线程会被阻塞，那么当一个线程再次获取它自己已经获取的锁时是否会被阻塞呢？如果不被阻塞，那么我们说该锁是可重入的，也就是只要该线程获取了该锁，那么可以无限次数（在高级篇中我们将知道，严格来说是有限次数）地进入被该锁锁住的代码。

```java
public class ReentrantLockTest {
    
    public synchronized void helloA() {
        System.out.println("a");
    }

    public synchronized void helloB() {
        System.out.println("b");
        helloA();
    }
    
}
```

在如上代码中，调用helloB方法前会先获取内置锁，然后打印输出。之后调用helloA方法，在调用前会先去获取内置锁，**如果内置锁不是可重入的，那么调用线程将会一直被阻塞。**

实际上，synchronized内部锁是可重入锁。可重入锁的原理是在锁内部维护一个线程标示，用来标示该锁目前被哪个线程占用，然后关联一个计数器。一开始计数器值为0，说明该锁没有被任何线程占用。当一个线程获取了该锁时，计数器的值会变成1，这时其他线程再来获取该锁时会发现锁的所有者不是自己而被阻塞挂起。

但是当获取了该锁的线程再次获取锁时发现锁拥有者是自己，就会把计数器值加+1，当释放锁后计数器值-1。当计数器值为0时，锁里面的线程标示被重置为null，这时候被阻塞的线程会被唤醒来竞争获取该锁。

#### 自旋锁

由于Java中的线程是与操作系统中的线程一一对应的，所以当一个线程在获取锁（比如独占锁）失败后，会被切换到内核状态而被挂起。当该线程获取到锁时又需要将其切换到内核状态而唤醒该线程。而从用户状态切换到内核状态的开销是比较大的，在一定程度上会影响并发性能。自旋锁则是，当前线程在获取锁时，**如果发现锁已经被其他线程占有，它不马上阻塞自己，在不放弃CPU使用权的情况下，多次尝试获取（默认次数是10，可以使用-XX:PreBlockSpinsh参数设置该值），很有可能在后面几次尝试中其他线程已经释放了锁。**如果尝试指定的次数后仍没有获取到锁则当前线程才会被阻塞挂起。由此看来自旋锁是**使用CPU时间换取线程阻塞与调度的开销，但是很有可能这些CPU时间白白浪费了**。

### 总结

本章主要介绍了并发编程的基础知识，为后面在高级篇讲解并发包源码打下了基础，并结合图示形象地讲述了为什么要使用多线程编程，多线程编程存在的线程安全问题，以及什么是内存可见性问题。然后讲解了synchronized和volatile关键字，并且强调前者既保证内存的可见性又保证原子性，而后者则主要保证内存可见性，但是二者的内存语义很相似。最后讲解了什么是CAS和线程间同步以及各种锁的概念，这些都为后面讲解JUC包源码奠定了基础。
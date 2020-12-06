---
title: "Java并发编程之美"
date: 2020-11-29T16:07:26+08:00
draft: false
tags: ["Java","并发编程"]
---

![thread](/img/thread.jpg)

该书在微信读书中上架，可以免费阅读，代码：[https://github.com/chuchinc/kl-book-resource](https://github.com/chuchinc/kl-book-resource)

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

#### ThreadLocal使用示例

#### ThreadLocal不支持继承性

#### InheritableThreadLocal类






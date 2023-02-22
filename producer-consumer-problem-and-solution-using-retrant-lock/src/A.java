import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Producer -> Produces an item in the buffer
 * Consumer -> Consumes an item from the buffer
 * If the shared buffer is not synchronized properly, there is chance that
 * ArrayIndexOutOfBoundsException may occur.
 * Hence, appropriate synchronized blocks for shared buffer must be used.
 * This solves the problem of exception, but sometimes a thread may go in
 * long waiting for acquiring lock on the buffer, such threads are starved(waiting for the lock)
 * Solution for the starvation caused by synchronized is Reentrant lock, and it should be used.
 */
class Producer implements Runnable {
    private final List<String> buffer;
    private final ReentrantLock lock;

    public Producer(List<String> buffer, ReentrantLock lock) {
        this.buffer = buffer;
        this.lock = lock;
    }


    @Override
    public void run() {
        String[] numbers = {"1", "2", "3"};
        for (String number : numbers) {
            main.waitForMillis(main.MILLIS);
            System.out.println(Thread.currentThread().getName() + " added " + number);
            addItemToTheBuffer(number);
        }
        System.out.println(Thread.currentThread().getName() + " added " + main.EOB);
        addItemToTheBuffer(main.EOB);
    }

    private void addItemToTheBuffer(String item) {
        try {
            this.lock.lock();
            main.waitForMillis(main.MILLIS);
            buffer.add(item);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.lock.unlock();
        }
    }
}

class Consumer implements Runnable {
    private final List<String> buffer;

    private final ReentrantLock lock;

    public Consumer(List<String> buffer, ReentrantLock lock) {
        this.buffer = buffer;
        this.lock = lock;
    }

    @Override
    public void run() {
        while (true) {
            try {
                this.lock.lock();
                if (buffer.isEmpty()) {
                    main.waitForMillis(main.MILLIS);
                    System.out.println(Thread.currentThread().getName() + " buffer is empty "
                            + "holdCount : "+ this.lock.getHoldCount());
                    continue;
                }
                if (buffer.get(0).equals(main.EOB)) {
                    main.waitForMillis(main.MILLIS);
                    System.out.println(Thread.currentThread().getName() + " exiting");
                    break;
                } else {
                    main.waitForMillis(main.MILLIS);
                    System.out.println(Thread.currentThread().getName() + " removed " + buffer.remove(0));
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                this.lock.unlock();
            }
        }
    }
}

 class main {
    public static final String EOB = "EOB";
    public static final long MILLIS = 100;

    public static void waitForMillis(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        List<String> buffer = new ArrayList<>();

        ReentrantLock lock = new ReentrantLock(true);

        Thread producerThread = new Thread(new Producer(buffer, lock));
        producerThread.setName("Producer-Thread");

        Thread consumerThread1 = new Thread(new Consumer(buffer, lock));
        consumerThread1.setName("Consumer-Thread1");

        Thread consumerThread2 = new Thread(new Consumer(buffer, lock));
        consumerThread2.setName("Consumer-Thread2");

        producerThread.start();
        consumerThread1.start();
        consumerThread2.start();
    }
}
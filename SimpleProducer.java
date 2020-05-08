import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Simulates a singleton resource provider that takes forever to spin up. In
 * practice, this is modeling the construction of a large in-memory cache.
 *
 * This implementation uses high-level synchronization objects rather than Futures.
 */
class SimpleProducer implements Runnable {
    // fields
    private volatile boolean initialized;
    private String data;
    private Lock lock;
    private Condition monitor;

    // singleton stuff
    private static SimpleProducer instance = new SimpleProducer();

    private SimpleProducer() {
        this.initialized = false;
        this.data = "foo,bar,baz,qux";
        this.lock = new ReentrantLock();
        this.monitor = lock.newCondition();
    }

    public static SimpleProducer getInstance() {
        return instance;
    }
    // end singleton stuff

    @Override
    public void run() {
        // simulate cache construction
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            System.out.println("SimpleProducer interrupted before startup, exiting");
            System.exit(0);
        }

        // finally open for business
        try {
            getInstance().lock.lock();
            getInstance().initialized = true;
            getInstance().monitor.signalAll();
        } finally {
            getInstance().lock.unlock();
        }
    }

    public static String getData() {
        getInstance().lock.lock();
        try {
            while (!getInstance().initialized) {
                // FIXME how to handle a timeout?
                getInstance().monitor.await(30, TimeUnit.SECONDS);
            }
        } catch (InterruptedException e) {
            System.out.println("Consumer interrupted before data was ready");
            return null;
        } finally {
            getInstance().lock.unlock();
        }
        return getInstance().data;
    }
}


import java.util.concurrent.Executors;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Simulates a singleton resource provider that takes forever to spin up. In
 * practice, this is modeling the construction of a large in-memory cache.
 *
 * This implementation uses a Future rather than high-level synchronization objects.
 */
class FutureProducer implements Runnable {
    private String data;
    private final AtomicReference<Future<String>> startupFuture = new AtomicReference<>();

    // singleton stuff
    private static FutureProducer instance = new FutureProducer();

    private FutureProducer() {
        this.data = "foo,bar,baz,qux";
    }

    public static FutureProducer getInstance() {
        return instance;
    }
    // end singleton stuff

    @Override
    public void run() {
        ExecutorService startupExecutor = Executors.newSingleThreadExecutor();
        Future<String> startupFuture = startupExecutor.submit(() -> {
            // simulate cache construction
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                System.out.println("FutureProducer interrupted before startup, exiting");
                System.exit(0);
            }

            return data;
        });

        // assign this pretty quickly so it is available for getData calls
        // hopefully they haven't started coming in this soon...
        this.startupFuture.set(startupFuture);

        // allow graceful shutdown (vs. shutdownNow)
        startupExecutor.shutdown();
    }

    public static String getData() {
        Future<String> f = getInstance().startupFuture.get();
        if (f == null) {
            // FIXME haven't even started yet... need to busy-wait, perhaps
        }

        try {
            String data = f.get(30, TimeUnit.SECONDS);
            return data;
        } catch (InterruptedException | ExecutionException e) {
            // FIXME failed to startup or was interrupted during startup; maybe throw to the caller?
            throw new IllegalStateException(e);
        } catch (TimeoutException e) {
            // FIXME got tired of waiting, maybe do something special here?
            throw new IllegalStateException(e);
        }
    }
}


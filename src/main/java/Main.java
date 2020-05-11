import java.time.LocalTime;
import java.time.ZoneId;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

public class Main {
    /*
     * There are two versions of the producer singleton: SimpleProducer and FutureProducer.
     * SimpleProducer uses high-level synchronization constructs like Lock and Condition.
     * FutureProducer uses wrappers like AtomicReference and Future.
     * In practice they should function (mostly) the same, with a few caveats:
     * - SimpleProducer doesn't handle spurious wakeups or timeouts
     * - FutureProducer doesn't handle Consumers requesting data before the Future is instantiated
     */
    private static final boolean USE_FUTURES = false;
    public static void main(String[] args) {
        System.out.println("Application started at " + LocalTime.now(ZoneId.of("America/Chicago")).toString());

        System.out.println("Starting 25 Consumers");
        ExecutorService consumerService = Executors.newFixedThreadPool(25);
        for (int i = 0; i < 25; i++) {
            consumerService.execute(new Consumer(USE_FUTURES));
        }

        System.out.println("Starting SimpleProducer");
        ExecutorService producerService = Executors.newSingleThreadExecutor();
        producerService.execute(SimpleProducer.getInstance());
    }
}


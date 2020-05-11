import java.time.LocalTime;
import java.time.ZoneId;

/**
 * Simulates real-world (e.g., indeterminate timing) requests for resources. In
 * practice, this is modeling users making requests to a web server.
 */
public class Consumer implements Runnable {
    private static final ZoneId ZONE = ZoneId.of("America/Chicago");

    private LocalTime initTime;
    private LocalTime callTime;
    private long waitTime;
    private boolean isFuture;

    public Consumer(boolean isFuture) {
        this.initTime = LocalTime.now(ZONE);
        this.waitTime = (long) (Math.random() * 9000) + 1000;
        this.isFuture = isFuture;
    }

    @Override
    public void run() {
        try {
            // simulate requests trickling in
            Thread.sleep(waitTime);

            // OK, the request actually came in
            this.callTime = LocalTime.now(ZONE);

            // producer may take a while to get data to us...
            String data;
            // HACK this is just reduce code duplication, because the producers
            // are singletons but the consumer behaves the same either way
            if(isFuture) {
                data = FutureProducer.getData();
            } else {
                data = SimpleProducer.getData();
            }

            // just pick a random data element to display
            String element = data.split(",")[(int) (Math.random() * 4)];
            System.out.printf("%s %s: %s\n", initTime.toString(), callTime.toString(), element);
        } catch (InterruptedException e) {
            System.out.println("Thread somehow already interrupted, wow");
        }
    }
}


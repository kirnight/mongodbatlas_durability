package gra_test;

import java.time.LocalDateTime;
import java.util.concurrent.*;

public class FailureRunner {
    public Failure failure;
    public FailureLogger logger;

    public FailureRunner(Failure failure, FailureLogger logger)
    {
        this.failure = failure;
        this.logger = logger;
    }

    public LocalDateTime[] Run(int experimentTime){
        LocalDateTime[] timeList = new LocalDateTime[2];
        int time = experimentTime / 3;  // Start stage, Failure stage and Recovery stage. 3 stages have same duration

        try {
            Thread.sleep(time * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Failure Induced");
        this.logger.LogFailureCompleteAsync();
        try {
            CompletableFuture.runAsync(()->this.failure.InduceAsync()).get(1, TimeUnit.SECONDS); // Prevent Poweroff blocking
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            Thread.sleep(time * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        this.failure.FixAsync();
        this.logger.LogFixCompleteAsync();
        System.out.println("Failure Fixed");

//        // JUST FOR ASYNC
//        try {
//            Thread.sleep(time * 1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//        System.out.println("Failure Induced 2");
////        this.logger.LogFailureCompleteAsync();
//        try {
//            CompletableFuture.runAsync(()->this.failure.InduceAsync()).get(1, TimeUnit.SECONDS); // Prevent Poweroff blocking
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        try {
//            Thread.sleep(time * 1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//        this.failure.FixAsync();
////        this.logger.LogFixCompleteAsync();
//        System.out.println("Failure Fixed 2");

        timeList[0] = logger.fail;
        timeList[1] = logger.fix;

        return timeList;
    }
}

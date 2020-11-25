package gra_test;

import java.time.LocalDateTime;

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

        this.failure.InduceAsync();
        System.out.println("Failure Induced");
        this.logger.LogFailureCompleteAsync();

        try {
            Thread.sleep(time * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        this.failure.FixAsync();
        System.out.println("Failure Fixed");
        this.logger.LogFixCompleteAsync();

        timeList[0] = logger.fail;
        timeList[1] = logger.fix;

        return timeList;
    }
}

package gra_test;

import java.time.LocalDateTime;

public class failureRunnable implements Runnable{
    public FailureRunner fail;
    public int experimentTime;
    public LocalDateTime[] timeList;
    public failureRunnable(FailureRunner fail, int experimentTime){
        this.fail = fail;
        this.experimentTime = experimentTime;
        this.timeList = new LocalDateTime[2];
    }


    @Override
    public void run() {
        timeList = fail.Run(experimentTime);
        fail.logger.fail = timeList[0];
        fail.logger.fix = timeList[1];
    }
}

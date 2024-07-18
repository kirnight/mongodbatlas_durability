package gra_test;

import java.time.LocalDateTime;

public class FailureLogger {
    public LocalDateTime fail;
    public LocalDateTime fix;
    //    public FailureLogger(LocalDateTime fail, LocalDateTime fix){
//        this.fail = fail;
//        this.fix = fix;
//    }
    public FailureLogger() {
    }

    public void LogFailureCompleteAsync()
    {
        this.fail = LocalDateTime.now();
    }

    public void LogFixCompleteAsync()
    {
        this.fix = LocalDateTime.now();
    }
}

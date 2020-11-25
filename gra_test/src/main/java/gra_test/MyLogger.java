package gra_test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class MyLogger {
    public ArrayList<LogRecord> logs;

    public MyLogger()
    {
        this.logs = new ArrayList<LogRecord>();
    }

    public void LogReadAsync(String id, int val, Duration time)
    {
        logs.add(new LogRecord("R", id, val, LocalDateTime.now(), time,false));
    }

    public void LogUpdateAsync(String id, int val, Duration time)
    {
        logs.add(new LogRecord("U", id, val, LocalDateTime.now(), time, false));
    }

    public void LogWriteAsync(String id, int val, Duration time)
    {
        logs.add(new LogRecord("W", id, val, LocalDateTime.now(), time, false));
    }

    public void LogErrAsync(String op, String id, int val, Duration time)
    {
        logs.add(new LogRecord(op, id, val, LocalDateTime.now(), time, true));
    }

    public ArrayList<LogRecord> GetLogs()
    {
        return this.logs;
    }
}

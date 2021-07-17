package gra_test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MyLogger {
    public List<LogRecord> logs;

    public MyLogger()
    {
        this.logs = Collections.synchronizedList(new ArrayList<LogRecord>());
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

    public List<LogRecord> GetLogs()
    {
        return this.logs;
    }
}

package gra_test;

import java.time.Duration;
import java.time.LocalDateTime;

public class LogRecord {
    public String operation;

    public String id;

    public LocalDateTime timeStamp;

    public int val;

    public Duration duration;

    public boolean isError;

    public LogRecord(String op, String id, int val, LocalDateTime timeStamp, Duration duration, boolean isError)
    {
        this.operation = op;
        this.id = id;
        this.timeStamp = timeStamp;
        this.val = val;
        this.duration = duration;
        this.isError = isError;
    }

    public String toString(){
//        if(this.isError){
//            return "ERR,"+operation+","+id+","+val+","+duration.toString()+","+timeStamp.toString();
//        }else{
//            return operation+","+id+","+val+","+duration.toString()+","+timeStamp.toString();
//        }

        return operation+" "+id+" "+timeStamp.toString()+" "+val+" "+duration.toString()+" "+isError;
    }
}

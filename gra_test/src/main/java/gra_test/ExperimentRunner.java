package gra_test;

import org.bson.types.ObjectId;
import org.bson.Document;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Random;

public class ExperimentRunner {

    public MyLogger myLogger;
    public MongoCommand client;

    public ArrayList<ObjectId> ids;
    public Random rng;

    public ExperimentRunner(MyLogger myLogger, MongoCommand client)
    {
        this.myLogger = myLogger;
        this.client = client;
        this.rng = new Random();
        this.ids = new ArrayList<ObjectId>();
    }

    public ArrayList<LogRecord> Run(int opInterval, int variance, double writeLoad, int experimentTime)
    {
        Setup();

        LocalDateTime endTime = LocalDateTime.now().plusSeconds(experimentTime);

        while (LocalDateTime.now().isBefore(endTime))
        {
            RunOpAsync(writeLoad);
        }

        return myLogger.GetLogs();

    }

    private void Setup()
    {
        for (int i = 0; i < 5; i++)
        {
            WriteAsync();
        }

    }

    private void RunOpAsync(double writeLoad)
    {

        if (rng.nextDouble() < writeLoad)
        {
            if (rng.nextDouble() > 0.5)
            {
                WriteAsync();
            }
            else
            {
                UpdateAsync();
            }
        }
        else
        {
            ReadAsync();
        }
    }

    private void ReadAsync()
    {
        ObjectId id = GetRandomObjectId();

        Document resultValue;

        LocalDateTime before = LocalDateTime.now();

        try
        {
            resultValue = client.GetAsync(id);
        }
        catch(Exception e)
        {
            myLogger.LogErrAsync("R", id.toString(), -1, Duration.between(before,LocalDateTime.now()));
            return;
        }
        LocalDateTime after = LocalDateTime.now();

        myLogger.LogReadAsync(id.toString(), (int)resultValue.get("val"), Duration.between(before,after));

    }

    private void WriteAsync()
    {
        int val = rng.nextInt();

        Document doc = new Document().append("val",val);

        LocalDateTime before = LocalDateTime.now();

        try
        {
            client.WriteAsync(doc);
        }
        catch(Exception e)
        {
            myLogger.LogErrAsync("W", doc.getObjectId("_id").toString(), val, Duration.between(before,LocalDateTime.now()));
            return;
        }

        LocalDateTime after = LocalDateTime.now();
        ObjectId id = doc.getObjectId("_id");
        ids.add(id);

        myLogger.LogWriteAsync(id.toString(), val, Duration.between(before,after));
    }

    private void UpdateAsync()
    {
        ObjectId id = GetRandomObjectId();

        int val = rng.nextInt();

        Document doc;

        LocalDateTime before = LocalDateTime.now();

        try
        {
            doc = client.UpdateAsync(id, val);
        }
        catch(Exception e)
        {
            myLogger.LogErrAsync("U", id.toString(), val, Duration.between(before,LocalDateTime.now()));
            return;
        }

        if (doc == null)
        {
            myLogger.LogErrAsync("U", id.toString(), val, Duration.between(before,LocalDateTime.now()));
            return;
        }

        LocalDateTime after = LocalDateTime.now();

        myLogger.LogUpdateAsync(id.toString(), val, Duration.between(before,after));
    }

    private ObjectId GetRandomObjectId()
    {
        int index = rng.nextInt(ids.size());

        return ids.get(index);

    }
}

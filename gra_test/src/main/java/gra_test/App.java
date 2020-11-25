package gra_test;

import com.mongodb.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class App {
    public static void main(String[] args){


        JSONParser parser = new JSONParser();
        ConfigManager settings = null;
        try {
            // A JSON object. Key value pairs are unordered.
            JSONObject jsonObject = (JSONObject) parser.parse(new FileReader("src/main/java/gra_test/config.json"));
            settings = new ConfigManager(jsonObject);


        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }

        //http://mongodb.github.io/mongo-java-driver/3.0/driver/reference/management/logging/
        Logger mongoLogger = Logger.getLogger("org.mongodb.driver");
        mongoLogger.setLevel(Level.SEVERE); // System log for mongo(will not use that for experiment)


        // 3 private internal DNS of AWS EC2 VM instances(for convenience)
        String privatePrimary = "<DNS 1>";
        String privateSecondary1 = "<DNS 2>";
        String privateSecondary2 = "<DNS 3>";
        // private internal DNS and instance id mapping
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(privatePrimary, "<instance 1 id>"); // (node0_4.4)
        map.put(privateSecondary1, "<instance 2 id>"); // (node1_4.4)
        map.put(privateSecondary2, "<instance 3 id>"); // (node2_4.4)

        // 1 mongo client instance setup
        String connectionurl = "mongodb://"+privatePrimary+","+privateSecondary1+","+privateSecondary2+"/?replicaSet=rs0&retryWrites=false";
        int numThreads = settings.getConnectionPoolSize();
        MongoClient mongoClient = MongoClients.create(
                MongoClientSettings.builder()
                        .applyConnectionString(new ConnectionString(connectionurl))
                        .readConcern(settings.getReadconcern())
                        .readPreference(settings.getReadpreference())
                        .writeConcern(settings.getWriteconcern())
                        .applyToConnectionPoolSettings(builder -> builder.maxSize(numThreads))
                        .applyToConnectionPoolSettings(builder -> builder.minSize(numThreads))
                        .build());


        MongoDatabase database = mongoClient.getDatabase("test");
        MongoCollection<Document> oldCollection = database.getCollection("testCollection");
        oldCollection.drop();
        MongoCollection<Document> collection = database.getCollection("testCollection");


        // warmup
        Warmup(mongoClient);
        System.out.println("Warmup complete. Setting up...");

        int operationInterval =10;
        int IntervalVariance=10;

        double WriteProbability = settings.getWriteProbability();
        int ExperimentTime = settings.getExperimentTime();

        // Multithreading, serving as different clients
        Thread[] runners = new Thread[numThreads+1];

        // log record for experiment
        List<ArrayList<LogRecord>> logs = new ArrayList<ArrayList<LogRecord>>(); //concurrentbag may be considered

        // Define threads
        for(int i = 0; i < numThreads; i++){
            ExperimentRunner runner = new ExperimentRunner(new MyLogger(), new MongoCommand(mongoClient,collection));
            runners[i] = new Thread(new clientRunnable(runner, logs, operationInterval, IntervalVariance, WriteProbability, ExperimentTime)); // operationInterval, IntervalVariance are not used
        }

        //Failure thread setup
        Failure failInduce = settings.getFailure(database, map);

        FailureRunner fail = new FailureRunner(failInduce,new FailureLogger(LocalDateTime.now(),LocalDateTime.now()));
        runners[numThreads] = new Thread(new failureRunnable(fail, ExperimentTime));

        // Start threading
        System.out.println("Thread setup completed. Start experiment.");
        for(int i = 0; i <= numThreads; i++){
            runners[i].start();
        }

        // End threading
        for(int i = 0; i <= numThreads; i++){
            try {
                runners[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Thread finished. Log writing...");


        // Make a huge log containing all records
        List<LogRecord> completeLog = new ArrayList<LogRecord>();
        for(int j = 0; j < logs.size();j++){
            completeLog.addAll(logs.get(j));
        }

        // ordering
        System.out.println("Ordering and writing records:"+completeLog.size());
        System.out.println(fail.logger.fail+"  "+ fail.logger.fix);
        completeLog.sort(Comparator.comparing(o -> o.timeStamp));
//        for(int k = 0; k < completeLog.size();k++){
//            System.out.println(completeLog.get(k).toString());
//        }


        try {
            FileWriter writer = new FileWriter("result.txt", false);
            BufferedWriter bufferedWriter = new BufferedWriter(writer);
            bufferedWriter.write(fail.logger.fail+"  "+ fail.logger.fix+"\n");
            for(int k = 0; k < completeLog.size();k++){
                bufferedWriter.write(completeLog.get(k).toString()+"\n");
            }
            bufferedWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }



        System.out.println("End of experiment");
        return;
    }

    public static void Warmup(MongoClient client)
    {
        while (true)
        {
            Document status = client.getDatabase("admin").runCommand(new Document("replSetGetStatus", 1));
            if ((double) status.get("ok") == 1)
            {
                break;
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

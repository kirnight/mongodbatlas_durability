package gra_test;

import com.mongodb.*;
//import com.mongodb.reactivestreams.client.MongoClient;
//import com.mongodb.reactivestreams.client.MongoClients;
//import com.mongodb.reactivestreams.client.MongoCollection;
//import com.mongodb.reactivestreams.client.MongoDatabase;
import com.mongodb.client.*;

import com.mongodb.internal.client.model.FindOptions;
import org.bson.Document;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.reactivestreams.Publisher;

import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
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


        // 3 private internal DNS of AWS EC2 VM instances
        List<String> servers = settings.getServerList();
        String server0 = servers.get(0);
        String server1 = servers.get(1);
        String server2 = servers.get(2);

        // private internal DNS and instance id mapping
        List<String> instances = settings.getInstanceId();
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(server0, instances.get(0)); // (node0)
        map.put(server1, instances.get(1)); // (node1)
        map.put(server2, instances.get(2)); // (node2)

        // 1 mongo client instance setup
        String connectionurl = "mongodb://"+server0+","+server1+","+server2+"/?replicaSet=rs0&connectTimeoutMS=2000&socketTimeoutMS=2000";
        int numThreads = settings.getConnectionPoolSize();
        MongoClientSettings clientSettings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(connectionurl))
                .readConcern(settings.getReadconcern())
                .readPreference(settings.getReadpreference())
                .writeConcern(settings.getWriteconcern())
                .applyToConnectionPoolSettings(builder -> builder.maxSize(numThreads))
                .applyToConnectionPoolSettings(builder -> builder.minSize(numThreads))
                .applyToClusterSettings(builder -> builder.serverSelectionTimeout(1000, TimeUnit.MILLISECONDS))
                .retryReads(false)
                .retryWrites(false)
                .build();

        System.out.println(clientSettings.getServerSettings());
        System.out.println(clientSettings.getConnectionPoolSettings());
        System.out.println(clientSettings.getHeartbeatSocketSettings());
        System.out.println(clientSettings.getRetryReads());
        System.out.println(clientSettings.getSocketSettings());


        MongoClient mongoClient = MongoClients.create(clientSettings);


        MongoDatabase database = mongoClient.getDatabase("test");
        MongoCollection<Document> oldCollection = database.getCollection("testCollection");
        oldCollection.drop();
        MongoCollection<Document> collection = database.getCollection("testCollection");


        // warmup
        Warmup(mongoClient);
        System.out.println("Warmup complete. Setting up...\n");

        // Replica set status check
        System.out.println("Database servers info: "+mongoClient.getClusterDescription().getShortDescription());
        System.out.println("Write concern: "+collection.getWriteConcern());
        System.out.println("Read concern: "+collection.getReadConcern().getLevel());
        System.out.println("Read preference: "+collection.getReadPreference());
        System.out.println();

        double WriteProbability = settings.getWriteProbability();
        int ExperimentTime = settings.getExperimentTime();
        int operationInterval = settings.getOperationInterval(); // Not used
        int intervalVariance = settings.getIntervalVariance(); // Not used

        // Multithreading, serving as different clients
        Thread[] runners = new Thread[numThreads+1];

        // log record for experiment
        List<List<LogRecord>> logs = new ArrayList<>(); //concurrentbag may be considered

        // Define threads
        for(int i = 0; i < numThreads; i++){
            ExperimentRunner runner = new ExperimentRunner(new MyLogger(), new MongoCommand(mongoClient,collection));
            runners[i] = new Thread(new clientRunnable(runner, logs, operationInterval, intervalVariance, WriteProbability, ExperimentTime)); // operationInterval, IntervalVariance are not used
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


        mongoClient.close();
        System.out.println("End of experiment");
        return;
    }

    public static void Warmup(MongoClient client)
    {
        while (true)
        {
//            // Create a publisher(JUST FOR ASYNC)
//            Publisher<Document> publisher = client.getDatabase("admin").runCommand(new Document("replSetGetStatus", 1));
//
//            SubscriberHelpers.ObservableSubscriber<Document> subscriber = new SubscriberHelpers.ObservableSubscriber<>();
//            publisher.subscribe(subscriber);
//            try {
//                subscriber.await();
//            } catch (Throwable throwable) {
//                throwable.printStackTrace();
//            }
//            List<Document> status = subscriber.getReceived(); // Block for the publisher to complete
//
//            if ((double) status.get(0).get("ok") == 1)
//            {
//                break;
//            }
//
//            try {
//                Thread.sleep(100);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }

            // JUST FOR SYNC
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

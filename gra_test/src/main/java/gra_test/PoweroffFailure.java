package gra_test;


//import com.mongodb.reactivestreams.client.MongoDatabase;
import org.reactivestreams.Publisher;
import com.mongodb.client.MongoDatabase;


import org.bson.Document;


import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.StartInstancesRequest;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class PoweroffFailure implements Failure {

    public MongoDatabase database;
    public HashMap<String, String> map;
    public String shutdownvm;
    private static final AWSCredentials credentials;
    private static final String myKey = "<path to private key>"; // ssh private key path
    public AmazonEC2 ec2Client;

    static {
        // put accesskey and secretkey here
        credentials = new BasicAWSCredentials(
                "<accessKey>",
                "<secretKey>"
        );
    }



    public PoweroffFailure(MongoDatabase database, HashMap map){
        this.database = database;
        this.map = map;
        this.shutdownvm = null;
        this.ec2Client = AmazonEC2ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(Regions.AP_SOUTHEAST_2)
                .build();
    }

    @Override
    public void InduceAsync() {

//        // Create a publisher(JUST FOR ASYNC)
//        Publisher<Document> publisher = database.runCommand(new Document("isMaster", 1));
//
//        SubscriberHelpers.ObservableSubscriber<Document> subscriber = new SubscriberHelpers.ObservableSubscriber<>();
//        publisher.subscribe(subscriber);
//        try {
//            subscriber.await();
//        } catch (Throwable throwable) {
//            throwable.printStackTrace();
//        }
//        List<Document> master = subscriber.getReceived(); // Block for the publisher to complete
//
//        String isMaster = (String) master.get(0).get("primary");

        String isMaster = (String) database.runCommand(new Document("isMaster", 1)).get("primary");  // Sync driver
        shutdownvm = isMaster.split(":")[0];

        System.out.println("Poweroff Server:"+this.shutdownvm+" instance ID:"+map.get(shutdownvm));

        // Poweroff signal is sent through terminal command

        Runtime runtime = Runtime.getRuntime();
        String command = "ssh -i "+myKey+" "+shutdownvm+" -o StrictHostKeyChecking=no\n sudo poweroff -f";
        try {
            Process process = runtime.exec(command);
            if (!process.waitFor(1, TimeUnit.MINUTES)) {
                System.out.println("Command timeout, attempting to destroy the process.");
                process.destroy();
            }

            try (BufferedReader err = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String in;
                while ((in = err.readLine()) != null) {
                    System.out.println(in); // 打印错误流中的信息
                }
            } catch (IOException e) {
                System.out.println("Error while reading from the process's error stream.");
                e.printStackTrace();
            }

            System.out.println("Command executed");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void FixAsync() {
        // Start an Amazon Instance
        StartInstancesRequest startInstancesRequest = new StartInstancesRequest()
                .withInstanceIds(map.get(shutdownvm));
        System.out.println("Restart Server:"+this.shutdownvm+" instance ID:"+map.get(shutdownvm));
        ec2Client.startInstances(startInstancesRequest);

    }
}

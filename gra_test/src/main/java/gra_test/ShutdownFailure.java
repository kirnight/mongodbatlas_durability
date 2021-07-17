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
import com.amazonaws.services.ec2.model.StopInstancesRequest;

import java.util.HashMap;
import java.util.List;

public class ShutdownFailure implements Failure {

    public MongoDatabase database;
    public HashMap<String, String> map;
    public String shutdownvm;
    private static final AWSCredentials credentials;
    public AmazonEC2 ec2Client;

    static {
        // put accesskey and secretkey here
        credentials = new BasicAWSCredentials(
                "<accessKey>",
                "<secretKey>"
        );
    }

    public ShutdownFailure(MongoDatabase database, HashMap map){
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

        StopInstancesRequest stopInstancesRequest = new StopInstancesRequest()
                .withInstanceIds(map.get(shutdownvm));

        // Stop an Instance
        stopInstancesRequest.setForce(false);

        System.out.println("Shutdown Server:"+this.shutdownvm+" instance ID:"+map.get(shutdownvm));
        ec2Client.stopInstances(stopInstancesRequest)
                .getStoppingInstances()
                .get(0)
                .getPreviousState()
                .getName();

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

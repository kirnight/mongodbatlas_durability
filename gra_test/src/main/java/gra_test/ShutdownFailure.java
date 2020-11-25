package gra_test;


import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.StopInstancesRequest;
import com.amazonaws.services.ec2.model.StartInstancesRequest;

import java.util.HashMap;

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
        String isMaster = (String) database.runCommand(new Document("isMaster", 1)).get("primary");
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

package gra_test;

import com.mongodb.ReadConcern;
import com.mongodb.ReadPreference;
import com.mongodb.WriteConcern;
//import com.mongodb.reactivestreams.client.MongoDatabase;
import com.mongodb.client.MongoDatabase;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class ConfigManager {
    public JSONObject jsonObject;
    public ConfigManager(JSONObject jsonObject){
        this.jsonObject = jsonObject;
    }

    public Failure getFailure(MongoDatabase database, HashMap map){
        String failureLine = (String) this.jsonObject.get("failure");
        switch(failureLine) {
            case "nofailure":
                System.out.println("No Failure Experiment");
                return new NoFailure();
            case "shutdown":
                System.out.println("Gracefully Shutdown Failure Experiment");
                return new ShutdownFailure(database, map);
            case "poweroff":
                System.out.println("Hard Poweroff Failure Experiment");
                return new PoweroffFailure(database, map);
            default:
                System.out.println("Error: getFailure! Exiting...");
                System.exit(0);
        }
        return null;
    }

    public ReadConcern getReadconcern(){
        String readConcernLine = (String) this.jsonObject.get("readConcern");
        switch(readConcernLine) {
            case "linearizable":
                return ReadConcern.LINEARIZABLE;
            case "local":
                return ReadConcern.LOCAL;
            case "majority":
                return ReadConcern.MAJORITY;
            default:
                System.out.println("Error: getReadconcern! Exiting...");
                System.exit(0);
        }
        return null;
    }

    public WriteConcern getWriteconcern(){
        String writeConcernLine = (String) this.jsonObject.get("writeConcern");
        switch(writeConcernLine) {
            case "journaled":
                return WriteConcern.JOURNALED.withJournal(true).withW(1).withWTimeout(1000, TimeUnit.MILLISECONDS);
            case "primary":
                return WriteConcern.W1.withJournal(false).withWTimeout(1000, TimeUnit.MILLISECONDS);
            case "majority":
                return WriteConcern.MAJORITY.withJournal(true).withWTimeout(1000, TimeUnit.MILLISECONDS); //
            default:
                System.out.println("Error: getWriteconcern! Exiting...");
                System.exit(0);
        }
        return null;
    }

    public ReadPreference getReadpreference(){
        String readPreferenceLine = (String) this.jsonObject.get("readPreference");
        switch(readPreferenceLine) {
            case "primary":
                return ReadPreference.primary();
            case "primaryPreferred":
                return ReadPreference.primaryPreferred();
            case "secondary":
                return ReadPreference.secondary();
            case "secondaryPreferred":
                return ReadPreference.secondaryPreferred();
            default:
                System.out.println("Error: getReadpreference! Exiting...");
                System.exit(0);
        }
        return null;
    }

    public List<String> getServerList(){
        List<String> servers = new ArrayList<>();
        JSONArray jsonArray = (JSONArray) this.jsonObject.get("replicas");
        for(int i = 0; i < jsonArray.size(); i++){
            servers.add((String) jsonArray.get(i));
        }
        return servers;
    }

    public List<String> getInstanceId(){
        List<String> instances = new ArrayList<>();
        JSONArray jsonArray = (JSONArray) this.jsonObject.get("awsinstanceid");
        for(int i = 0; i < jsonArray.size(); i++){
            instances.add((String) jsonArray.get(i));
        }
        return instances;
    }

    public int getConnectionPoolSize(){
        String numThreadLine = (String) this.jsonObject.get("numThread");
        return Integer.parseInt(numThreadLine);
    }

    public double getWriteProbability(){
        String writeProbabilityLine = (String) this.jsonObject.get("writeProbability");
        return Double.parseDouble(writeProbabilityLine);
    }

    public int getOperationInterval(){  // (Milliseconds)
        String operationIntervalLine = (String) this.jsonObject.get("operationInterval");
        return Integer.parseInt(operationIntervalLine);
    }

    public int getIntervalVariance(){  // (Milliseconds)
        String intervalVarianceLine = (String) this.jsonObject.get("intervalVariance");
        return Integer.parseInt(intervalVarianceLine);
    }

    public int getExperimentTime(){  // (Seconds)
        String experimentTimeLine = (String) this.jsonObject.get("experiment");
        return Integer.parseInt(experimentTimeLine);
    }

}

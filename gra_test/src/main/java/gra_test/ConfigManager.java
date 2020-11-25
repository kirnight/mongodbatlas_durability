package gra_test;

import com.mongodb.ReadConcern;
import com.mongodb.ReadPreference;
import com.mongodb.WriteConcern;
import com.mongodb.client.MongoDatabase;
import org.json.simple.JSONObject;

import java.util.HashMap;

public class ConfigManager {
    public JSONObject jsonObject;
    public ConfigManager(JSONObject jsonObject){
        this.jsonObject = jsonObject;
    }

    public Failure getFailure(MongoDatabase database, HashMap map){
        String failureLine = (String) this.jsonObject.get("failure");
        switch(failureLine) {
            case "nofailure":
                return new NoFailure();
            case "shutdown":
                return new ShutdownFailure(database, map);
            case "poweroff":
                System.out.println("poweroff here");
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
                return WriteConcern.JOURNALED;
            case "primary":
                return WriteConcern.W1;
            case "majority":
                return WriteConcern.MAJORITY;
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

    public int getConnectionPoolSize(){
        String numThreadLine = (String) this.jsonObject.get("numThread");
        return Integer.parseInt(numThreadLine);
    }

    public double getWriteProbability(){
        String writeProbabilityLine = (String) this.jsonObject.get("writeProbability");
        return Double.parseDouble(writeProbabilityLine);
    }

    public int getExperimentTime(){
        String experimentTimeLine = (String) this.jsonObject.get("experiment");
        return Integer.parseInt(experimentTimeLine);
    }

}

package gra_test;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.*;

public class MongoCommand {
    private MongoClient client;
    private MongoCollection<Document> collection;

    public MongoCommand(MongoClient client, MongoCollection<Document> collection)
    {
        this.client = client;
        this.collection = collection;
    }

    public Document UpdateAsync(ObjectId id, int val)
    {
        Bson filter = eq("_id", id);
        Bson updateOperation = set("val", val);
        return collection.findOneAndUpdate(filter, updateOperation);

    }

    public void WriteAsync(Document doc)
    {
        collection.insertOne(doc);
    }

    public Document GetAsync(ObjectId id)
    {
        return collection.find(eq("_id", id)).first();
    }
}

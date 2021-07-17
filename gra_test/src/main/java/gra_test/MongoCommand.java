package gra_test;


import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
//import com.mongodb.reactivestreams.client.MongoClient;
//import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;

import com.mongodb.client.model.FindOneAndUpdateOptions;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.concurrent.TimeUnit;

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

    public Document UpdateAsync(ObjectId id, int val) throws Throwable {
        Bson filter = eq("_id", id);
        Bson updateOperation = set("val", val);
        FindOneAndUpdateOptions option = new FindOneAndUpdateOptions().maxTime(1000, TimeUnit.MILLISECONDS);
        return collection.findOneAndUpdate(filter, updateOperation, option);

//        SubscriberHelpers.ObservableSubscriber<Document> subscriber = new SubscriberHelpers.ObservableSubscriber<>();
//        collection.findOneAndUpdate(filter, updateOperation).subscribe(subscriber);
//        subscriber.await();
//        return subscriber.getReceived().get(0);
    }

    public void WriteAsync(Document doc) throws Throwable {
        collection.insertOne(doc);
//        SubscriberHelpers.ObservableSubscriber<Object> subscriber = new SubscriberHelpers.ObservableSubscriber<>();
//        collection.insertOne(doc).subscribe(subscriber);
//        subscriber.await();
    }

    public Document GetAsync(ObjectId id) throws Throwable {
        return collection.find(eq("_id", id)).first();

//        SubscriberHelpers.ObservableSubscriber<Document> subscriber = new SubscriberHelpers.ObservableSubscriber<>();
//        collection.find(eq("_id", id)).first().subscribe(subscriber);
//        subscriber.await();
//        return subscriber.getReceived().get(0);
    }
}

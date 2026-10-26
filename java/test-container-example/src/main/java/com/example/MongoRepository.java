package com.example;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.Document;

import java.util.Optional;

/** Minimal MongoDB-backed repository for users. */
public class MongoRepository {

    private final MongoCollection<Document> collection;

    public MongoRepository(MongoClient client, String database) {
        this.collection = client.getDatabase(database).getCollection("users");
    }

    public void save(String id, String name) {
        collection.insertOne(new Document("_id", id).append("name", name));
    }

    public Optional<String> findNameById(String id) {
        Document doc = collection.find(Filters.eq("_id", id)).first();
        return Optional.ofNullable(doc).map(d -> d.getString("name"));
    }

    public long count() {
        return collection.countDocuments();
    }
}

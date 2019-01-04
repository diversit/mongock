package com.github.cloudyrock.mongock.test.proxy;

import com.github.cloudyrock.mongock.ChangeLog;
import com.github.cloudyrock.mongock.ChangeSet;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

/**
 *
 * @since 04/04/2018
 */
@ChangeLog(order = "5")
public class ProxiesMongockTestResource {

  @ChangeSet(author = "testuser", id = "ProxyMongoDatabaseInsertTest", order = "01")
  public void testInsertWithDB(MongoDatabase mongoDatabase) {
    MongoCollection<Document> coll = mongoDatabase.getCollection("anyCollection");

    coll.insertOne(Document.parse(new BasicDBObject("value", "value1").toJson()));
    coll.insertOne(Document.parse(new BasicDBObject("value", "value2").toJson()));
  }

  @ChangeSet(author = "testuser", id = "ProxyMongoDatabaseTest", order = "02")
  public void testMongoDatabase(MongoDatabase mongoDatabase) {
    System.out.println("invoked ProxyMongoDatabaseTest with db=" + mongoDatabase.toString());
  }

}

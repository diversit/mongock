package com.github.cloudyrock.mongock;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;
import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;

import java.net.InetSocketAddress;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * For test cases, build a new {@link de.bwaldvogel.mongo.MongoServer} instance
 * to be used in tests.
 */
public abstract class MongoServerBuilder {

  /**
   * On every call, creates a new in-memory MongoServer on a random port
   * and returns a database with given name.
   *
   * @param databaseName Database name to return.
   * @return MongoDatabase with given name.
   */
  protected MongoDatabase createServerAndGetDatabase(String databaseName) {
    MongoServer mongoServer = new MongoServer(new MemoryBackend());
    InetSocketAddress serverAddress = mongoServer.bind();
    MongoClient mongoClient = new MongoClient(new ServerAddress(serverAddress));

    return mongoClient.getDatabase(databaseName);
  }

  /**
   * @param fakeMongoDatabase Database to return by mocked MongoClient.
   * @return A mocked MongoClient which always return given database.
   */
  protected MongoClient getMockMongoClient(MongoDatabase fakeMongoDatabase) {
    MongoClient mongoClient = mock(MongoClient.class);
    when(mongoClient.getDatabase(anyString())).thenReturn(fakeMongoDatabase);
    return mongoClient;
  }

}

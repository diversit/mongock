package com.github.cloudyrock.mongock.test.changelogs;

import com.github.cloudyrock.mongock.ChangeLog;
import com.github.cloudyrock.mongock.ChangeSet;
import com.mongodb.DB;
import com.mongodb.client.MongoDatabase;

/**
 *
 * @since 30.07.14
 */
@ChangeLog(order = "2")
public class AnotherMongockTestResource {

  @ChangeSet(author = "testuser", id = "Btest1", order = "01")
  public void testChangeSet() {
    System.out.println("invoked B1");
  }

  @ChangeSet(author = "testuser", id = "Btest2", order = "02")
  public void testChangeSet2() {
    System.out.println("invoked B2");
  }

  @ChangeSet(author = "testuser", id = "Btest3", order = "03")
  public void testChangeSet3() {
    System.out.println("invoked B3");
  }

  @ChangeSet(author = "testuser", id = "Btest4", order = "04", runAlways = true)
  public void testChangeSetWithAlways() {
    System.out.println("invoked B4 with always");
  }

  @ChangeSet(author = "testuser", id = "Btest5", order = "05")
  public void testChangeSet6(MongoDatabase mongoDatabase) {
    System.out.println("invoked B5 with db=" + mongoDatabase.toString());
  }

}

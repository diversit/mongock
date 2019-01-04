package com.github.cloudyrock.mongock.test.changelogs;

import com.github.cloudyrock.mongock.ChangeLog;
import com.github.cloudyrock.mongock.ChangeSet;
import com.mongodb.DB;
import com.mongodb.client.MongoDatabase;

/**
 *
 * @since 27/07/2014
 */
@ChangeLog(order = "1")
public class MongockTestResource {

  @ChangeSet(author = "testuser", id = "test1", order = "01")
  public void testChangeSet() {

    System.out.println("invoked 1");

  }

  @ChangeSet(author = "testuser", id = "test2", order = "02")
  public void testChangeSet2() {

    System.out.println("invoked 2");

  }

  @ChangeSet(author = "testuser", id = "test3", order = "03")
  public void testChangeSet3() {

    System.out.println("invoked 3");

  }

  @ChangeSet(author = "testuser", id = "test4", order = "04")
  public void testChangeSet4(MongoDatabase mongoDatabase) {

    System.out.println("invoked 4 with mongoDatabase=" + mongoDatabase.toString());

  }

}

package com.github.cloudyrock.mongock;

import com.github.cloudyrock.mongock.test.changelogs.MongockTestResource;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import org.junit.After;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.data.mongodb.core.MongoTemplate;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Class to provide common configuration for Mongock**Test
 *
 *
 * @since 04/04/2018
 */
public class SpringMongockTestBase extends MongoServerBuilder {

  static final String CHANGELOG_COLLECTION_NAME = "dbchangelog";

  protected SpringMongock runner;

  protected MongoDatabase fakeMongoDatabase;

  @Mock
  protected ChangeEntryRepository changeEntryRepository;

  @Mock
  protected LockChecker lockChecker;

  @Mock
  protected LockRepository lockRepository;

  @Spy
  protected SpringChangeService changeService;

  protected MongoClient mongoClient;

  @Mock
  private MongoRepository indexDao;

  @Before
  public void init() throws Exception {
    fakeMongoDatabase = createServerAndGetDatabase("mongocktest");
    com.github.cloudyrock.mongock.TestUtils.setField(changeEntryRepository, "mongoDatabase", fakeMongoDatabase);

    doCallRealMethod().when(changeEntryRepository).save(any(ChangeEntry.class));
    TestUtils.setField(changeEntryRepository, "indexDao", indexDao);
    TestUtils.setField(changeEntryRepository, "changelogCollectionName", CHANGELOG_COLLECTION_NAME);
    TestUtils.setField(changeEntryRepository, "collection", fakeMongoDatabase.getCollection(CHANGELOG_COLLECTION_NAME));

    changeService.setChangeLogsBasePackage(MongockTestResource.class.getPackage().getName());
    mongoClient = getMockMongoClient(fakeMongoDatabase);

    SpringMongock temp = new SpringMongock(
        changeEntryRepository,
        mongoClient,
        changeService,
        lockChecker);

    temp.setChangelogMongoDatabase(fakeMongoDatabase);
    temp.setMongoTemplate(new MongoTemplate(mongoClient, "mongocktest"));
    temp.setEnabled(true);
    temp.setThrowExceptionIfCannotObtainLock(true);
    temp.setSpringEnvironment(null);
    runner = spy(temp);
  }

  @After
  public void cleanUp() throws NoSuchFieldException, IllegalAccessException {
    TestUtils.setField(runner, "mongoTemplate", null);
    TestUtils.setField(runner, "jongo", null);
    fakeMongoDatabase.drop();
  }

}

package com.github.cloudyrock.mongock;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Mongock runner
 *
 *
 * @since 26/07/2014
 */
public class Mongock implements Closeable {
  private static final Logger logger = LoggerFactory.getLogger(Mongock.class);

  protected final ChangeEntryRepository changeEntryRepository;
  protected final ChangeService changeService;
  protected final LockChecker lockChecker;
  protected final MongoClient mongoClient;

  private boolean throwExceptionIfCannotObtainLock;
  private boolean enabled;
  private MongoDatabase changelogMongoDatabase;

  protected Mongock(ChangeEntryRepository changeEntryRepository,
          MongoClient mongoClient,
          ChangeService changeService,
          LockChecker lockChecker) {
    this.changeEntryRepository = changeEntryRepository;
    this.mongoClient = mongoClient;
    this.changeService = changeService;
    this.lockChecker = lockChecker;
  }

  void setThrowExceptionIfCannotObtainLock(boolean throwExceptionIfCannotObtainLock) {
    this.throwExceptionIfCannotObtainLock = throwExceptionIfCannotObtainLock;
  }

  void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  void setChangelogMongoDatabase(MongoDatabase changelogMongoDatabase) {
    this.changelogMongoDatabase = changelogMongoDatabase;
  }

  public void execute() {
    if (!isEnabled()) {
      logger.info("Mongock is disabled. Exiting.");
      return;
    }

    try {
      lockChecker.acquireLockDefault();
      executeMigration();
    } catch (LockCheckException lockEx) {

      if (throwExceptionIfCannotObtainLock) {
        logger.error(lockEx.getMessage());
        throw new MongockException(lockEx.getMessage());
      }
      logger.warn(lockEx.getMessage());
      logger.warn("Mongock did not acquire process lock. EXITING WITHOUT RUNNING DATA MIGRATION");

    } finally {
      lockChecker.releaseLockDefault();//we do it anyway, it's idempotent
      logger.info("Mongock has finished his job.");
    }

  }

  /**
   * @return true if an execution is in progress, in any process.
   */
  public boolean isExecutionInProgress() {
    return lockChecker.isLockHeld();
  }

  /**
   * @return true if Mongock runner is enabled and able to run, otherwise false
   */
  public boolean isEnabled() {
    return enabled;
  }

  /**
   * Closes the Mongo instance used by Mongock.
   * This will close either the connection Mongock was initiated with or that which was internally created.
   */
  public void close() {
    mongoClient.close();
  }

  private void executeMigration() {
    logger.info("Mongock starting the data migration sequence..");

    for (Class<?> changelogClass : changeService.fetchChangeLogs()) {

      Object changelogInstance;
      try {
        changelogInstance = changeService.createInstance(changelogClass);
        List<Method> changesetMethods = changeService.fetchChangeSets(changelogInstance.getClass());
        for (Method changesetMethod : changesetMethods) {
          executeIfNewOrRunAlways(changelogInstance, changesetMethod, changeService.createChangeEntry(changesetMethod));
        }

      } catch (NoSuchMethodException | IllegalAccessException | InstantiationException e) {
        throw new MongockException(e.getMessage(), e);
      } catch (InvocationTargetException e) {
        Throwable targetException = e.getTargetException();
        throw new MongockException(targetException.getMessage(), e);
      }

    }
  }

  private void executeIfNewOrRunAlways(Object changelogInstance, Method changesetMethod, ChangeEntry changeEntry) throws IllegalAccessException, InvocationTargetException {
    try {
      if (changeEntryRepository.isNewChange(changeEntry)) {
        executeChangeSetMethod(changesetMethod, changelogInstance);
        changeEntryRepository.save(changeEntry);
        logger.info("{} applied", changeEntry );
      } else if (changeService.isRunAlwaysChangeSet(changesetMethod)) {
        executeChangeSetMethod(changesetMethod, changelogInstance);
        logger.info("{} re-applied", changeEntry );
      } else {
        logger.info("{} pass over", changeEntry );
      }
    } catch (MongockException e) {
      logger.error(e.getMessage());
    }
  }

  protected void executeChangeSetMethod(Method changeSetMethod, Object changeLogInstance)
      throws IllegalAccessException, InvocationTargetException {
    if (changeSetMethod.getParameterTypes().length == 1
        && changeSetMethod.getParameterTypes()[0].equals(MongoDatabase.class)) {
      logger.debug("method with MongoDatabase argument");

      changeSetMethod.invoke(changeLogInstance, this.changelogMongoDatabase);
    } else if (changeSetMethod.getParameterTypes().length == 0) {
      logger.debug("method with no params");

      changeSetMethod.invoke(changeLogInstance);
    } else {
      throw new MongockException("ChangeSet method " + changeSetMethod.getName() +
          " has wrong arguments list. Please see docs for more info!");
    }
  }
}

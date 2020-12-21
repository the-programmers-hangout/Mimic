package uk.co.markg.mimic.markov;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import uk.co.markg.mimic.database.MessageRepository;
import uk.co.markg.mimic.database.ServerConfigRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MarkovInitialiser {

  private static final Logger logger = LogManager.getLogger(MarkovInitialiser.class);
  private static final ScheduledExecutorService scheduler =
      Executors.newSingleThreadScheduledExecutor();
  private MessageRepository messageRepository;
  private ServerConfigRepository serverRepository;
  private static final String SERVER_ROOT = "markov/servers/";
  private static final String USER_ROOT = "markov/users/";

  public MarkovInitialiser() {
    messageRepository = MessageRepository.getRepository();
    serverRepository = ServerConfigRepository.getRepository();
    initChainUpdater();
  }

  public void init() {
    createRootDirs();
    initHighCapacityServers();
    initHighCapacityUsers();
  }

  private void createRootDirs() {
    checkAndCreateDir(SERVER_ROOT);
    checkAndCreateDir(USER_ROOT);
  }

  private void checkAndCreateDir(String path) {
    var dir = new File(path);
    if (!dir.exists()) {
      dir.mkdirs();
    }
  }

  private void initHighCapacityServers() {
    var highCapactityServers = messageRepository.getHighCapacityServers();
    List<Class<? extends Markov>> chainTypes = List.of(Bigram.class, Trigram.class);
    for (Class<? extends Markov> chainClass : chainTypes) {
      for (Long server : highCapactityServers) {
        String fileEnd = getFileEnd(chainClass);
        var file = new File(SERVER_ROOT + server + fileEnd);
        logger.info(file.getAbsolutePath());
        if (!file.exists()) {
          loadComplete(chainClass, server);
        } else {
          loadPartial(chainClass, file, server);
        }
      }
    }
    logger.info("Completed saving high capacity server files");
  }
  
  private String getFileEnd(Class<? extends Markov> chainClass) {
    try {
      return chainClass.getDeclaredConstructor().newInstance().getFileEnd();
    } catch (ReflectiveOperationException e) {
      e.printStackTrace();
    }
    return Markov.FILE_END;
  }

  private void loadComplete(Class<? extends Markov> chainClass, long server) {
    try {
      Markov ngram = chainClass.getDeclaredConstructor().newInstance();
      logger.info("Saving {} for server: {}", chainClass.getSimpleName(), server);
      try (var messages = messageRepository.getByServerid(server)) {
        messages.forEach(x -> ngram.parseInput(x));
      }
      ngram.setLastMessageId(messageRepository.getLatestServerMessage(server));
      try {
        ngram.save(SERVER_ROOT + server);
      } catch (IOException e) {
        e.printStackTrace();
      }
    } catch (ReflectiveOperationException e) {
      e.printStackTrace();
    }
  }

  private void loadPartial(Class<? extends Markov> chainClass, File file, long server) {
    try {
      Markov ngram = MarkovLoader.of(chainClass).from(file);
      if (messageRepository.getLatestServerMessage(server) > ngram.getLastMessageId()) {
        try (var messages =
            messageRepository.getByServeridFromMessage(server, ngram.getLastMessageId())) {
          messages.forEach(x -> ngram.parseInput(x));
        }
        ngram.save(SERVER_ROOT + server);
      } else {
        logger.info("{} chain for server {} is up to date", chainClass.getSimpleName(), server);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void initHighCapacityUsers() {
    var servers = serverRepository.getAllServerIds();
    for (Long server : servers) {
      var users = messageRepository.getHighCapacityUsers(server);
      for (Long user : users) {
        logger.info("Saving bigram for user {} in server {}.", user, server);
        Markov markov = new Bigram();
        try (var messages = messageRepository.getByUserid(user, server)) {
          messages.forEach(x -> markov.parseInput(x));
        }
        try {
          final String dirPath = USER_ROOT + server + "/";
          checkAndCreateDir(dirPath);
          markov.save(dirPath + user);
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  private void initChainUpdater() {
    scheduler.scheduleAtFixedRate(() -> init(), 12, 12, TimeUnit.HOURS);
  }

}

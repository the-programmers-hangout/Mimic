package uk.co.markg.mimic.markov;

import java.io.File;
import java.io.IOException;
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
    for (Long server : highCapactityServers) {
      Markov bigram = new Bigram();
      logger.info("Saving bigram for server: {}", server);
      try (var messages = messageRepository.getByServerid(server)) {
        messages.forEach(x -> bigram.parseInput(x));
      }
      try {
        bigram.save(SERVER_ROOT + server);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    for (Long server : highCapactityServers) {
      Markov trigram = new Trigram();
      logger.info("Saving trigam for server: {}", server);
      try (var messages = messageRepository.getByServerid(server)) {
        messages.forEach(x -> trigram.parseInput(x));
      }
      try {
        trigram.save(SERVER_ROOT + server);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    logger.info("Completed saving high capacity server files");
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

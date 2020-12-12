package uk.co.markg.mimic.markov;

import java.io.File;
import java.io.IOException;
import uk.co.markg.mimic.database.MessageRepository;
import uk.co.markg.mimic.database.ServerConfigRepository;

public class MarkovInitialiser {

  private MessageRepository messageRepository;
  private ServerConfigRepository serverRepository;
  private static final String SERVER_ROOT = "markov/servers/";
  private static final String USER_ROOT = "markov/users/";

  public MarkovInitialiser() {
    messageRepository = MessageRepository.getRepository();
    serverRepository = ServerConfigRepository.getRepository();
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
      Markov markov = new Markov();
      try (var messages = messageRepository.getByServerid(server)) {
        messages.forEach(x -> markov.parseInput(x));
      }
      try {
        markov.save(SERVER_ROOT + server);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  private void initHighCapacityUsers() {
    var servers = serverRepository.getAllServerIds();
    for (Long server : servers) {
      var users = messageRepository.getHighCapacityUsers(server);
      for (Long user : users) {
        Markov markov = new Markov();
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

}

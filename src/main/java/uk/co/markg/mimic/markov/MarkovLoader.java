package uk.co.markg.mimic.markov;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Optional;
import com.esotericsoftware.kryo.io.Input;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.co.markg.mimic.database.MessageRepository;

public class MarkovLoader {

  private static final Logger logger = LogManager.getLogger(MarkovLoader.class);

  private Class<? extends Markov> clazz;
  private final String fileEnd;

  public static MarkovLoader of(Class<? extends Markov> clazz) {
    return new MarkovLoader(clazz);
  }

  private MarkovLoader(Class<? extends Markov> clazz) {
    this.clazz = clazz;
    String fileSuffix = "";
    try {
      fileSuffix = clazz.getDeclaredConstructor().newInstance().getFileEnd();
    } catch (ReflectiveOperationException | IllegalArgumentException | SecurityException e) {
      e.printStackTrace();
    }
    this.fileEnd = fileSuffix;
  }

  /**
   * Creates a new {@link uk.co.markg.mimic.markov.Bigram Markov} instance loaded from the file.
   * 
   * @param f The file containing the saved messages
   * @return The {@link uk.co.markg.mimic.markov.Bigram Markov} instance containing the saved users
   *         messages
   * @throws IOException If the file is not found
   */
  public Markov from(File f) throws IOException {
    logger.info("Loaded from file {}", f.getAbsolutePath());
    Input input = new Input(new FileInputStream(f.getAbsolutePath()));
    Markov markov = Markov.kryo.readObject(input, clazz);
    input.close();
    return markov;
  }

  public Markov loadServer(long serverid) {
    File file = new File("markov/servers/" + serverid + fileEnd);
    var markov = file.exists() ? loadFromFile(file) : loadChain(serverid);
    return markov.orElseThrow(RuntimeException::new);
  }

  public Markov loadUser(long userid, long serverid) {
    File file = new File("markov/users/" + serverid + "/" + userid + fileEnd);
    var markov = file.exists() ? loadFromFile(file) : loadUserChain(userid, serverid);
    return markov.orElseThrow(RuntimeException::new);
  }

  /**
   * Loads Markov chain from file.
   * 
   * @param file The Markov file to be loaded
   */
  private Optional<Markov> loadFromFile(File file) {
    logger.info("Loading chain from file {}", file.getAbsolutePath());
    try {
      return Optional.of(from(file));
    } catch (IOException e) {
      logger.error(e.getMessage(), e);
    }
    return Optional.empty();
  }

  /**
   * Loads chain from database.
   * 
   * @param event The {@link net.dv8tion.jda.api.events.message.MessageReceivedEvent
   *              MessageReceivedEvent} instance
   */
  private Optional<Markov> loadChain(long serverid) {
    logger.info("Loading chain from database");
    var repo = MessageRepository.getRepository();
    try {
      Markov chain = clazz.getDeclaredConstructor().newInstance();
      try (var messages = repo.getByServerid(serverid)) {
        messages.forEach(x -> chain.parseInput(x));
      }
      return Optional.of(chain);
    } catch (ReflectiveOperationException | IllegalArgumentException | SecurityException e) {
      e.printStackTrace();
    }
    return Optional.empty();
  }

  /**
   * Loads chain from database.
   * 
   * @param event The {@link net.dv8tion.jda.api.events.message.MessageReceivedEvent
   *              MessageReceivedEvent} instance
   */
  private Optional<Markov> loadUserChain(long userid, long serverid) {
    logger.info("Loading chain from database");
    var repo = MessageRepository.getRepository();
    try {
      Markov chain = clazz.getDeclaredConstructor().newInstance();
      try (var messages = repo.getByUserid(userid, serverid)) {
        messages.forEach(x -> chain.parseInput(x));
      }
      return Optional.of(chain);
    } catch (ReflectiveOperationException | IllegalArgumentException | SecurityException e) {
      e.printStackTrace();
    }
    return Optional.empty();
  }

}

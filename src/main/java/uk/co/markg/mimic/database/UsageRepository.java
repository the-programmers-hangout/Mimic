package uk.co.markg.mimic.database;

import static uk.co.markg.mimic.db.tables.Usage.USAGE;
import java.time.LocalDateTime;
import org.jooq.DSLContext;
import disparse.parser.reflection.Injectable;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import uk.co.markg.mimic.db.tables.pojos.Usage;

public class UsageRepository {
  private DSLContext dsl;

  /**
   * {@link disparse.parser.reflection.Injectable Injectable} method used by disparse upon command
   * invocation.
   * 
   * @return a new usage respository instance
   */
  @Injectable
  public static UsageRepository getRepository() {
    return new UsageRepository();
  }

  private UsageRepository() {
    dsl = JooqConnection.getJooqContext();
  }

  public int save(Class<?> clazz, MessageReceivedEvent event) {
    Usage usage = new Usage();
    usage.setCommand(clazz.getSimpleName());
    usage.setServerid(event.getGuild().getIdLong());
    usage.setUsagetime(LocalDateTime.now());
    return save(usage);
  }

  private int save(Usage usage) {
    return dsl.executeInsert(dsl.newRecord(USAGE, usage));
  }

  public int deleteByServerId(long serverid) {
    return dsl.deleteFrom(USAGE).where(USAGE.SERVERID.eq(serverid)).execute();
  }
}

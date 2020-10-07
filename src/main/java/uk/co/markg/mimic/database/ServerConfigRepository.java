package uk.co.markg.mimic.database;

import org.jooq.DSLContext;
import disparse.parser.reflection.Injectable;
import uk.co.markg.mimic.db.tables.pojos.ServerConfig;
import static uk.co.markg.mimic.db.tables.ServerConfig.SERVER_CONFIG;

public class ServerConfigRepository {

  private DSLContext dsl;

  @Injectable
  public static ServerConfigRepository getRepository() {
    return new ServerConfigRepository();
  }

  public ServerConfigRepository() {
    dsl = JooqConnection.getJooqContext();
  }

  public int save(ServerConfig serverConfig) {
    return dsl.executeInsert(dsl.newRecord(SERVER_CONFIG, serverConfig));
  }

  public int delete(long serverid) {
    return dsl.deleteFrom(SERVER_CONFIG).where(SERVER_CONFIG.SERVERID.eq(serverid)).execute();
  }

}

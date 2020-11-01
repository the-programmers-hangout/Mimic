package uk.co.markg.mimic.database;

import org.jooq.DSLContext;
import disparse.parser.reflection.Injectable;
import uk.co.markg.mimic.db.tables.pojos.ServerConfig;
import static uk.co.markg.mimic.db.tables.ServerConfig.SERVER_CONFIG;

/**
 * A {@link org.jooq.DSLContext DSLContext} implementation to access the
 * {@link uk.co.markg.mimic.db.tables.ServerConfig ServerConfig} table generated by JOOQ.
 */
public class ServerConfigRepository {

  private DSLContext dsl;

  /**
   * {@link disparse.parser.reflection.Injectable Injectable} method used by disparse upon command
   * invocation.
   * 
   * @return a new server config repository instance
   */
  @Injectable
  public static ServerConfigRepository getRepository() {
    return new ServerConfigRepository();
  }

  public ServerConfigRepository() {
    dsl = JooqConnection.getJooqContext();
  }

  /**
   * Gets the server config from the database.
   * 
   * @param serverid The target server
   * @return The {@link uk.co.markg.mimic.db.tables.pojos.ServerConfig ServerConfig} instance
   */
  public ServerConfig get(long serverid) {
    return dsl.selectFrom(SERVER_CONFIG).where(SERVER_CONFIG.SERVERID.eq(serverid))
        .fetchOneInto(ServerConfig.class);
  }

  /**
   * Saves the server Configuration into the database. If the server already exists, it will update
   * the config instance.
   * 
   * @param config The server config you want to add/update
   * @return The number of inserted or modified records
   */
  public int save(ServerConfig config) {
    if (exists(config.getServerid())) {
      return dsl.update(SERVER_CONFIG).set(SERVER_CONFIG.OPT_IN_ROLE, config.getOptInRole())
          .where(SERVER_CONFIG.SERVERID.eq(config.getServerid())).execute();
    }
    return dsl.executeInsert(dsl.newRecord(SERVER_CONFIG, config));
  }

  /**
   * Deletes the server config from the database.
   * 
   * @param serverid The target server to be deleted
   * @return The number of deleted records
   */
  public int delete(long serverid) {
    return dsl.deleteFrom(SERVER_CONFIG).where(SERVER_CONFIG.SERVERID.eq(serverid)).execute();
  }

  /**
   * Checks whether or not a server config exists in the database.
   * 
   * @param serverid The target server
   * @return True if the server config exists
   */
  public boolean exists(long serverid) {
    return dsl.selectFrom(SERVER_CONFIG).where(SERVER_CONFIG.SERVERID.eq(serverid)).fetchOne(0,
        int.class) != 0;
  }
}

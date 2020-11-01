package uk.co.markg.mimic.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import disparse.parser.reflection.Injectable;

public class JooqConnection {
  private static final Logger logger = LogManager.getLogger(JooqConnection.class);
  private static HikariConfig config;
  private static DSLContext dsl;

  private JooqConnection() {
  }

  /**
   * Initialises the Jooq {@link org.jooq.DSLContext DSLContext} if it has not been initialised and
   * returns it
   * 
   * @return The dsl context
   */
  @Injectable
  public static DSLContext getJooqContext() {
    if (dsl == null && config == null) {
      init();
    }
    return dsl;
  }

  /**
   * Initialises the Jooq {@link org.jooq.DSLContext DSLContext} with a
   * {@link com.zaxxer.hikari.HikariDataSource HikariDataSource}
   */
  private static void init() {
    logger.info("Initialising Jooq context");
    config = new HikariConfig();
    config.setJdbcUrl(System.getenv("B_HOST"));
    config.setUsername(System.getenv("B_USER"));
    config.setPassword(System.getenv("B_PASS"));
    dsl = DSL.using(new HikariDataSource(config), SQLDialect.POSTGRES);
  }
}

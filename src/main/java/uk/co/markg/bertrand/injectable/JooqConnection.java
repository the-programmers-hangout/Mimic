package uk.co.markg.bertrand.injectable;

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

  @Injectable
  public static DSLContext getJooqContext() {
    if (dsl == null && config == null) {
      init();
    }
    return dsl;
  }

  private static void init() {
    logger.info("Initialising Jooq context");
    config = new HikariConfig();
    config.setJdbcUrl(System.getenv("B_HOST"));
    config.setUsername(System.getenv("B_USER"));
    config.setPassword(System.getenv("B_PASS"));
    dsl = DSL.using(new HikariDataSource(config), SQLDialect.POSTGRES);
  }
}

package uk.co.markg.mimic.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.codegen.GenerationTool;
import org.jooq.impl.DSL;
import org.jooq.meta.jaxb.Configuration;
import org.jooq.meta.jaxb.Database;
import org.jooq.meta.jaxb.Generate;
import org.jooq.meta.jaxb.Generator;
import org.jooq.meta.jaxb.Jdbc;
import org.jooq.meta.jaxb.Target;
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

  /**
   * Will run jooq code generation to build records, pojos etc for jooq. Run only when database
   * structure has been migrated.
   * 
   * @throws Exception When code generation fails
   */
  public static void executeGeneration() throws Exception {
    var configuration =
        new Configuration()
            .withJdbc(new Jdbc().withDriver("org.postgresql.Driver")
                .withUrl(System.getenv("B_HOST")).withUser(
                    System.getenv("B_USER"))
                .withPassword(System.getenv("B_PASS")))
            .withGenerator(new Generator()
                .withDatabase(
                    new Database().withExcludes("flyway_schema_history|information_schema.*|pg_.*")
                        .withInputSchema("public").withOutputSchemaToDefault(Boolean.TRUE))
                .withGenerate(new Generate().withPojos(Boolean.TRUE)
                    .withDeprecationOnUnknownTypes(Boolean.FALSE))
                .withTarget(new Target().withPackageName("uk.co.markg.mimic.db")
                    .withDirectory("src/main/java")));

    GenerationTool.generate(configuration);
  }
}

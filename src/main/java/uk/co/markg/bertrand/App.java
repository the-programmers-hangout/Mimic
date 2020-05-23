package uk.co.markg.bertrand;

import javax.security.auth.login.LoginException;
import org.flywaydb.core.Flyway;
import org.jooq.codegen.GenerationTool;
import org.jooq.meta.jaxb.Configuration;
import org.jooq.meta.jaxb.Database;
import org.jooq.meta.jaxb.Generate;
import org.jooq.meta.jaxb.Generator;
import org.jooq.meta.jaxb.Jdbc;
import org.jooq.meta.jaxb.Target;
import disparse.discord.jda.Dispatcher;
import net.dv8tion.jda.api.JDABuilder;
import uk.co.markg.bertrand.listener.DeleteMessage;
import uk.co.markg.bertrand.listener.MarkovResponse;
import uk.co.markg.bertrand.listener.MessageReader;

/**
 * Hello world!
 *
 */
public class App {
  public static final String PREFIX = "mimic!";

  public static void main(String[] args) throws Exception {
    initDatabase();
    if (args.length == 1 && "--generate".equals(args[0])) {
      executeJooqGeneration();
    }
    launchBot();
  }

  /**
   * Initialises Flyway and initiates any required db migrations
   */
  private static void initDatabase() {
    Flyway.configure()
        .dataSource(System.getenv("B_HOST"), System.getenv("B_USER"), System.getenv("B_PASS"))
        .load().migrate();
  }

  /**
   * Will run jooq code generation to build records, pojos etc for jooq. Run only when database
   * structure has been migrated.
   * 
   * @throws Exception When code generation fails
   */
  private static void executeJooqGeneration() throws Exception {
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
                .withTarget(new Target().withPackageName("uk.co.markg.bertrand.db")
                    .withDirectory("src/main/java")));

    GenerationTool.generate(configuration);
  }

  /**
   * Prepares the disparse {@link disparse.discord.Dispatcher Dispatcher}, adds listeners to the bot
   * and builds the jda instance
   * 
   * @throws LoginException
   * @throws InterruptedException
   */
  private static void launchBot() throws LoginException, InterruptedException {
    var builder = Dispatcher.init(JDABuilder.createDefault(System.getenv("B_TOKEN")), PREFIX, 10);
    builder.addEventListeners(new MessageReader(), new MarkovResponse(), new DeleteMessage());
    builder.build().awaitReady();
  }
}

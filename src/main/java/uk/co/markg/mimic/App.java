package uk.co.markg.mimic;

import java.awt.Color;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
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
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import uk.co.markg.mimic.listener.GuildListener;
import uk.co.markg.mimic.listener.MarkovResponse;
import uk.co.markg.mimic.listener.MemberLeave;
import uk.co.markg.mimic.listener.MessageReader;
import uk.co.markg.mimic.listener.ModifyMessage;

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
                .withTarget(new Target().withPackageName("uk.co.markg.mimic.db")
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
    Dispatcher.Builder dispatcherBuilder = new Dispatcher.Builder(App.class).prefix(PREFIX)
        .pageLimit(10).withHelpBaseEmbed(() -> new EmbedBuilder().setColor(Color.decode("#eb7701")))
        .description("Mimic: A bot that talks like you!");

    var builder = Dispatcher.init(JDABuilder.create(System.getenv("B_TOKEN"), getIntents()),
        dispatcherBuilder.build());
    builder.addEventListeners(new MessageReader(), new ModifyMessage(), new MemberLeave(),
        new GuildListener());
    builder.disableCache(getFlags());
    builder.build().awaitReady();
  }

  private static List<GatewayIntent> getIntents() {
    List<GatewayIntent> intents = new ArrayList<>();
    intents.add(GatewayIntent.GUILD_EMOJIS);
    intents.add(GatewayIntent.GUILD_MESSAGES);
    intents.add(GatewayIntent.GUILD_MEMBERS);
    return intents;
  }

  private static EnumSet<CacheFlag> getFlags() {
    List<CacheFlag> flags = new ArrayList<>();
    flags.add(CacheFlag.ACTIVITY);
    flags.add(CacheFlag.VOICE_STATE);
    flags.add(CacheFlag.CLIENT_STATUS);
    return EnumSet.copyOf(flags);
  }
}

package uk.co.markg.mimic;

import java.awt.Color;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import javax.security.auth.login.LoginException;
import org.flywaydb.core.Flyway;
import disparse.discord.jda.Dispatcher;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import uk.co.markg.mimic.database.JooqConnection;
import uk.co.markg.mimic.listener.GuildListener;
import uk.co.markg.mimic.listener.MemberLeave;
import uk.co.markg.mimic.listener.MessageReader;
import uk.co.markg.mimic.listener.ModifyMessage;
import uk.co.markg.mimic.markov.MarkovInitialiser;

/**
 * Hello world!
 *
 */
public class App {
  public static final String PREFIX = "mimic!";

  public static void main(String[] args) throws Exception {
    initDatabase();
    if (args.length == 1 && "--generate".equals(args[0])) {
      JooqConnection.executeGeneration();
    }
    new MarkovInitialiser().init();
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
   * Prepares the disparse {@link disparse.discord.Dispatcher Dispatcher}, adds listeners to the bot
   * and builds the jda instance
   * 
   * @throws LoginException
   * @throws InterruptedException
   */
  private static void launchBot() throws LoginException, InterruptedException {
    Dispatcher.Builder dispatcherBuilder = new Dispatcher.Builder(App.class).prefix(PREFIX)
        .pageLimit(10).withHelpBaseEmbed(() -> new EmbedBuilder().setColor(Color.decode("#eb7701")))
        .description("Mimic: A bot that talks like you!")
        .autogenerateReadmeWithNameAndPath("", "COMMANDS.md");

    var builder = Dispatcher.init(JDABuilder.create(System.getenv("B_TOKEN"), getIntents()),
        dispatcherBuilder.build());
    builder.addEventListeners(new MessageReader(), new ModifyMessage(), new MemberLeave(),
        new GuildListener());
    builder.disableCache(getFlags());
    builder.build().awaitReady();
    MessageAction.setDefaultMentions(EnumSet.noneOf(Message.MentionType.class));
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

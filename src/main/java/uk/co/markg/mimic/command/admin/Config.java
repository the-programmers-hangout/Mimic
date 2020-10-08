package uk.co.markg.mimic.command.admin;

import java.time.temporal.ChronoUnit;
import disparse.discord.AbstractPermission;
import disparse.discord.jda.DiscordRequest;
import disparse.parser.dispatch.CooldownScope;
import disparse.parser.reflection.CommandHandler;
import disparse.parser.reflection.Cooldown;
import disparse.parser.reflection.Flag;
import disparse.parser.reflection.ParsedEntity;
import disparse.parser.reflection.Populate;
import uk.co.markg.mimic.database.ServerConfigRepository;
import uk.co.markg.mimic.db.tables.pojos.ServerConfig;

public class Config {

  private DiscordRequest request;
  private ConfigRequest flags;
  private ServerConfigRepository serverConfigRepository;

  @Populate
  public Config(DiscordRequest request, ConfigRequest flags,
      ServerConfigRepository serverConfigRepository) {
    this.request = request;
    this.flags = flags;
    this.serverConfigRepository = serverConfigRepository;
  }

  @ParsedEntity
  static class ConfigRequest {
    @Flag(shortName = 'o', longName = "opt",
        description = "Set the required role for using the opt-in command")
    String optInRole = "";
  }

  @Cooldown(amount = 5, unit = ChronoUnit.SECONDS, scope = CooldownScope.USER,
      sendCooldownMessage = false)
  @CommandHandler(commandName = "config", description = "Setup per-server config",
      perms = AbstractPermission.BAN_MEMBERS)
  public void execute() {
    long serverid = request.getEvent().getGuild().getIdLong();
    serverConfigRepository.save(new ServerConfig(serverid, flags.optInRole));
    request.getEvent().getChannel().sendMessage("Opt-in role set to " + flags.optInRole).queue();
  }

}

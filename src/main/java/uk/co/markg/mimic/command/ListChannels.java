package uk.co.markg.mimic.command;

import java.awt.Color;
import java.time.temporal.ChronoUnit;
import java.util.List;
import disparse.discord.AbstractPermission;
import disparse.discord.jda.DiscordRequest;
import disparse.parser.dispatch.CooldownScope;
import disparse.parser.reflection.CommandHandler;
import disparse.parser.reflection.Cooldown;
import disparse.parser.reflection.Populate;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import uk.co.markg.mimic.database.ChannelRepository;
import uk.co.markg.mimic.db.tables.pojos.Channels;

public class ListChannels {
  private MessageReceivedEvent event;
  private ChannelRepository channelRepo;

  /**
   * Command execution method held by Disparse.
   * 
   * @param request     The {@link disparse.discord.jda.DiscordRequest DiscordRequest} dispatched to
   *                    this command
   * @param channelRepo The {@link uk.co.markg.mimic.database.ChannelRepository ChannelRepository}
   *                    used to communicate with the database
   */
  @Populate
  public ListChannels(DiscordRequest request, ChannelRepository channelRepo) {
    this.event = request.getEvent();
    this.channelRepo = channelRepo;
  }

  /**
   * Command execution method held by Disparse. Has a cooldown of five seconds per user.
   */
  @Cooldown(amount = 5, unit = ChronoUnit.SECONDS, scope = CooldownScope.USER,
      sendCooldownMessage = false)
  @CommandHandler(commandName = "channels", description = "Lists all read-only channels registered")
  public void executeList() {
    this.execute();
  }

  /**
   * Command execution method held by Disparse
   */
  @Cooldown(amount = 5, unit = ChronoUnit.SECONDS, scope = CooldownScope.USER,
      sendCooldownMessage = false)
  @CommandHandler(commandName = "channels.full", description = "Lists all channels registered",
      perms = AbstractPermission.BAN_MEMBERS)
  public void executeListFull() {
    this.executeFull();
  }

  /**
   * Controls execution of the command. Retrieves all channels that have the read-only permission
   * from the database and builds an embed to send to discord
   */
  private void execute() {
    sendChannelList(channelRepo.getAllReadable(event.getGuild().getIdLong()));
  }

  /**
   * Controls execution of the command. Retrieves all channels from the database and builds an embed
   * to send to discord
   */
  private void executeFull() {
    sendChannelList(channelRepo.getAll(event.getGuild().getIdLong()));
  }

  /**
   * Sends an {@link net.dv8tion.jda.api.entities.MessageEmbed MessageEmbed} containing the list of
   * channels and their permissions.
   * 
   * @param channels the list of channels retrieved from the database
   */
  private void sendChannelList(List<Channels> channels) {
    var message = buildListOfChannels(channels);
    event.getChannel().sendMessage(message).queue();
  }

  /**
   * Create a {@link net.dv8tion.jda.api.entities.MessageEmbed MessageEmbed} to display all the
   * channels and their respective permissions.
   *
   * @param channels the list of channels retrieved from the database
   * @return a {@link net.dv8tion.jda.api.entities.MessageEmbed MessageEmbed}
   */
  private MessageEmbed buildListOfChannels(List<Channels> channels) {
    EmbedBuilder eb = new EmbedBuilder();
    eb.setTitle("Channels");
    eb.setColor(Color.decode("#eb7701"));
    for (Channels channel : channels) {
      eb.addField("Channel", "<#" + channel.getChannelid() + ">", true);
      eb.addField("Read", channel.getReadPerm().toString(), true);
      eb.addField("Write", channel.getWritePerm().toString(), true);
    }
    return eb.build();
  }
}

package uk.co.markg.bertrand.command;

import java.awt.Color;
import java.util.List;
import disparse.parser.reflection.CommandHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import uk.co.markg.bertrand.database.ChannelRepository;
import uk.co.markg.bertrand.db.tables.pojos.Channels;

public class ListChannels {
  private MessageReceivedEvent event;
  private ChannelRepository channelRepo;

  public ListChannels(MessageReceivedEvent event, ChannelRepository channelRepo) {
    this.event = event;
    this.channelRepo = channelRepo;
  }

  /**
   * Command execution method held by Disparse
   * 
   * @param event The message event from discord that triggered the command
   * @param repo  The channel repository used to communicate with the database
   */
  @CommandHandler(commandName = "channels", description = "Lists all channels registered",
      roles = "staff")
  public static void executeList(MessageReceivedEvent event, ChannelRepository repo) {
    new ListChannels(event, repo).execute();
  }

  /**
   * Controls execution of the command. Retrieves all channels from the database and builds an embed
   * to send to discord
   */
  private void execute() {
    var channels = channelRepo.getAll();
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

package uk.co.markg.bertrand.command;

import disparse.parser.reflection.CommandHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import java.awt.Color;

public class About {

  @CommandHandler(commandName = "about", description = "Displays info about the bot")
  public static void execute(MessageReceivedEvent event) {
    EmbedBuilder eb = new EmbedBuilder();
    eb.setTitle("Mimic Bot");
    eb.setDescription("Mimic is a bot that generates markov chains based on user messages. "
        + "Opting in will tell mimic to read the last 50,000 messages per configured channel and save your messages. "
        + "It will then read any new valid messages sent in those channels.");
    eb.setThumbnail(event.getJDA().getSelfUser().getAvatarUrl());
    eb.addField("Source", "https://github.com/Toby-Larone/bertrand", false);
    eb.setColor(Color.decode("#eb7701"));
    event.getChannel().sendMessage(eb.build()).queue();
  }
}

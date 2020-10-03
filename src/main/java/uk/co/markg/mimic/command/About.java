package uk.co.markg.mimic.command;

import java.awt.Color;
import java.time.temporal.ChronoUnit;
import disparse.discord.jda.DiscordRequest;
import disparse.discord.jda.DiscordResponse;
import disparse.parser.dispatch.CooldownScope;
import disparse.parser.reflection.CommandHandler;
import disparse.parser.reflection.Cooldown;
import net.dv8tion.jda.api.EmbedBuilder;

public class About {

  @Cooldown(amount = 1, unit = ChronoUnit.MINUTES, scope = CooldownScope.USER,
      sendCooldownMessage = false)
  @CommandHandler(commandName = "about", description = "Displays info about the bot")
  public static DiscordResponse execute(DiscordRequest request) {
    EmbedBuilder eb = new EmbedBuilder();
    eb.setTitle("Mimic Bot");
    eb.setDescription(
        "Mimic is a bot that generates messages using markov chains based on user messages. "
            + "Opting in will tell mimic to read the last 20,000 messages per configured channel and save your messages. "
            + "It will then read any new valid messages sent in those channels.");
    eb.setThumbnail(request.getEvent().getJDA().getSelfUser().getAvatarUrl());
    eb.addField("Source", "https://github.com/Toby-Larone/mimic", false);
    eb.setColor(Color.decode("#eb7701"));
    return DiscordResponse.of(eb);
  }
}

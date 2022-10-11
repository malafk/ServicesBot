package lol.maltest.minebuddiesbot.listeners;

import lol.maltest.minebuddiesbot.DiscordBot;
import lol.maltest.minebuddiesbot.impl.PanelObject;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;

import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CommandListener extends ListenerAdapter {
    private DiscordBot discordBot;
    private JDA jda;

    public CommandListener(DiscordBot discordBot, JDA jda) {
        this.discordBot = discordBot;
        this.jda = jda;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("createpanel")) {
            if(!event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
                event.reply(":x: You need `ADMINISTRATOR` permissions for this!").setEphemeral(true).queue();
                return;
            }

            OptionMapping name = event.getOption("name");
            OptionMapping emoji = event.getOption("emoji");
            OptionMapping questionOne = event.getOption("questions");

            ArrayList<String> questions = new ArrayList<>(Arrays.asList(questionOne.getAsString().split(",")));

            String panelId = name.getAsString();

            discordBot.botConfig.set("panel." + panelId + ".emoji", emoji.getAsString());
            discordBot.botConfig.set("panel." + panelId + ".questions", questions);

            try {
                discordBot.botConfig.save();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            discordBot.panelObjects.add(new PanelObject(panelId, emoji.getAsString(), questions));

            event.reply(":white_check_mark: Created that panel! Now you need to repost the Tickets Message").queue();
        }
        if (event.getName().equals("blacklist")) {
            if(!event.getMember().hasPermission(Permission.BAN_MEMBERS)) {
                event.reply(":x: You need `BAN_MEMBERS` permissions for this!").setEphemeral(true).queue();
                return;
            }
            OptionMapping name = event.getOption("user");
            ArrayList<String> blacklisted = new ArrayList<>();
            if(discordBot.botConfig.get("blacklisted") != null) {
                blacklisted = (ArrayList<String>) discordBot.botConfig.get("blacklisted");
            }
            blacklisted.add(name.getAsMember().getId());
            discordBot.botConfig.set("blacklisted", blacklisted);
            try {
                discordBot.botConfig.save();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            discordBot.blacklistedUsers.add(name.getAsMember().getId());
            event.reply("✅ Blacklisted " + name.getAsMember().getAsMention() + " from using tickets!").queue();
        }
        if (event.getName().equals("unblacklist")) {
            if(!event.getMember().hasPermission(Permission.BAN_MEMBERS)) {
                event.reply(":x: You need `BAN_MEMBERS` permissions for this!").setEphemeral(true).queue();
                return;
            }
            OptionMapping name = event.getOption("user");
            ArrayList<String> blacklisted = new ArrayList<>();
            blacklisted = (ArrayList<String>) discordBot.botConfig.get("blacklisted");
            blacklisted.remove(name.getAsMember().getId());
            discordBot.botConfig.set("blacklisted", blacklisted);
            try {
                discordBot.botConfig.save();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            discordBot.blacklistedUsers.remove(name.getAsMember().getId());
            event.reply("✅ Unblacklisted " + name.getAsMember().getAsMention() + " from using tickets!").queue();
        }
        if (event.getName().equals("adduser")) {
            if(event.getChannel().asTextChannel().getParentCategory().getId().equals(category.getId())) {
                OptionMapping name = event.getOption("user");
                event.getChannel().asTextChannel().getManager().putPermissionOverride(name.getAsMember(), EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND), null).queue();
                event.reply(":white_check_mark: Added " + name.getAsMember().getAsMention() + " to the ticket!").queue();
            }
        }
        if (event.getName().equals("removeuser")) {
            if(event.getChannel().asTextChannel().getParentCategory().getId().equals(category.getId())) {
                OptionMapping name = event.getOption("user");
                event.getChannel().asTextChannel().getManager().removePermissionOverride(name.getAsMember()).queue();
                event.reply(":white_check_mark: Removed " + name.getAsMember().getAsMention() + " from the ticket!").queue();
            }
        }
        if(event.getName().equals("displaytickets")) {
            if(!event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
                event.reply(":x: You need `ADMINISTRATOR` permissions for this!").setEphemeral(true).queue();
                return;
            }
            if(discordBot.botConfig.getSection("panel").getKeys().size() == 0) {
                event.reply(":x: There is no ticket panels!").setEphemeral(true).queue();
            }

            EmbedBuilder eb = new EmbedBuilder();
            eb.setTitle("Open a ticket");
            eb.setDescription("> Press the appropriate button to start!");
            eb.setColor(Color.decode("#2f3136"));

            ArrayList<Button> buttons = new ArrayList<>();

            for(PanelObject panelObject : discordBot.panelObjects) {
                buttons.add(panelObject.getButton());
            }

            event.reply("Done!").queue();
            event.getChannel().sendMessageEmbeds(eb.build()).addActionRow(buttons).queue();
        }
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        for(PanelObject panelObject : discordBot.panelObjects) {
            String button = panelObject.nameId + "_button";
            if (button.equals(event.getButton().getId())) {
                if(discordBot.blacklistedUsers.contains(event.getMember().getId())) {
                    event.reply(":x: You are blacklisted from using tickets!").setEphemeral(true).queue();
                    return;
                }
                event.replyModal(panelObject.getPanelModal()).queue();
                return;
            }
        }
        if(event.getButton().getId().startsWith("close_")) {
            String channelId = event.getButton().getId().replace("close_", "");
            TextChannel textChannel = guild.getTextChannelById(channelId);

            EmbedBuilder closeConfirm = new EmbedBuilder();
            closeConfirm.setColor(Color.decode("#2f3136"));
            closeConfirm.setTitle("Close Confirmination");
            closeConfirm.setDescription("Please confirm that you want to close this ticket");
            Button button = Button.danger("confirm_" + textChannel.getId(), "Close Ticket").withEmoji(Emoji.fromFormatted("✔"));
            event.replyEmbeds(closeConfirm.build()).addActionRow(button).queue();
            return;
        }
        if(event.getButton().getId().startsWith("confirm_")) {
            String channelId = event.getButton().getId().replace("confirm_", "");
            TextChannel textChannel = guild.getTextChannelById(channelId);
            textChannel.getManager().setParent(closedCategory).queue();

            Button button = Button.secondary("reopen_" + textChannel.getId(), "Re Open Ticket").withEmoji(Emoji.fromFormatted("\uD83D\uDD10"));
            Button delet = Button.secondary("delete_" + textChannel.getId(), "Delete Ticket").withEmoji(Emoji.fromFormatted("❌"));

            EmbedBuilder ticket = new EmbedBuilder();
            ticket.setTitle("Ticket Closed!");
            ticket.setColor(Color.decode("#2f3136"));
            ticket.setDescription("Closed by " + event.getMember().getAsMention() + ". Please choose what happens next");
            event.replyEmbeds(ticket.build()).addActionRow(button, delet).queue();
            return;
        }
        if(event.getButton().getId().startsWith("reopen_")) {
            String channelId = event.getButton().getId().replace("reopen_", "");
            TextChannel textChannel = guild.getTextChannelById(channelId);
            textChannel.getManager().setParent(category).queue();

            EmbedBuilder opened = new EmbedBuilder();

            opened.setTitle("Ticket Re Opened!");
            opened.setColor(Color.decode("#2f3136"));
            opened.setDescription(event.getMember().getAsMention() + " Reopened the ticket");
            event.replyEmbeds(opened.build()).queue();
            return;
        }
        if(event.getButton().getId().startsWith("delete_")) {
            String channelId = event.getButton().getId().replace("delete_", "");
            TextChannel textChannel = guild.getTextChannelById(channelId);

            event.reply("This channel will be deleted in 5 seconds.").queue();

            textChannel.delete().queueAfter(5, TimeUnit.SECONDS);
            return;
        }
    }

    @Override
    public void onModalInteraction(ModalInteractionEvent event) {
        for(PanelObject panelObject : discordBot.panelObjects) {
            String modalId = panelObject.nameId + "_modal";
            if(modalId.equals(event.getModalId())) {
                TextChannel textChannel = category.createTextChannel("ticket-" + event.getMember().getEffectiveName()).complete();
                createEmbed(panelObject, textChannel, event.getValues());
                event.reply("Created ticket " + textChannel.getAsMention() + "!").setEphemeral(true).queue();
            }
        }
    }

    public void createEmbed(PanelObject panelObject, TextChannel textChannel, List<ModalMapping> modalMappings) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle(panelObject.name);
        eb.setColor(Color.decode("#2f3136"));
        eb.setDescription("Thank you for contacting us!\nPlease be patient for a staff member to reply!");

        EmbedBuilder questions = new EmbedBuilder();
        questions.setColor(Color.decode("#2f3136"));

        for(ModalMapping modalMapping : modalMappings) {
            String result = "";
            String title = modalMapping.getId().replaceAll("()([A-Z])", "$1 $2");
            String[] words = title.split(" ");
            for(String word: words) {
                if(word.length() > 0)
                 result += Character.toUpperCase(word.charAt(0)) + word.substring(1) + " ";
            }
            System.out.println(result);
            questions.addField(result.replace("*", ""), modalMapping.getAsString(), false);
        }

        Button button = Button.danger("close_" + textChannel.getId(), "Close Ticket").withEmoji(Emoji.fromFormatted("\uD83D\uDD12"));

        textChannel.sendMessageEmbeds(eb.build(), questions.build()).addActionRow(button).queue();
    }

    Category category;
    Category closedCategory;
    Guild guild;
    @Override
    public void onReady(ReadyEvent e) {
        guild = jda.getGuildById(discordBot.botConfig.getString("guild"));
        category = guild.getCategoryById(discordBot.botConfig.getString("opened_category"));
        closedCategory = guild.getCategoryById(discordBot.botConfig.getString("closed_category/"));


        if(guild != null) {
            guild.upsertCommand("createpanel", "Add answer")
                    .addOption(OptionType.STRING, "name", "The name of panel", true)
                    .addOption(OptionType.STRING, "emoji", "Emoji for button", true)
                    .addOption(OptionType.STRING, "questions", "Questions",true)
                    .queue();

            guild.upsertCommand("displaytickets", "Create the embed for tickets")
                    .queue();

            guild.upsertCommand("adduser", "Add someone to ticket")
                    .addOption(OptionType.MENTIONABLE, "user", "The user to add", true)
                    .queue();

            guild.upsertCommand("removeuser", "Remove someone to ticket")
                    .addOption(OptionType.MENTIONABLE, "user", "The user to remove", true)
                    .queue();

            guild.upsertCommand("blacklist", "Blacklist user from opening tickets")
                    .addOption(OptionType.MENTIONABLE, "user", "The user to blacklist", true)
                    .queue();

            guild.upsertCommand("unblacklist", "Unblacklist user from opening tickets")
                    .addOption(OptionType.MENTIONABLE, "user", "The user to unblacklist", true)
                    .queue();

            System.out.println("Registered commands.");
        } else {
            System.out.println(guild.getId());
        }
    }
}

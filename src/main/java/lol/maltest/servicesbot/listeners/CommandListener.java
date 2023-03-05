package lol.maltest.servicesbot.listeners;

import lol.maltest.servicesbot.DiscordBot;
import lol.maltest.servicesbot.impl.PanelObject;
import lol.maltest.servicesbot.util.EmbedHelper;
import lol.maltest.servicesbot.util.EmojiUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.Instant;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandListener extends ListenerAdapter {

    private DiscordBot discordBot;
    private JDA jda;

    Category category;
    Category closedCategory;
    TextChannel logsChannel;
    String supportRolePing;

    Random random;

    public CommandListener(DiscordBot discordBot, JDA jda) {
        this.discordBot = discordBot;
        this.jda = jda;
        this.supportRolePing = "<@&" + discordBot.botConfig.getString("support_role") + ">";
        this.random = new Random();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getChannel() instanceof TextChannel && event.getChannel().getName().contains("vouches")) {
            EmojiUtil[] vouchEmojis = {EmojiUtil.STAR, EmojiUtil.FLAME, EmojiUtil.TICK, EmojiUtil.HEART, EmojiUtil.DOLLAR, EmojiUtil.CHAMPAGNE, EmojiUtil.ROCKET};
            EmojiUtil randomEmoji = vouchEmojis[random.nextInt(vouchEmojis.length)];
            String emojiString = randomEmoji.emoji;
            Pattern pattern = Pattern.compile("<:[^:]+:(\\d+)>");
            try (MatcherCloseable c1 = () -> pattern.matcher(emojiString)) {
                Matcher matcher = c1.getMatcher();
                if (matcher.find()) {
                    String id = matcher.group(1);
                    Pattern pattern1 = Pattern.compile("<:[^:]+:(\\d+)>");
                    try (MatcherCloseable c2 = () -> pattern1.matcher(emojiString)) {
                        Matcher matcher1 = c2.getMatcher();
                        if (matcher1.matches()) {
                            Emoji emoji = Emoji.fromCustom(matcher1.group(1), Long.parseLong(id), false);
                            event.getMessage().addReaction(emoji).complete();
                        } else {
                            System.out.println("No emoji ID found for " + emojiString);
                        }
                    }
                } else {
                    System.out.println("No emoji ID found for " + emojiString);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FunctionalInterface
    interface MatcherCloseable extends Closeable {
        Matcher getMatcher();
        @Override
        default void close() throws IOException {}
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("stealemoji")) {
            if (!event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
                event.reply(EmojiUtil.CROSS.emoji + " You need `ADMINISTRATOR` permissions for this!").setEphemeral(true).queue();
                return;
            }
            OptionMapping emoji = event.getOption("emoji");
            Pattern pattern = Pattern.compile("<:[^:]+:(\\d+)>");
            Matcher matcher = pattern.matcher(emoji.getAsString());
            if(!matcher.matches()) {
                event.reply(EmojiUtil.CROSS.emoji + " You didnt pass through a valid emoji").queue();
                System.out.println(emoji.getAsString() +" was invalid emoji");
                return;
            }
            String emojiId = matcher.group(1);
            String url = "https://cdn.discordapp.com/emojis/" + emojiId + ".png";

            // save

            try {
                // Download the emoji image from the URL
                InputStream imageStream = new URL(url).openStream();
                byte[] imageData = imageStream.readAllBytes();
                imageStream.close();

                // Create and save the emoji to the server
                Pattern pattern1 = Pattern.compile("<:([^:]+):\\d+>");
                Matcher matcher1 = pattern1.matcher(emoji.getAsString());
                if(!matcher1.matches()) {
                    event.reply(EmojiUtil.CROSS.emoji + " You didnt pass through a valid emoji").queue();
                    System.out.println(emoji.getAsString() +" was invalid emoji");
                    return;
                }
                RichCustomEmoji r = event.getGuild().createEmoji(matcher1.group(1), Icon.from(imageData)).complete();
                event.reply(EmojiUtil.TICK.emoji + " Saved the emoji <:" + r.getName() + ":" + r.getId() + "> to the server!").queue();
            } catch (IOException e) {
                // Handle any errors that occur while downloading or creating the emoji
                e.printStackTrace();
            }
        }
        if (event.getName().equals("createpanel")) {
            if (!event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
                event.reply(EmojiUtil.CROSS.emoji + " You need `ADMINISTRATOR` permissions for this!").setEphemeral(true).queue();
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

            event.reply(EmojiUtil.TICK.emoji + " Created that panel! Now you need to repost the Tickets Message").queue();
        }
        if (event.getName().equals("claim")) {
            if (!event.getMember().getRoles().contains(discordBot.supportRole)) {
                event.reply(EmojiUtil.CROSS.emoji + " You need the support role for this!").setEphemeral(true).queue();
                return;
            }

            event.getChannel().asTextChannel().getManager().setTopic("Claimed by " + event.getMember().getEffectiveName() + " | <t:" + new Date().getTime() / 1000 + ":F>").queue();

            EmbedBuilder eb = new EmbedBuilder();
            eb.setTitle(discordBot.ticketPrefix + " Claimed");
            eb.setDescription(EmojiUtil.TICK.emoji + " Your " + discordBot.ticketPrefix + " has been claimed by **<@" + event.getMember().getId() + ">**! They will complete this " + discordBot.ticketPrefix + " for you.");
            eb.setColor(Color.decode("#2f3136"));
            event.replyEmbeds(eb.build()).queue();
        }
        if (event.getName().equals("generateinvoice")) {
            if (!event.getMember().hasPermission(Permission.MANAGE_PERMISSIONS)) {
                event.reply(EmojiUtil.CROSS.emoji + " You need `MANAGE_PERMISSIONS` permissions for this!").setEphemeral(true).queue();
                return;
            }
            OptionMapping price = event.getOption("price");

            EmbedBuilder eb = new EmbedBuilder();
            eb.setTitle("Invoice");
            eb.setDescription(EmojiUtil.DOLLAR.emoji + " Thank you for working with Mal's Coding Services. Please send **£" + price.getAsDouble() + "** to **mattd.123@yahoo.com** via PayPal. Think we did good? Tips are greatly appreciated!");
            eb.setColor(Color.decode("#2f3136"));
            event.replyEmbeds(eb.build()).queue();
        }
        if (event.getName().equals("blacklist")) {
            if (!event.getMember().hasPermission(Permission.BAN_MEMBERS)) {
                event.reply(EmojiUtil.CROSS.emoji + " You need `BAN_MEMBERS` permissions for this!").setEphemeral(true).queue();
                return;
            }
            OptionMapping name = event.getOption("user");
            ArrayList<String> blacklisted = new ArrayList<>();
            if (discordBot.botConfig.get("blacklisted") != null) {
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
            event.reply(EmojiUtil.TICK.emoji + " Blacklisted " + name.getAsMember().getAsMention() + " from using tickets!").queue();
        }
        if (event.getName().equals("unblacklist")) {
            if (!event.getMember().hasPermission(Permission.BAN_MEMBERS)) {
                event.reply(EmojiUtil.CROSS.emoji + " You need `BAN_MEMBERS` permissions for this!").setEphemeral(true).queue();
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
            event.reply(EmojiUtil.TICK.emoji + " Unblacklisted " + name.getAsMember().getAsMention() + " from using tickets!").queue();
        }
        if (event.getName().equals("adduser")) {
            if (!isInTickets(event.getChannel().asTextChannel().getParentCategoryIdLong()) && event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
                event.reply(EmojiUtil.CROSS.emoji + " You cannot add users here!").queue();
                return;
            }
            if (event.getChannel().asTextChannel().getParentCategory().getId().equals(category.getId())) {
                OptionMapping name = event.getOption("user");
                event.getChannel().asTextChannel().upsertPermissionOverride(name.getAsMember()).setAllowed(Permission.VIEW_CHANNEL).queue();
                event.reply(EmojiUtil.TICK.emoji + " Added " + name.getAsMember().getAsMention() + " to the ticket!").queue();
            }
        }
        if (event.getName().equals("removeuser")) {
            if (!isInTickets(event.getChannel().asTextChannel().getParentCategoryIdLong()) && event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
                event.reply(EmojiUtil.CROSS.emoji + " You cannot add users here!").queue();
                return;
            }
            if (event.getChannel().asTextChannel().getParentCategory().getId().equals(category.getId())) {
                OptionMapping name = event.getOption("user");
                event.getChannel().asTextChannel().upsertPermissionOverride(name.getAsMember()).setDenied(Permission.VIEW_CHANNEL).queue();
                event.reply(EmojiUtil.TICK.emoji + " Removed " + name.getAsMember().getAsMention() + " from the ticket!").queue();
            }
        }
        if (event.getName().equals("displaytickets")) {
            if (!event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
                event.reply(EmojiUtil.CROSS.emoji + " You need `ADMINISTRATOR` permissions for this!").setEphemeral(true).queue();
                return;
            }
            if (discordBot.botConfig.getSection("panel").getKeys().size() == 0) {
                event.reply(EmojiUtil.CROSS.emoji + " There is no ticket panels!").setEphemeral(true).queue();
            }

            EmbedBuilder eb = new EmbedBuilder();
            eb.setTitle("Open a ticket");
            eb.setDescription("At MineBuddies players are our first priority so if you have any problems please do not hesitate to open a support ticket and a staff member will assist you as soon as possible. Please add as much detail you can on the form.\n" +
                    "\nPlease choose the right topic as you will get assisted quicker.\n" +
                    "\n\uD83D\uDEE1 **General Support**: If you have a question how to do something or any other casual server questions, open this type of support ticket.\n" +
                    "\n\uD83D\uDC64 **Player Report**: Suspect a player is cheating or is someone breaking rules, open this type of support ticket.\n" +
                    "\n\uD83D\uDC1E **Bug Report**: If you have found a server bug then you can create this type of ticket to help us improve the server. You may receive rewards for reporting bugs depending on the severity.\n" +
                    "\n\uD83D\uDD75 **Appeal**: If you think you were wrongly punished or think you deserve a second chance, open this type of support ticket.\n" +
                    "\n\uD83D\uDC40 **Staff Report**: If you think a staff member is abusing, open this type of support ticket. If the \"abusing\" staff member closes your ticket please contact a higher up.\n" +
                    "\n\uD83D\uDEA8 **Purchase Support**: If you believe there is a problem with your purchase and have waited over 15 minutes, open this type of support ticket.\n" +
                    "\nWe will try to reply as fast as possible. Please excessively don't ping staff members.");
            eb.setColor(Color.decode("#2f3136"));

            int count = 1;
            ArrayList<Button> buttons = new ArrayList<>();
            ArrayList<Button> buttons2 = new ArrayList<>();

            for (PanelObject panelObject : discordBot.panelObjects) {
                if (count >= 5) {
                    buttons2.add(panelObject.getButton());
                } else {
                    buttons.add(panelObject.getButton());
                }
                count++;
            }

            event.reply("Done!").queue();
            event.getChannel().sendMessageEmbeds(eb.build()).addActionRow(buttons).addActionRow(buttons2).queue();
        }
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        for (PanelObject panelObject : discordBot.panelObjects) {
            String button = panelObject.nameId + "_button";
            if (button.equals(event.getButton().getId())) {
                if (discordBot.blacklistedUsers.contains(event.getMember().getId())) {
                    event.reply(EmojiUtil.CROSS.emoji + " You are blacklisted from using tickets!").setEphemeral(true).queue();
                    return;
                }
                event.replyModal(panelObject.getPanelModal()).queue();
                return;
            }
        }
        if (event.getButton().getId().startsWith("close_")) {
            String channelId = event.getButton().getId().replace("close_", "");
            TextChannel textChannel = discordBot.guild.getTextChannelById(channelId);

            EmbedBuilder closeConfirm = new EmbedBuilder();
            closeConfirm.setColor(Color.decode("#2f3136"));
            closeConfirm.setTitle("Close Confirmination");
            closeConfirm.setDescription("Please confirm that you want to close this ticket");
            Button button = Button.danger("confirm_" + textChannel.getId(), "Close Ticket").withEmoji(Emoji.fromFormatted("✔"));
            event.replyEmbeds(closeConfirm.build()).addActionRow(button).queue();
            return;
        }
        if (event.getButton().getId().startsWith("confirm_")) {
            String channelId = event.getButton().getId().replace("confirm_", "");
            TextChannel textChannel = discordBot.guild.getTextChannelById(channelId);
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
        if (event.getButton().getId().startsWith("reopen_")) {
            String channelId = event.getButton().getId().replace("reopen_", "");
            TextChannel textChannel = discordBot.guild.getTextChannelById(channelId);
            textChannel.getManager().setParent(category).queue();

            EmbedBuilder opened = new EmbedBuilder();

            opened.setTitle("Ticket Re Opened!");
            opened.setColor(Color.decode("#2f3136"));
            opened.setDescription(event.getMember().getAsMention() + " Reopened the ticket");
            event.replyEmbeds(opened.build()).queue();
            return;
        }
        if (event.getButton().getId().startsWith("delete_")) {
            String channelId = event.getButton().getId().replace("delete_", "");
            TextChannel textChannel = discordBot.guild.getTextChannelById(channelId);

            event.reply("This channel will be deleted in 5 seconds.").queue();

            textChannel.delete().queueAfter(5, TimeUnit.SECONDS);
        }
    }

    @Override
    public void onModalInteraction(ModalInteractionEvent event) {
        for (PanelObject panelObject : discordBot.panelObjects) {
            String modalId = panelObject.nameId + "_modal";
            if (modalId.equals(event.getModalId())) {
                TextChannel textChannel = category.createTextChannel(discordBot.ticketPrefix + "-" + event.getMember().getEffectiveName()).complete();
                textChannel.getManager().putPermissionOverride(event.getMember(), EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND), null).queue();
                textChannel.upsertPermissionOverride(event.getGuild().getPublicRole()).setDenied(Permission.VIEW_CHANNEL).queue();
                textChannel.upsertPermissionOverride(event.getMember()).setAllowed(Permission.VIEW_CHANNEL).queue();
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

        for (ModalMapping modalMapping : modalMappings) {
            String result = "";
            String title = modalMapping.getId().replaceAll("()([A-Z])", "$1 $2");
            String[] words = title.split(" ");
            for (String word : words) {
                if (word.length() > 0)
                    result += Character.toUpperCase(word.charAt(0)) + word.substring(1) + " ";
            }
            System.out.println(result);
            questions.addField(result.replace("*", ""), modalMapping.getAsString(), false);
        }

        Button button = Button.danger("close_" + textChannel.getId(), "Close Ticket").withEmoji(Emoji.fromFormatted("\uD83D\uDD12"));

        textChannel.sendMessageEmbeds(eb.build(), questions.build()).addActionRow(button).queue();
        textChannel.sendMessage(supportRolePing).queue();
    }

    private boolean isInTickets(long catId) {
        return discordBot.botConfig.getInt("opened_category") == catId;
    }

    @Override
    public void onReady(ReadyEvent e) {
        discordBot.guild = jda.getGuildById(discordBot.botConfig.getString("guild"));
        discordBot.supportRole = discordBot.guild.getRoleById("1031964996583821383");
        category = discordBot.guild.getCategoryById(discordBot.botConfig.getString("opened_category"));
        closedCategory = discordBot.guild.getCategoryById(discordBot.botConfig.getString("closed_category"));
        logsChannel = discordBot.guild.getTextChannelById(discordBot.botConfig.getString("logs_channel"));


        if (discordBot.guild != null) {
            discordBot.guild.upsertCommand("createpanel", "Add answer")
                    .addOption(OptionType.STRING, "name", "The name of panel", true)
                    .addOption(OptionType.STRING, "emoji", "Emoji for button", true)
                    .addOption(OptionType.STRING, "questions", "Questions", true)
                    .queue();

            if (!discordBot.ticketPrefix.toLowerCase().equals("ticket")) {
                discordBot.guild.upsertCommand("claim", "Claim a order")
                        .queue();
                discordBot.guild.upsertCommand("generateinvoice", "Create a invoice")
                        .addOption(OptionType.NUMBER, "price", "The price for the invoice", true)
                        .queue();
            }

            discordBot.guild.upsertCommand("displaytickets", "Create the embed for tickets")
                    .queue();

            discordBot.guild.upsertCommand("adduser", "Add someone to ticket")
                    .addOption(OptionType.MENTIONABLE, "user", "The user to add", true)
                    .queue();

            discordBot.guild.upsertCommand("removeuser", "Remove someone to ticket")
                    .addOption(OptionType.MENTIONABLE, "user", "The user to remove", true)
                    .queue();

            discordBot.guild.upsertCommand("blacklist", "Blacklist user from opening tickets")
                    .addOption(OptionType.MENTIONABLE, "user", "The user to blacklist", true)
                    .queue();

            discordBot.guild.upsertCommand("unblacklist", "Unblacklist user from opening tickets")
                    .addOption(OptionType.MENTIONABLE, "user", "The user to unblacklist", true)
                    .queue();

            discordBot.guild.upsertCommand("stealemoji", "Copy an emoji from another server and save it to this server")
                    .addOption(OptionType.STRING, "emoji", "Emoji to clone", true)
                    .queue();

            System.out.println("Registered commands for guild " + discordBot.guild.getName() + ".");
        } else {
            System.out.println("Guild is null make sure the ID is correct");
        }
        logsChannel.sendMessageEmbeds(new EmbedHelper().setTitle("Bot has started").setDescription(EmojiUtil.TICK.emoji + " Bot has started!").setColor(Color.decode("#6ebce3")).setTimestamp(Instant.now()).build()).queue();
    }
}

package lol.maltest.minebuddiesbot.util;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;
import java.time.Instant;
import java.time.temporal.TemporalAccessor;

/**
 * @author maltest - Mal#4786
 *
 * DiscordJDA Embed helper
 */
public class EmbedHelper {

    private EmbedBuilder eb;

    public EmbedHelper() {
        eb = new EmbedBuilder();
    }

    /**
     * Set the title of the embed
     *
     * @param title The title of the embed
     * @return      Add data to the current EmbedBuilder
     */
    public EmbedHelper setTitle(String title) {
        eb.setTitle(title);
        return this;
    }

    /**
     * Set the title of the embed with a clickable link
     *
     * @param title     The title of the embed
     * @param titleLink The title's clickable link
     * @return          Add data to the current EmbedBuilder
     */
    public EmbedHelper setTitle(String title, String titleLink) {
        eb.setTitle(title);
        return this;
    }

    /**
     * Set the description of the embed
     *
     * @param description The description of the embed
     * @return            Add data to the current EmbedBuilder
     */
    public EmbedHelper setDescription(String description) {
        eb.setDescription(description);
        return this;
    }

    /**
     * Add a Field to the embed. Inline defaults to false
     *
     * @param title       The title of the field
     * @param description The description of the field
     * @return            Add data to the current EmbedBuilder
     */
    public EmbedHelper addField(String title, String description) {
        eb.addField(title, description, false);
        return this;
    }

    /**
     * Add a Field to the embed.
     *
     * @param title       The title of the field
     * @param description The description of the field
     * @param isInline    Is the field inline with other fields or not
     * @return            Add data to the current EmbedBuilder
     */
    public EmbedHelper addField(String title, String description, boolean isInline) {
        eb.addField(title, description, isInline);
        return this;
    }

    /**
     * Set the color of the embed.
     *
     * @param color Color to set the embed.
     * @return      Add data to the current EmbedBuilder
     */
    public EmbedHelper setColor(Color color) {
        eb.setColor(color);
        return this;
    }

    /**
     * Set the author of the embed
     *
     * @param author The author's name/username
     * @return       Add data to the current EmbedBuilder
     */
    public EmbedHelper setAuthor(String author) {
        eb.setAuthor(author);
        return this;
    }

    /**
     * Set the author of the embed
     *
     * @param author The author's name/username
     * @param image  The author's profile picture
     * @return       Add data to the current EmbedBuilder
     */
    public EmbedHelper setAuthor(String author, String image) {
        eb.setAuthor(author, image);
        return this;
    }

    /**
     * Set the timestamp of the embed
     * Hint: You can use {@link Instant#now()} to quickly get current time
     *
     * @param timestamp The time to be set on the embed
     * @return          Add data to the current EmbedBuilder
     */
    public EmbedHelper setTimestamp(TemporalAccessor timestamp) {
        eb.setTimestamp(timestamp);
        return this;
    }

    /**
     * Set the footer of an embed
     *
     * @param footer The footer text to be added
     * @return       Add data to the current EmbedBuilder
     */
    public EmbedHelper setFooter(String footer) {
        eb.setFooter(footer);
        return this;
    }

    /**
     * Set the footer of an embed with an icon
     *
     * @param footer  The footer text to be added
     * @param iconUrl The URL of the footer image
     * @return        Add data to the current EmbedBuilder
     */
    public EmbedHelper setFooter(String footer, String iconUrl) {
        eb.setFooter(footer, iconUrl);
        return this;
    }

    /**
     * Set the thumbnail picture of an embed
     *
     * @param imageUrl The Image URL to be set
     * @return         Add data to the current EmbedBuilder
     */
    public EmbedHelper setThumbnail(String imageUrl) {
        eb.setThumbnail(imageUrl);
        return this;
    }

    /**
     * Set the Image picture of an embed
     *
     * @param imageUrl The Image URL to be set
     * @return         Add data to the current EmbedBuilder
     */
    public EmbedHelper setImage(String imageUrl) {
        eb.setImage(imageUrl);
        return this;
    }

    /**
     * Get the current EmbedBuilder
     * Just encase I missed a method!
     *
     * @return The embed as a EmbedBuilder
     */
    public EmbedBuilder getEmbedBuilder() {
        return eb;
    }

    /**
     * Build the embed into a MessageEmbed
     *
     * @return The finalised embed
     */
    public MessageEmbed build() {
        return eb.build();
    }
}

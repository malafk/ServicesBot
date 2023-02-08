package lol.maltest.servicesbot.util;

public enum EmojiUtil {

    TICK("<:MCS_tick:1034922333200195654>"),
    DOLLAR("<:MCS_dollar:1034922323297452073>"),
    BARRIER("<:MCS_barrier:1034922320982192238>"),
    CROSS("<:MCS_cross:1034922322215305246>"),
    FLAME("<:MCS_flame:1034922324731904072>"),
    HEART("<:MCS_heart:1034922326019551272>"),
    HOURGLASS("<:MCS_hourglass:1034922327370105042>"),
    STAR("<:MCS_star:1034922328968155267>"),
    THUMBSDOWN("<:MCS_thumbsDown:1034922330268389457>"),
    THUMBSUP("<:MCS_thumbsUp:1034922331878998176>");


    public final String emoji;

    EmojiUtil(String emojiName) {
        this.emoji = emojiName;
    }

}

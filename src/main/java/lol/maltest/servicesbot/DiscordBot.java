package lol.maltest.servicesbot;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import lol.maltest.servicesbot.impl.PanelObject;
import lol.maltest.servicesbot.listeners.CommandListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class DiscordBot {

    public JDA jda;
    public Guild guild;
    public Role supportRole;
    public YamlDocument botConfig;
    public ArrayList<String> blacklistedUsers = new ArrayList<>();
    public ArrayList<PanelObject> panelObjects = new ArrayList<>();

    public String ticketPrefix;

    public DiscordBot() {

        try {
            botConfig = YamlDocument.create(new File("data", "config.yml"), getClass().getClassLoader().getResource("config.yml").openStream(),
                    GeneralSettings.DEFAULT, LoaderSettings.builder().setAutoUpdate(true).build(), DumperSettings.DEFAULT, UpdaterSettings.builder().setVersioning(new BasicVersioning("file-version")).build());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        ticketPrefix = botConfig.getString("prefix");

        jda = JDABuilder.createLight(botConfig.getString("token"), GatewayIntent.GUILD_MESSAGES, GatewayIntent.DIRECT_MESSAGES, GatewayIntent.MESSAGE_CONTENT).build();

        jda.addEventListener(new CommandListener(this, jda));

        if(botConfig.getSection("panel") != null) {
            botConfig.getSection("panel").getKeys().forEach(key -> {
                String emoji = botConfig.getString("panel." + key + ".emoji");
                panelObjects.add(new PanelObject(key.toString(), emoji, (ArrayList<String>) botConfig.getStringList("panel." + key + ".questions")));
            });
        }
        if(botConfig.getSection("blacklisted") != null) {
            blacklistedUsers = (ArrayList<String>) botConfig.get("blacklisted");
        }
        System.out.println("Cached " + panelObjects.size() + " panels");
        System.out.println("Blacklisted " + blacklistedUsers.size() + " users");
    }

}

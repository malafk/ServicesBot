package lol.maltest.minebuddiesbot.impl;

import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class PanelObject {

    public final String name;
    public final String nameId;
    public final String emoji;
    public final ArrayList<String> questions;

    public PanelObject(String name, String emoji, ArrayList<String> questions) {
        this.name = name;
        this.emoji =emoji;
        this.questions = questions;
        this.nameId = name.replace(" ", "").replace("*", "").toLowerCase();
    }

    public Modal getPanelModal() {
        ArrayList<ActionRow> actionRows = new ArrayList<>();

        for(String question : questions) {
            boolean required = question.endsWith("*");
            TextInput body = TextInput.create(question.replace(" ", ""), question.replace("*", ""), TextInputStyle.SHORT)
                    .setMaxLength(1000)
                    .setRequired(required)
                    .build();

            actionRows.add(ActionRow.of(body));
        }

        return Modal.create(nameId + "_modal", name)
                .addActionRows(actionRows)
                .build();
    }

    public Button getButton() {
        return Button.secondary(nameId + "_button", name).withEmoji(Emoji.fromFormatted(emoji));
    }
}

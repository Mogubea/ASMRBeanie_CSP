package me.mogubea.listeners;

import io.papermc.paper.chat.ChatRenderer;
import me.mogubea.profile.MoguProfile;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class MoguChatRenderer implements ChatRenderer {

    @Override
    public @NotNull Component render(@NotNull Player player, @NotNull Component playerName, @NotNull Component message, @NotNull Audience audience) {
        MoguProfile profile = MoguProfile.from(player);
        Component component = Component.empty();
        if (profile.hasJob())
            component = component.append(profile.getJob().getIcon().append(Component.text(" "))).clickEvent(ClickEvent.clickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/setjob " + profile.getJob().getLowerName()));

        return component
                .append(profile.getColouredName().clickEvent(ClickEvent.clickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tell " + profile.getDisplayName())))
                .append(Component.text(" » ", TextColor.color(0x59556A)))
                .append(message.color(TextColor.color(0xefefef)));
    }

}

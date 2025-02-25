package me.mogubea.listeners;

import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import me.mogubea.main.Main;
import me.mogubea.profile.MoguProfile;
import me.mogubea.statistics.SimpleStatType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ChatListener extends EventListener {

    private final ChatRenderer chatRenderer;

    protected ChatListener(@NotNull ListenerManager manager) {
        super(manager);
        chatRenderer = new MoguChatRenderer();
    }

    @EventHandler(ignoreCancelled = true)
    public void onChat(AsyncChatEvent e) {
        e.renderer(chatRenderer);

        String filteredMessage = plugin.getWordFilter().filter(((TextComponent)e.message()).content());
        Component newMessage = ((TextComponent) e.message()).content(filteredMessage);

        if (filteredMessage.contains("@")) {
            newMessage = Component.text("");
            final Set<Player> pinged = new HashSet<>();

            // @Name and Emotes
            final String[] spaceSplit = filteredMessage.split(" ");
            for (String word : spaceSplit) {
                if (word.startsWith("@")) { // Handle looking for pings within the message.
                    Player ping = plugin.searchForPlayer(word.substring(1));
                    if (ping != null) {
                        MoguProfile prof = MoguProfile.from(ping);
                        if (pinged.add(ping))
                            ping.playSound(ping.getLocation(), Sound.BLOCK_AMETHYST_CLUSTER_HIT, 0.3F, 0.7F);
                        newMessage = newMessage.append(Component.text("@").color(prof.getNameColour()).append(prof.getColouredName()).append(Component.text(" ")));
                        continue;
                    }
                }

                newMessage = newMessage.append(Component.text(word + " "));
            }
        }

        e.message(newMessage);
        addToStat(e.getPlayer(), SimpleStatType.GENERIC, "chat", 1);
    }

}

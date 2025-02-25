/*
 * Copyright (C) 2011-2020 lishid. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package me.mogubea.commands;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.*;
import java.util.function.Function;

/**
 * Utility class for common tab completions.
 */
public class TabCompleter {

    private final List<Material> validMaterials;

    protected TabCompleter() {
        validMaterials = new ArrayList<>();
        for (Material enumConstant : Material.values()) {
            if (enumConstant.isLegacy()) continue;
            if (!enumConstant.isItem()) continue;
            if (enumConstant.isEmpty()) continue;
            validMaterials.add(enumConstant);
        }
    }

    /**
     * Offer tab completions for whole numbers.
     *
     * @param argument the argument to complete
     * @return integer options
     */
    public List<String> completeInteger(String argument) {
        // Ensure existing argument is actually a number
        if (!argument.isEmpty()) {
            try {
                Integer.parseInt(argument);
            } catch (NumberFormatException e) {
                return Collections.emptyList();
            }
        }

        List<String> completions = new ArrayList<>(10);
        for (int i = -1; ++i < 10;)
            completions.add(argument + i);

        return completions;
    }
    
    public List<String> completeIntegerBetween(String argument, int min, int max) {
        // Ensure existing argument is actually a number
        if (!argument.isEmpty()) {
            try {
                Integer.parseInt(argument);
            } catch (NumberFormatException e) {
                return Collections.emptyList();
            }
        }

        List<String> completions = new ArrayList<>(10);
        for (int i = min; i < (max+1); ++i) {
            completions.add("" + i);
        }

        return completions;
    }

    public List<String> completeFloat(String argument) {
        // Ensure existing argument is actually a number
        if (!argument.isEmpty()) {
            try {
                Float.parseFloat(argument);
            } catch (NumberFormatException e) {
                return Collections.emptyList();
            }
        }

        List<String> completions = new ArrayList<>(10);
        for (int i = 0; i < 10; ++i) {
            completions.add(argument + i);
        }

        return completions;
    }
    
    /**
     * Offer tab completions for a given Enum.
     *
     * @param argument the argument to complete
     * @param enumClazz the Enum to complete for
     * @return the matching Enum values
     */
    public List<String> completeEnum(String argument, Class<? extends Enum<?>> enumClazz) {
        argument = argument.toLowerCase(Locale.ENGLISH);
        List<String> completions = new ArrayList<>();

        for (Enum<?> enumConstant : enumClazz.getEnumConstants()) {
            String name = enumConstant.name().toLowerCase();
            if (name.contains(argument)) {
                completions.add(name);
            }
        }

        return completions;
    }

    /**
     * Offer tab completions for a given array of Strings.
     *
     * @param argument the argument to complete
     * @param options the Strings which may be completed
     * @return the matching Strings
     */
    public List<String> completeString(String argument, String[] options) {
        argument = argument.toLowerCase(Locale.ENGLISH);
        List<String> completions = new ArrayList<>();

        for (String option : options) {
            if (option.startsWith(argument)) {
                completions.add(option);
            }
        }

        return completions;
    }
    
    public List<String> completeString(String argument, Collection<String> options) {
        argument = argument.toLowerCase(Locale.ENGLISH);
        List<String> completions = new ArrayList<>();

        for (String option : options) {
            if (option.startsWith(argument)) {
                completions.add(option);
            }
        }

        return completions;
    }

    /**
     * Offer tab completions for visible online Players' names.
     *
     * @param sender the command's sender
     * @param argument the argument to complete
     * @return the matching Players' names
     */
    public List<String> completeOnlinePlayer(CommandSender sender, String argument) {
        List<String> completions = new ArrayList<>();
        Player senderPlayer = sender instanceof Player ? (Player) sender : null;

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (senderPlayer != null && !senderPlayer.canSee(player)) {
                continue;
            }

            if (StringUtil.startsWithIgnoreCase(player.getName(), argument)) {
                completions.add(player.getName());
            }
        }

        return completions;
    }

    /**
     * Offer tab completions for a given array of Objects.
     *
     * @param argument the argument to complete
     * @param converter the Function for converting the Object into a comparable String
     * @param options the Objects which may be completed
     * @return the matching Strings
     */
    public <T> List<String> completeObject(String argument, Function<T, String> converter, T[] options) {
        argument = argument.toLowerCase(Locale.ENGLISH);
        List<String> completions = new ArrayList<>();

        for (T option : options) {
            String optionString = converter.apply(option).toLowerCase();
            if (optionString.contains(argument)) {
                completions.add(optionString);
            }
        }

        return completions;
    }
    
    public <T> List<String> completeObject(String argument, Function<T, String> converter, Collection<T> options) {
        argument = argument.toLowerCase(Locale.ENGLISH);
        List<String> completions = new ArrayList<>();

        for (T option : options) {
            String optionString = converter.apply(option).toLowerCase();
            if (optionString.contains(argument)) {
                completions.add(optionString);
            }
        }

        return completions;
    }

    /**
     * Ignores all LEGACY materials and invalid items like lava_cauldron or water
     */
    public List<String> completeItems(String argument) {
        argument = argument.toLowerCase(Locale.ENGLISH);

        List<String> completions = new ArrayList<>();

        for (int x = -1; ++x < validMaterials.size();) {
            String name = validMaterials.get(x).name().toLowerCase();
            if (name.contains(argument))
                completions.add(name);
        }

        return completions;
    }

}
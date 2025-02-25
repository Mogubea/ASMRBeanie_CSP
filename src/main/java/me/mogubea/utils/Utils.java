package me.mogubea.utils;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.TreeMap;
import java.util.UUID;

public class Utils {

    private final static TreeMap<Integer, String> map = new TreeMap<>();

    static {

        map.put(1000, "M");
        map.put(900, "CM");
        map.put(500, "D");
        map.put(400, "CD");
        map.put(100, "C");
        map.put(90, "XC");
        map.put(50, "L");
        map.put(40, "XL");
        map.put(10, "X");
        map.put(9, "IX");
        map.put(5, "V");
        map.put(4, "IV");
        map.put(1, "I");

    }

    public static String toRoman(int number) {
        int l =  map.floorKey(number);
        if ( number == l ) {
            return map.get(number);
        }
        return map.get(l) + toRoman(number-l);
    }

    public static TextComponent getProgressBar(char symbol, int amount, long value, long maxvalue, int baseColor, int filledColor) {
        TextComponent component = Component.empty();
        int todo = (int)(((float)value/(float)maxvalue) * (float)amount);
        
        for (int c = -1; ++c < 2;) {
            StringBuilder sb = new StringBuilder();

            for (int x = -1; ++x < todo;)
                sb.append(symbol);
            component = component.append(Component.text(sb.toString(), TextColor.color(c == 0 ? filledColor : baseColor)));
            todo = amount - todo;
        }

        return component.decoration(TextDecoration.ITALIC, false);
    }

    public static String toBase64(Object obj) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

            dataOutput.writeObject(obj);
            dataOutput.close();

            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("Unable to compress object.", e);
        }
    }

    public static Object fromBase64(String base64) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(base64));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            Object item = dataInput.readObject();

            dataInput.close();
            return item;
        } catch (Exception e) {
            throw new IllegalStateException("Unable to decompress object.", e);
        }
    }


    public static ItemStack itemStackFromBase64(String base64) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(base64));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            ItemStack item = (ItemStack) dataInput.readObject();

            dataInput.close();
            return item;
        } catch (Exception e) {
            throw new IllegalStateException("Unable to decompress itemstack.", e);
        }
    }

    public static String itemStackArrayToBase64(ItemStack[] items) throws IllegalStateException {
        if (items == null) return null;

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

            // Write the size of the inventory
            dataOutput.writeInt(items.length);

            // Save every element in the list
            for (int i = 0; i < items.length; i++) {
                dataOutput.writeObject(items[i]);
            }

            // Serialize that array
            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("Unable to save item stacks.", e);
        }
    }

    public static ItemStack[] itemStackArrayFromBase64(String data) {
        if (data == null) return null;

        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            ItemStack[] items = new ItemStack[dataInput.readInt()];

            // Read the serialized inventory
            for (int i = 0; i < items.length; i++) {
                items[i] = (ItemStack) dataInput.readObject();
            }

            dataInput.close();
            return items;
        } catch (Exception e) {
            throw new IllegalStateException("Unable to decode class type.", e);
        }
    }

    public static ItemStack getSkullWithCustomSkin(String base64) {
        ItemStack i = new ItemStack(Material.PLAYER_HEAD,1);
        SkullMeta meta = (SkullMeta) i.getItemMeta();
        PlayerProfile ack = Bukkit.createProfile(UUID.randomUUID());
        ack.getProperties().add(new ProfileProperty("textures", base64));
        meta.setPlayerProfile(ack);
        i.setItemMeta(meta);
        return i;
    }

    /**
     * For a skull that'll likely be used as an itemstack, using GameProfile's rather than PlayerProfile's for valid consistency.
     */
    public static ItemStack getSkullWithCustomSkin(UUID uuid, String base64) {
        ItemStack i = new ItemStack(Material.PLAYER_HEAD,1);
        SkullMeta meta = (SkullMeta) i.getItemMeta();
        PlayerProfile ack = Bukkit.createProfile(uuid, null);
        ack.getProperties().add(new ProfileProperty("textures", base64));
        meta.setPlayerProfile(ack);
        i.setItemMeta(meta);
        return i;
    }

    public static String timeStringFromNow(long timeInMillis) {
        long cur = System.currentTimeMillis();
        long secs = (timeInMillis > cur ? timeInMillis - cur : cur - timeInMillis) / 1000;
        long mins = secs / 60;
        secs -= mins*60;
        long hours = mins / 60;
        mins -= hours*60;
        long days = hours / 24;
        hours -= days*24;
        long weeks = days / 7;
        days -= weeks*7;

        if (weeks > 0)
            return weeks + (weeks > 1 ? " Weeks" : " Week") + (days > 0 ? " and " + days + (days > 1 ? " Days" : " Day") : "");
        if (days > 0)
            return days + (days > 1 ? " Days" : " Day") + (hours > 0 ? " and " + hours + (hours > 1 ? " Hours" : " Hour") : "");
        if (hours > 0)
            return hours + (hours > 1 ? " Hours" : " Hour") + (mins > 0 ? " and " + mins + (mins > 1 ? " Minutes" : " Minute") : "");
        if (mins > 0)
            return mins + (mins > 1 ? " Minutes" : " Minute") + (secs > 0 ? " and " + secs + (secs > 1 ? " Seconds" : " Second") : "");

        return secs + (secs > 1 ? " Seconds" : " Second");
    }

    public static String timeStringFromMillis(long timeInMillis) {
        long cur = System.currentTimeMillis();
        return timeStringFromNow(timeInMillis + cur);
    }

}

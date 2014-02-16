package me.offluffy.populationdensity.utils;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Messages {
    /**
     * Fetches a no permission message
     * Use Message.NO_PERM to not display the permission
     *
     * @param perm The permission to display in the message
     * @return The String message
     */
    public static String noPerm(String perm) {
        return (Clr.ERR + String.format("You lack the required permission: %s", perm));
    }

    /**
     * Sends a message to a player from the Message list
     *
     * @param sender The CommandSender to send the message
     * @param msg    The Message enum to send
     * @see Message
     */
    public static void send(CommandSender sender, Message msg) {
        sender.sendMessage(msg.msg());
    }

    /**
     * Sends a message to a player from the Message list
     *
     * @param player The Player to send the message
     * @param msg    The Message enum to send
     * @see Message
     */
    public static void send(Player player, Message msg) {
        player.sendMessage(msg.msg());
    }

    public static enum Clr {
        TITLE('6', 'l'), HEAD('a'), NORM('3'), NOTE('7', 'o'), ERR('c'), WARN('6'), PRE('7');
        private String c;

        Clr(char... colorChar) {
            c = "";
            for (char ch : colorChar) c += "\u00A7" + ch;
        }

        @Override
        public String toString() { return c; }

        public String cc(char... colors) {
            String s = "";
            for (char ch : colors)
                s += "\u00A7" + ch;
            return s;
        }

        public String cc(String colors) {
            return cc(colors.toCharArray());
        }
    }

    public static enum Message {
        NO_PERM(Clr.ERR + "You do not have permission to do that"),
        NO_WORLD(Clr.ERR + "PopulationDensity has not been properly configured. Please update your config.yml and specify a world to manage."),
        NOT_ONLINE(Clr.ERR + "You must be in-game to use that"),
        NO_REGION(Clr.ERR + "You're not in a region!"),
        NO_CITY(Clr.ERR + "There is no City world to teleport to");
        private String msg;

        Message(String msg) {
            this.msg = msg;
        }

        public String msg() {
            return msg;
        }
    }
}

package me.guichaguri.tickratechanger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import me.guichaguri.tickratechanger.api.TickrateAPI;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.event.HoverEvent;
import net.minecraft.event.HoverEvent.Action;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.config.Configuration;

/**
 * @author Guilherme Chaguri
 */
public class TickrateCommand extends CommandBase {
    private List<String> aliases;
    public TickrateCommand() {
        aliases = Arrays.asList("ticks", "tickratechanger", "trc", "settickrate");
    }

    @Override
    public String getName() {
        return "tickrate";
    }
    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/tickrate [ticks per second] [all/server/client/playername]";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 4;
    }
    @Override
    public List getAliases() {
        return aliases;
    }
    @Override
    public List addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        if(args.length < 1) {
            return null;
        }
        List<String> tab = new ArrayList<String>();
        if(args.length == 1) {
            tab.add("20");
            tab.add("2");
            tab.add("5");
            tab.add("10");
            tab.add("15");
            tab.add("25");
            tab.add("35");
            tab.add("50");
            tab.add("100");
            float defaultTickrate = TickrateChanger.DEFAULT_TICKRATE;
            String defTickrate = defaultTickrate + "";
            if(defaultTickrate == (int)defaultTickrate) defTickrate = (int)defaultTickrate + "";
            if(!tab.contains(defTickrate)) {
                tab.add(0, defTickrate);
            }
            tab.add("setdefault");
        } else if(args.length == 2) {
            if(args[0].equalsIgnoreCase("setdefault")) {
                tab.add("20");
                tab.add("2");
                tab.add("5");
                tab.add("10");
                tab.add("15");
                tab.add("25");
                tab.add("35");
                tab.add("50");
                tab.add("100");
                float defaultTickrate = TickrateChanger.DEFAULT_TICKRATE;
                String defTickrate = defaultTickrate + "";
                if(defaultTickrate == (int)defaultTickrate) defTickrate = (int)defaultTickrate + "";
                if(!tab.contains(defTickrate)) {
                    tab.add(0, defTickrate);
                }
            } else {
                tab.add("all");
                tab.add("server");
                tab.add("client");
                for(EntityPlayer p : (List<EntityPlayer>) MinecraftServer.getServer().getConfigurationManager().playerEntityList) {
                    tab.add(p.getName());
                }
            }
        } else if(((args.length == 3) || (args.length == 4)) && (args[0].equalsIgnoreCase("setdefault"))) {
            tab.add("--dontsave");
            tab.add("--dontupdate");
        }
        return tab;
    }

    @Override
    public void execute(ICommandSender sender, String[] args) throws CommandException {
        if(args.length < 1) {
            sender.addChatMessage(new ChatComponentTranslation("tickratechanger.show.clientside"));
            chat(sender, c("Current Server Tickrate: ", 'f', 'l'), c((1000F / TickrateChanger.MILISECONDS_PER_TICK) + " ticks per second", 'a'));
            chat(sender, c("Default Tickrate: ", 'f', 'l'), c(TickrateChanger.DEFAULT_TICKRATE + " ticks per second", 'e'));
            chat(sender, c("/tickrate <ticks per second> [all/server/client/", 'b'), c("playername", 'b', 'o'), c("]", 'b'));
            chat(sender, c("/tickrate setdefault <ticks per second> [--dontsave, --dontupdate]", 'b'));
            chat(sender);
            chat(sender, c("Use ", 'c'), c("/tickrate help", 'c', 'n'), c(" for more command info", 'c'));
            chat(sender);
            return;
        }
        if(args[0].equalsIgnoreCase("help")) {
            chat(sender, c(" * * Tickrate Changer * * ", '5', 'l'), c("by ", '7', 'o'), c("Guichaguri", 'f', 'o'));
            chat(sender, c("Mouse over the command to see what it does", 'f', 'l'));
            chat(sender, c("/tickrate 20 ", new ChatComponentText[]{c("Sets the ", 'a'), c("server & client", 'f'), c(" tickrate to 20", 'a')}, '7'));
            chat(sender, c("/tickrate 20 server ", new ChatComponentText[]{c("Sets the ", 'a'), c("server", 'f'), c(" tickrate to 20", 'a')}, '7'));
            chat(sender, c("/tickrate 20 client ", new ChatComponentText[]{c("Sets ", 'a'), c("all clients", 'f'), c(" tickrate to 20", 'a')}, '7'));
            chat(sender, c("/tickrate 20 Notch ", new ChatComponentText[]{c("Sets the ", 'a'), c("Notch's client", 'f'), c(" tickrate to 20", 'a')}, '7'));
            chat(sender, c("/tickrate setdefault 20 ", new ChatComponentText[]{c("Sets the ", 'a'), c("default", 'f'), c(" tickrate to 20", 'a')}, '7'));
            chat(sender, c("/tickrate setdefault 20 --dontsave ", new ChatComponentText[]{c("Sets the ", 'a'), c("default", 'f'),
                                                                                c(" tickrate to 20 without saving in the config", 'a')}, '7'));
            chat(sender, c("/tickrate setdefault 20 --dontupdate ", new ChatComponentText[]{c("Sets the ", 'a'), c("default", 'f'),
                                                                                c(" tickrate to 20 without updating players", 'a')}, '7'));
            chat(sender, c("/tickrate setdefault 20 --dontsave --dontupdate", new ChatComponentText[]{c("Sets the ", 'a'), c("default", 'f'),
                                                                                c(" tickrate to 20 without saving and updating anything", 'a')}, '7'));
            chat(sender, c(" * * * * * * * * * * * * * * ", '5', 'l'));
            return;
        } else if((args[0].equalsIgnoreCase("setdefault")) && (args.length > 1)) {
            boolean save = true, update = true;
            for(String s : args) {
                if(s.equalsIgnoreCase("--dontsave")) save = false;
                if(s.equalsIgnoreCase("--dontupdate")) update = false;
            }
            float ticksPerSecond;
            try {
                ticksPerSecond = Float.parseFloat(args[1]);
            } catch(Exception ex) {
                chat(sender, c("Something went wrong!", '4'));
                chat(sender, c("/tickrate setdefault <ticks per second> [--dontsave, --dontupdate]", 'c'));
                return;
            }
            TickrateChanger.DEFAULT_TICKRATE = ticksPerSecond;
            if(save) {
                Configuration cfg = new Configuration(TickrateChanger.CONFIG_FILE);
                cfg.get("default", "tickrate", 20.0, "Default tickrate. The game will always initialize with this value.").set(ticksPerSecond);
                cfg.save();
            }
            if(update) {
                TickrateAPI.changeTickrate(ticksPerSecond);
            }
            chat(sender, c("Default tickrate successfully changed to", 'a'), c(" " + ticksPerSecond, 'f'), c(".", 'a'));
            return;
        }

        float ticksPerSecond;
        try {
            ticksPerSecond = Float.parseFloat(args[0]);
        } catch(Exception ex) {
            chat(sender, c("Something went wrong!", '4'));
            chat(sender, c("/tickrate <ticks per second> [all/server/client/", 'c'), c("playername", 'c', 'o'), c("]", 'c'));
            return;
        }

        if(!TickrateAPI.isValidTickrate(ticksPerSecond)) {
            chat(sender, c("Invalid tickrate value!", 'c'), c(" (Must be tickrate > 0)", '7'));
            return;
        }

        if((args.length < 2) || (args[1].equalsIgnoreCase("all"))) {
            TickrateAPI.changeTickrate(ticksPerSecond);
            chat(sender, c("Tickrate successfully changed to", 'a'), c(" " + ticksPerSecond, 'f'), c(".", 'a'));
        } else if(args[1].equalsIgnoreCase("client")) {
            TickrateAPI.changeClientTickrate(ticksPerSecond);
            chat(sender, c("All connected players client tickrate successfully changed to", 'a'), c(" " + ticksPerSecond, 'f'), c(".", 'a'));
        } else if(args[1].equalsIgnoreCase("server")) {
            TickrateAPI.changeServerTickrate(ticksPerSecond);
            chat(sender, c("Server tickrate successfully changed to", 'a'), c(" " + ticksPerSecond, 'f'), c(".", 'a'));
        } else {
            EntityPlayer p = MinecraftServer.getServer().getConfigurationManager().getPlayerByUsername(args[1]);
            if(p == null) {
                chat(sender, c("Player not found", 'c'));
                return;
            }
            TickrateAPI.changeClientTickrate(p, ticksPerSecond);
            chat(sender, c(p.getName() + "'s client tickrate successfully changed to", 'a'), c(" " + ticksPerSecond, 'f'), c(".", 'a'));
        }
    }

    public static void chat(ICommandSender sender, ChatComponentText ... comps) {
        ChatComponentText top;
        if(comps.length == 1) {
            top = comps[0];
        } else {
            top = new ChatComponentText("");
            for(ChatComponentText c : comps) {
                top.appendSibling(c);
            }
        }
        sender.addChatMessage(top);
    }

    public static ChatComponentText c(String s, ChatComponentText[] hover, char ... chars) {
        ChatComponentText c = c(s, chars);
        ChatComponentText hoverComp;
        if(hover.length == 1) {
            hoverComp = hover[0];
        } else {
            hoverComp = new ChatComponentText("");
            for(ChatComponentText txt : hover) {
                hoverComp.appendSibling(txt);
            }
        }
        c.setChatStyle(c.getChatStyle().setChatHoverEvent(new HoverEvent(Action.SHOW_TEXT, hoverComp)));
        return c;
    }
    public static ChatComponentText c(String s, char ... chars) {
        EnumChatFormatting[] formattings = new EnumChatFormatting[chars.length];
        int i = 0;
        for(char c : chars) {
            enums: for(EnumChatFormatting f : EnumChatFormatting.values()) {
                if(f.toString().equals("\u00a7" + c)) {
                    formattings[i] = f;
                    break enums;
                }
            }
            i++;
        }
        return c(s, formattings);
    }
    public static ChatComponentText c(String s, EnumChatFormatting ... formattings) {
        ChatComponentText comp = new ChatComponentText(s);
        ChatStyle style = comp.getChatStyle();
        for(EnumChatFormatting f : formattings) {
            if(f == EnumChatFormatting.BOLD) {
                style.setBold(true);
            } else if(f == EnumChatFormatting.ITALIC) {
                style.setItalic(true);
            } else if(f == EnumChatFormatting.UNDERLINE) {
                style.setUnderlined(true);
            } else {
                style.setColor(f);
            }
        }
        comp.setChatStyle(style);
        return comp;
    }
}

package me.guichaguri.tickratechanger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import me.guichaguri.tickratechanger.api.TickrateAPI;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.util.text.event.HoverEvent.Action;
import net.minecraft.world.GameRules;

/**
 * @author Guilherme Chaguri
 */
public class TickrateCommand extends CommandBase {
    private List<String> aliases;
    private List<String> suggestedTickrateValues;
    public TickrateCommand() {
        aliases = Arrays.asList("ticks", "tickratechanger", "trc", "settickrate");
        suggestedTickrateValues = Arrays.asList("20", "2.5", "5", "10", "15", "25", "35", "50", "100");
    }

    @Override
    public String getCommandName() {
        return "tickrate";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/tickrate [ticks per second] [all/server/client/playername]";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }
    @Override
    public List getCommandAliases() {
        return aliases;
    }

    @Override
    public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos) {
        if(args.length < 1) {
            return null;
        }
        List<String> tab = new ArrayList<String>();
        if(args.length == 1) {
            tab.addAll(suggestedTickrateValues);
            float defaultTickrate = TickrateChanger.DEFAULT_TICKRATE;
            String defTickrate = defaultTickrate + "";
            if(defaultTickrate == (int)defaultTickrate) defTickrate = (int)defaultTickrate + "";
            if(!tab.contains(defTickrate)) {
                tab.add(0, defTickrate);
            }
            tab.add("setdefault");
            tab.add("setmap");
        } else if(args.length == 2) {
            if((args[0].equalsIgnoreCase("setdefault")) || (args[0].equalsIgnoreCase("setmap"))) {
                tab.addAll(suggestedTickrateValues);
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
                for(EntityPlayerMP p : server.getPlayerList().getPlayerList()) {
                    tab.add(p.getDisplayNameString());
                }
            }
        } else if(((args.length == 3) || (args.length == 4)) && (args[0].equalsIgnoreCase("setdefault"))) {
            tab.add("--dontsave");
            tab.add("--dontupdate");
        } else if(((args.length == 3) || (args.length == 4)) && (args[0].equalsIgnoreCase("setmap"))) {
            tab.add("--dontupdate");
        }
        return tab;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        boolean showMessages = TickrateChanger.SHOW_MESSAGES;
        if(args.length < 1) {
            sender.addChatMessage(new TextComponentTranslation("tickratechanger.show.clientside"));
            chat(sender, c("Current Server Tickrate: ", 'f', 'l'), c(TickrateAPI.getServerTickrate() + " ticks per second", 'a'));
            try {
                GameRules rules = server.getEntityWorld().getGameRules();
                if(rules.hasRule(TickrateChanger.GAME_RULE)) {
                    float tickrate = Float.parseFloat(rules.getString(TickrateChanger.GAME_RULE));
                    chat(sender, c("Current Map Tickrate: ", 'f', 'l'), c(tickrate + " ticks per second", 'a'));
                }
            } catch(Exception ex) {}
            chat(sender, c("Default Tickrate: ", 'f', 'l'), c(TickrateChanger.DEFAULT_TICKRATE + " ticks per second", 'e'));
            chat(sender, c("/tickrate <ticks per second> [all/server/client/", 'b'), c("playername", 'b', 'o'), c("]", 'b'));
            chat(sender, c("/tickrate setdefault <ticks per second> [--dontsave, --dontupdate]", 'b'));
            chat(sender, c("/tickrate setmap <ticks per second> [--dontupdate]", 'b'));
            chat(sender);
            chat(sender, c("Use ", 'c'), c("/tickrate help", 'c', 'n'), c(" for more command info", 'c'));
            chat(sender);
            return;
        }
        if(args[0].equalsIgnoreCase("help")) {
            chat(sender, c(" * * Tickrate Changer * * ", '5', 'l'), c("by ", '7', 'o'), c("Guichaguri", 'f', 'o'));
            chat(sender, c("Mouse over the command to see what it does", 'f', 'l'));
            chat(sender, c("/tickrate 20 ", new TextComponentString[]{c("Sets the ", 'a'), c("server & client", 'f'), c(" tickrate to 20", 'a')}, '7'));
            chat(sender, c("/tickrate 20 server ", new TextComponentString[]{c("Sets the ", 'a'), c("server", 'f'), c(" tickrate to 20", 'a')}, '7'));
            chat(sender, c("/tickrate 20 client ", new TextComponentString[]{c("Sets ", 'a'), c("all clients", 'f'), c(" tickrate to 20", 'a')}, '7'));
            chat(sender, c("/tickrate 20 Notch ", new TextComponentString[]{c("Sets the ", 'a'), c("Notch's client", 'f'), c(" tickrate to 20", 'a')}, '7'));
            chat(sender, c("/tickrate setdefault 20 ", new TextComponentString[]{c("Sets the ", 'a'), c("default", 'f'), c(" tickrate to 20", 'a')}, '7'));
            chat(sender, c("/tickrate setdefault 20 --dontsave ", new TextComponentString[]{c("Sets the ", 'a'), c("default", 'f'),
                                                                                c(" tickrate to 20 without saving in the config", 'a')}, '7'));
            chat(sender, c("/tickrate setdefault 20 --dontupdate ", new TextComponentString[]{c("Sets the ", 'a'), c("default", 'f'),
                                                                                c(" tickrate to 20 without updating players", 'a')}, '7'));
            chat(sender, c("/tickrate setdefault 20 --dontsave --dontupdate", new TextComponentString[]{c("Sets the ", 'a'), c("default", 'f'),
                                                                                c(" tickrate to 20 without saving and updating anything", 'a')}, '7'));
            chat(sender, c("/tickrate setmap 20 ", new TextComponentString[]{c("Sets the ", 'a'), c("map", 'f'), c(" tickrate to 20", 'a')}, '7'));
            chat(sender, c("/tickrate setmap 20 --dontupdate ", new TextComponentString[]{c("Sets the ", 'a'), c("map", 'f'),
                    c(" tickrate to 20 without updating", 'a')}, '7'));
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
            if(!TickrateAPI.isValidTickrate(ticksPerSecond)) {
                chat(sender, c("Invalid tickrate value!", 'c'), c(" (Must be tickrate > 0)", '7'));
                return;
            }
            TickrateAPI.changeDefaultTickrate(ticksPerSecond, save);
            if(update) {
                TickrateAPI.changeTickrate(ticksPerSecond);
            }
            if(showMessages) chat(sender, c("Default tickrate successfully changed to", 'a'), c(" " + ticksPerSecond, 'f'), c(".", 'a'));
            return;
        } else if((args[0].equalsIgnoreCase("setmap")) && (args.length > 1)) {
            boolean update = true;
            for(String s : args) {
                if(s.equalsIgnoreCase("--dontupdate")) update = false;
            }
            float ticksPerSecond;
            try {
                ticksPerSecond = Float.parseFloat(args[1]);
            } catch(Exception ex) {
                chat(sender, c("Something went wrong!", '4'));
                chat(sender, c("/tickrate setmap <ticks per second> [--dontupdate]", 'c'));
                return;
            }
            if(!TickrateAPI.isValidTickrate(ticksPerSecond)) {
                chat(sender, c("Invalid tickrate value!", 'c'), c(" (Must be tickrate > 0)", '7'));
                return;
            }
            TickrateAPI.changeMapTickrate(ticksPerSecond);
            if(update) {
                TickrateAPI.changeTickrate(ticksPerSecond);
            }
            if(showMessages) chat(sender, c("Map tickrate successfully changed to", 'a'), c(" " + ticksPerSecond, 'f'), c(".", 'a'));
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
            if(showMessages) chat(sender, c("Tickrate successfully changed to", 'a'), c(" " + ticksPerSecond, 'f'), c(".", 'a'));
        } else if(args[1].equalsIgnoreCase("client")) {
            TickrateAPI.changeClientTickrate(ticksPerSecond);
            if(showMessages) chat(sender, c("All connected players client tickrate successfully changed to", 'a'), c(" " + ticksPerSecond, 'f'), c(".", 'a'));
        } else if(args[1].equalsIgnoreCase("server")) {
            TickrateAPI.changeServerTickrate(ticksPerSecond);
            if(showMessages) chat(sender, c("Server tickrate successfully changed to", 'a'), c(" " + ticksPerSecond, 'f'), c(".", 'a'));
        } else {
            EntityPlayer p = server.getPlayerList().getPlayerByUsername(args[1]);
            if(p == null) {
                chat(sender, c("Player not found", 'c'));
                return;
            }
            TickrateAPI.changeClientTickrate(p, ticksPerSecond);
            if(showMessages) chat(sender, c(p.getDisplayNameString() + "'s client tickrate successfully changed to", 'a'), c(" " + ticksPerSecond, 'f'), c(".", 'a'));
        }
    }

    public static void chat(ICommandSender sender, TextComponentString ... comps) {
        TextComponentString top;
        if(comps.length == 1) {
            top = comps[0];
        } else {
            top = new TextComponentString("");
            for(TextComponentString c : comps) {
                top.appendSibling(c);
            }
        }
        sender.addChatMessage(top);
    }

    public static TextComponentString c(String s, TextComponentString[] hover, char ... chars) {
        TextComponentString c = c(s, chars);
        TextComponentString hoverComp;
        if(hover.length == 1) {
            hoverComp = hover[0];
        } else {
            hoverComp = new TextComponentString("");
            for(TextComponentString txt : hover) {
                hoverComp.appendSibling(txt);
            }
        }
        c.setChatStyle(c.getChatStyle().setChatHoverEvent(new HoverEvent(Action.SHOW_TEXT, hoverComp)));
        return c;
    }
    public static TextComponentString c(String s, char ... chars) {
        TextFormatting[] formattings = new TextFormatting[chars.length];
        int i = 0;
        for(char c : chars) {
            enums: for(TextFormatting f : TextFormatting.values()) {
                if(f.toString().equals("\u00a7" + c)) {
                    formattings[i] = f;
                    break enums;
                }
            }
            i++;
        }
        return c(s, formattings);
    }
    public static TextComponentString c(String s, TextFormatting ... formattings) {
        TextComponentString comp = new TextComponentString(s);
        Style style = comp.getChatStyle();
        for(TextFormatting f : formattings) {
            if(f == TextFormatting.BOLD) {
                style.setBold(true);
            } else if(f == TextFormatting.ITALIC) {
                style.setItalic(true);
            } else if(f == TextFormatting.UNDERLINE) {
                style.setUnderlined(true);
            } else {
                style.setColor(f);
            }
        }
        comp.setChatStyle(style);
        return comp;
    }
}

package me.guichaguri.tickratechanger;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import me.guichaguri.tickratechanger.api.TickrateAPI;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.util.text.event.HoverEvent.Action;
import net.minecraft.world.GameRules;

import static net.minecraft.util.text.TextFormatting.*;

/**
 * @author Guilherme Chaguri
 */
public class TickrateCommand extends CommandBase {
    private final List<String> aliases = ImmutableList.of("ticks", "tickratechanger", "trc", "settickrate");
    private final List<String> suggestedTickrateValues = ImmutableList.of("20", "2.5", "5", "10", "15", "25", "35", "50", "100");

    @Override
    public String getName() {
        return "tickrate";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/tickrate [ticks per second] [all/server/client/playername]";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public List getAliases() {
        return aliases;
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos) {
        if(args.length < 1) return null;

        List<String> tab = new ArrayList<String>();

        if(args.length == 1) {

            tab.addAll(suggestedTickrateValues);
            float defaultTickrate = TickrateChanger.DEFAULT_TICKRATE;

            String defTickrate = Float.toString(defaultTickrate);
            if(defaultTickrate == (int)defaultTickrate) {
                defTickrate = Integer.toString((int)defaultTickrate);
            }

            if(!tab.contains(defTickrate)) tab.add(0, defTickrate);
            tab.add("setdefault");
            tab.add("setmap");

        } else if(args.length == 2) {

            if((args[0].equalsIgnoreCase("setdefault")) || (args[0].equalsIgnoreCase("setmap"))) {

                tab.addAll(suggestedTickrateValues);
                float defaultTickrate = TickrateChanger.DEFAULT_TICKRATE;

                String defTickrate = Float.toString(defaultTickrate);
                if(defaultTickrate == (int)defaultTickrate) {
                    defTickrate = Integer.toString((int)defaultTickrate);
                }

                if(!tab.contains(defTickrate)) tab.add(0, defTickrate);

            } else {

                tab.add("all");
                tab.add("server");
                tab.add("client");

                for(EntityPlayerMP p : server.getPlayerList().getPlayers()) {
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

        if(args.length < 1) {
            // Show tickrate information
            showInfo(server, sender);
            return;
        } else if(args[0].equalsIgnoreCase("help")) {
            // Show help
            showHelp(sender);
            return;
        } else if((args[0].equalsIgnoreCase("setdefault")) && (args.length > 1)) {
            // Set default tickrate
            cmdSetDefault(sender, args);
            return;
        } else if((args[0].equalsIgnoreCase("setmap")) && (args.length > 1)) {
            // Set map tickrate
            cmdSetMap(sender, args);
            return;
        }

        float ticksPerSecond;

        try {
            ticksPerSecond = Float.parseFloat(args[0]);
        } catch(Exception ex) {
            chat(sender, t("tickratechanger.cmd.error", DARK_RED));
            chat(sender, c("/tickrate <ticks per second> [all/server/client/", RED), c("playername", RED, ITALIC), c("]", RED));
            return;
        }

        if((args.length < 2) || (args[1].equalsIgnoreCase("all"))) {

            // Set the tickrate
            TickrateAPI.changeTickrate(ticksPerSecond);

            if(TickrateChanger.SHOW_MESSAGES) {
                chat(sender, t("tickratechanger.cmd.everything.success", GREEN, ticksPerSecond));
            }

        } else if(args[1].equalsIgnoreCase("client")) {

            // Set client tickrate
            TickrateAPI.changeClientTickrate(ticksPerSecond);

            if(TickrateChanger.SHOW_MESSAGES) {
                chat(sender, t("tickratechanger.cmd.client.success", GREEN, ticksPerSecond));
            }

        } else if(args[1].equalsIgnoreCase("server")) {

            // Set server tickrate
            TickrateAPI.changeServerTickrate(ticksPerSecond);

            if(TickrateChanger.SHOW_MESSAGES) {
                chat(sender, t("tickratechanger.cmd.server.success", GREEN, ticksPerSecond));
            }

        } else {

            // Set player tickrate
            EntityPlayer p = server.getPlayerList().getPlayerByUsername(args[1]);
            if(p == null) {
                chat(sender, t("tickratechanger.cmd.player.error", RED));
                return;
            }

            TickrateAPI.changeClientTickrate(p, ticksPerSecond);

            if(TickrateChanger.SHOW_MESSAGES) {
                chat(sender, t("tickratechanger.cmd.player.success", GREEN, p.getDisplayNameString(), ticksPerSecond));
            }

        }
    }

    private void showInfo(MinecraftServer server, ICommandSender sender) {
        chat(sender, new TextComponentTranslation("tickratechanger.show.clientside"));
        chat(sender, t("tickratechanger.info.server", WHITE), t("tickratechanger.info.value", GREEN, TickrateAPI.getServerTickrate()));

        try {
            GameRules rules = server.getEntityWorld().getGameRules();
            if(rules.hasRule(TickrateChanger.GAME_RULE)) {
                float tickrate = Float.parseFloat(rules.getString(TickrateChanger.GAME_RULE));
                chat(sender, t("tickratechanger.info.map", WHITE), t("tickratechanger.info.value", GREEN, tickrate));
            }
        } catch(Exception ex) {
            // Invalid map tickrate
        }

        chat(sender, t("tickratechanger.info.default", WHITE), t("tickratechanger.info.value", YELLOW, TickrateChanger.DEFAULT_TICKRATE));
        chat(sender);
        chat(sender, c("/tickrate <ticks per second> [all/server/client/", AQUA), c("playername", DARK_AQUA), c("]", AQUA));
        chat(sender, c("/tickrate setdefault <ticks per second> [--dontsave, --dontupdate]", AQUA));
        chat(sender, c("/tickrate setmap <ticks per second> [--dontupdate]", AQUA));
        chat(sender);
        chat(sender, t("tickratechanger.info.help", RED, WHITE + "/tickrate help" + RED));
    }

    private void showHelp(ICommandSender sender) {
        chat(sender, c(" * * Tickrate Changer * * ", DARK_PURPLE, BOLD), c("by ", GRAY, ITALIC), c("Guichaguri", WHITE, ITALIC));
        chat(sender, t("tickratechanger.help.desc", GREEN));

        chat(sender, c("/tickrate 20", t("tickratechanger.help.command.1", GREEN), GRAY));
        chat(sender, c("/tickrate 20 server", t("tickratechanger.help.command.2", GREEN), GRAY));
        chat(sender, c("/tickrate 20 client", t("tickratechanger.help.command.3", GREEN), GRAY));
        chat(sender, c("/tickrate 20 Notch", t("tickratechanger.help.command.4", GREEN), GRAY));
        chat(sender, c("/tickrate setdefault 20", t("tickratechanger.help.command.5", GREEN), GRAY));
        chat(sender, c("/tickrate setdefault 20 --dontsave", t("tickratechanger.help.command.6", GREEN), GRAY));
        chat(sender, c("/tickrate setdefault 20 --dontupdate", t("tickratechanger.help.command.7", GREEN), GRAY));
        chat(sender, c("/tickrate setdefault 20 --dontsave --dontupdate", t("tickratechanger.help.command.8", GREEN), GRAY));
        chat(sender, c("/tickrate setmap 20", t("tickratechanger.help.command.9", GREEN), GRAY));
        chat(sender, c("/tickrate setmap 20 --dontupdate", t("tickratechanger.help.command.10", GREEN), GRAY));

        chat(sender, c(" * * * * * * * * * * * * * * ", DARK_PURPLE, BOLD));
    }

    private void cmdSetDefault(ICommandSender sender, String[] args) {
        boolean save = true, update = true;

        for(String s : args) {
            if(s.equalsIgnoreCase("--dontsave")) save = false;
            if(s.equalsIgnoreCase("--dontupdate")) update = false;
        }

        float ticksPerSecond;

        try {
            ticksPerSecond = Float.parseFloat(args[1]);
        } catch(Exception ex) {
            chat(sender, t("tickratechanger.cmd.error", DARK_RED));
            chat(sender, c("/tickrate setdefault <ticks per second> [--dontsave, --dontupdate]", RED));
            return;
        }

        TickrateAPI.changeDefaultTickrate(ticksPerSecond, save);
        if(update) TickrateAPI.changeTickrate(ticksPerSecond);

        if(TickrateChanger.SHOW_MESSAGES) {
            chat(sender, t("tickratechanger.cmd.default.success", GREEN, ticksPerSecond));
        }
    }

    private void cmdSetMap(ICommandSender sender, String[] args) {
        boolean update = true;

        for(String s : args) {
            if(s.equalsIgnoreCase("--dontupdate")) update = false;
        }

        float ticksPerSecond;

        try {
            ticksPerSecond = Float.parseFloat(args[1]);
        } catch(Exception ex) {
            chat(sender, t("tickratechanger.cmd.error", DARK_RED));
            chat(sender, c("/tickrate setmap <ticks per second> [--dontupdate]", RED));
            return;
        }

        TickrateAPI.changeMapTickrate(ticksPerSecond);
        if(update) TickrateAPI.changeTickrate(ticksPerSecond);

        if(TickrateChanger.SHOW_MESSAGES) {
            chat(sender, t("tickratechanger.cmd.map.success", GREEN, ticksPerSecond));
        }
    }

    protected static ITextComponent clientTickrateMsg() {
        ITextComponent msg = new TextComponentString("");
        msg.appendSibling(t("tickratechanger.info.client", WHITE));
        msg.appendText(" ");
        msg.appendSibling(t("tickratechanger.info.value", GREEN, TickrateAPI.getClientTickrate()));
        return msg;
    }

    protected static ITextComponent successTickrateMsg(float ticksPerSecond) {
        return t("tickratechanger.cmd.everything.success", GREEN, ticksPerSecond);
    }

    private static void chat(ICommandSender sender, ITextComponent... comps) {
        ITextComponent top;
        if(comps.length == 1) {
            top = comps[0];
        } else {
            top = new TextComponentString("");
            for(ITextComponent c : comps) {
                top.appendSibling(c);
                top.appendText(" ");
            }
        }
        sender.sendMessage(top);
    }

    private static ITextComponent t(String langKey, TextFormatting formatting, Object ... data) {
        ITextComponent comp = new TextComponentTranslation(langKey, data);
        comp.setStyle(comp.getStyle().setColor(formatting));
        return comp;
    }

    private static TextComponentString c(String s, ITextComponent hover, TextFormatting ... formattings) {
        TextComponentString txt = c(s, formattings);
        txt.setStyle(txt.getStyle().setHoverEvent(new HoverEvent(Action.SHOW_TEXT, hover)));
        return txt;
    }

    private static TextComponentString c(String s, TextFormatting ... formattings) {
        TextComponentString comp = new TextComponentString(s);
        Style style = comp.getStyle();
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
        comp.setStyle(style);
        return comp;
    }
}

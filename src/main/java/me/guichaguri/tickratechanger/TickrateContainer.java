package me.guichaguri.tickratechanger;

import me.guichaguri.tickratechanger.TickrateMessageHandler.TickrateMessage;
import me.guichaguri.tickratechanger.api.TickrateAPI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.GameRules;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.input.Keyboard;

/**
 * @author Guilherme Chaguri
 */
@Mod(modid = TickrateChanger.MODID, name = "Tickrate Changer", version = TickrateChanger.VERSION)
public class TickrateContainer {

    public static boolean KEYS_AVAILABLE = false;

    public static KeyBinding KEY_5 = null;
    public static KeyBinding KEY_10 = null;
    public static KeyBinding KEY_15 = null;
    public static KeyBinding KEY_20 = null;
    public static KeyBinding KEY_40 = null;
    public static KeyBinding KEY_60 = null;
    public static KeyBinding KEY_100 = null;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        TickrateChanger.NETWORK = NetworkRegistry.INSTANCE.newSimpleChannel("TickrateChanger");
        TickrateChanger.NETWORK.registerMessage(TickrateMessageHandler.class, TickrateMessage.class, 0, Side.CLIENT);
        TickrateChanger.NETWORK.registerMessage(TickrateMessageHandler.class, TickrateMessage.class, 1, Side.SERVER);

        TickrateChanger.CONFIG_FILE = event.getSuggestedConfigurationFile();
        Configuration cfg = new Configuration(TickrateChanger.CONFIG_FILE);
        TickrateChanger.DEFAULT_TICKRATE = (float)cfg.get("default", "tickrate", 20.0,
                "Default tickrate. The game will always initialize with this value.").getDouble(20);
        TickrateChanger.MIN_TICKRATE = (float)cfg.get("minimum", "tickrate", 0.1,
                "Minimum tickrate from servers. Prevents really low tickrate values.").getDouble(0.1);
        TickrateChanger.MAX_TICKRATE = (float)cfg.get("maximum", "tickrate", 1000,
                "Maximum tickrate from servers. Prevents really high tickrate values.").getDouble(1000);
        TickrateChanger.SHOW_MESSAGES = cfg.get("miscellaneous", "show-messages", true,
                "If it will show log messages in the console and the game").getBoolean(true);
        KEYS_AVAILABLE = cfg.get("miscellaneous", "keybindings", false,
                "If it will have special keys for setting the tickrate").getBoolean(false);

        if(KEYS_AVAILABLE) {
            // Keys
            KEY_5 = new KeyBinding("Set tickrate to 5", Keyboard.KEY_NONE, "key.categories.misc");
            KEY_10 = new KeyBinding("Set tickrate to 10", Keyboard.KEY_NONE, "key.categories.misc");
            KEY_15 = new KeyBinding("Set tickrate to 15", Keyboard.KEY_NONE, "key.categories.misc");
            KEY_20 = new KeyBinding("Set tickrate to 20", Keyboard.KEY_NONE, "key.categories.misc");
            KEY_40 = new KeyBinding("Set tickrate to 40", Keyboard.KEY_NONE, "key.categories.misc");
            KEY_60 = new KeyBinding("Set tickrate to 60", Keyboard.KEY_NONE, "key.categories.misc");
            KEY_100 = new KeyBinding("Set tickrate to 100", Keyboard.KEY_NONE, "key.categories.misc");
            ClientRegistry.registerKeyBinding(KEY_5);
            ClientRegistry.registerKeyBinding(KEY_10);
            ClientRegistry.registerKeyBinding(KEY_15);
            ClientRegistry.registerKeyBinding(KEY_20);
            ClientRegistry.registerKeyBinding(KEY_40);
            ClientRegistry.registerKeyBinding(KEY_60);
            ClientRegistry.registerKeyBinding(KEY_100);
        }

        cfg.save();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        TickrateAPI.changeTickrate(TickrateChanger.DEFAULT_TICKRATE);
    }

    @EventHandler
    public void start(FMLServerStartingEvent event) {
        TickrateChanger.COMMAND = new TickrateCommand();
        event.registerServerCommand(TickrateChanger.COMMAND);
    }

    @SubscribeEvent
    public void chat(ClientChatReceivedEvent event) {
        ITextComponent message = event.getMessage();
        if(message instanceof TextComponentTranslation) {
            TextComponentTranslation t = (TextComponentTranslation)message;
            if(t.getKey().equals("tickratechanger.show.clientside")) {
                message = new TextComponentString("");
                message.appendSibling(TickrateCommand.c("Your Current Client Tickrate: ", 'f', 'l'));
                message.appendSibling(TickrateCommand.c(TickrateAPI.getClientTickrate() + " ticks per second", 'a'));
                event.setMessage(message);
            }
        }
    }

    @SubscribeEvent
    public void disconnect(ClientDisconnectionFromServerEvent event) {
        TickrateAPI.changeServerTickrate(TickrateChanger.DEFAULT_TICKRATE);
        TickrateAPI.changeClientTickrate(null, TickrateChanger.DEFAULT_TICKRATE);
    }

    @SubscribeEvent
    public void connect(ClientConnectedToServerEvent event) {
        if(event.isLocal()) {
            float tickrate = TickrateChanger.DEFAULT_TICKRATE;
            try {
                MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
                if(server != null) {
                    GameRules rules = server.getEntityWorld().getGameRules();
                    if(rules.hasRule(TickrateChanger.GAME_RULE)) {
                        tickrate = Float.parseFloat(rules.getString(TickrateChanger.GAME_RULE));
                    }
                }
            } catch(Exception ex) {
                ex.printStackTrace();
            }
            TickrateAPI.changeServerTickrate(tickrate);
            TickrateAPI.changeClientTickrate(null, tickrate);
        } else {
            TickrateAPI.changeClientTickrate(null, 20F);
        }
    }

    @SubscribeEvent
    public void connect(PlayerLoggedInEvent event) {
        if(FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER) {
            float tickrate = TickrateChanger.DEFAULT_TICKRATE;
            try {
                MinecraftServer server = event.player.getServer();
                if(server != null) {
                    GameRules rules = server.getEntityWorld().getGameRules();
                    if(rules.hasRule(TickrateChanger.GAME_RULE)) {
                        tickrate = Float.parseFloat(rules.getString(TickrateChanger.GAME_RULE));
                    }
                }
            } catch(Exception ex) {
                ex.printStackTrace();
            }
            TickrateAPI.changeClientTickrate(event.player, tickrate);
        }
    }

    long lastKeyInputTime = 0;

    @SubscribeEvent
    public void key(KeyInputEvent event) {
        if(!KEYS_AVAILABLE) return;

        float tickrate;
        if(KEY_5.isPressed()) {
            tickrate = 5;
        } else if(KEY_10.isPressed()) {
            tickrate = 10;
        } else if(KEY_15.isPressed()) {
            tickrate = 15;
        } else if(KEY_20.isPressed()) {
            tickrate = 20;
        } else if(KEY_40.isPressed()) {
            tickrate = 40;
        } else if(KEY_60.isPressed()) {
            tickrate = 60;
        } else if(KEY_100.isPressed()) {
            tickrate = 100;
        } else {
            return;
        }

        // Cooldown. 0.1 real life second to prevent spam
        if(lastKeyInputTime > Minecraft.getSystemTime() - 100) return;
        lastKeyInputTime = Minecraft.getSystemTime();

        TickrateChanger.NETWORK.sendToServer(new TickrateMessage(tickrate));
    }

}

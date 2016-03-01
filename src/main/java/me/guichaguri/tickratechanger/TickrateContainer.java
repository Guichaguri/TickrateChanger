package me.guichaguri.tickratechanger;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import java.util.Arrays;
import me.guichaguri.tickratechanger.TickrateMessageHandler.TickrateMessage;
import me.guichaguri.tickratechanger.api.TickrateAPI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.GameRules;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.ModMetadata;
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
public class TickrateContainer extends DummyModContainer {

    private static ModMetadata createMetadata() {
        ModMetadata meta = new ModMetadata();
        meta.modId = TickrateChanger.MODID;
        meta.name = "Tickrate Changer";
        meta.version = TickrateChanger.VERSION;
        meta.authorList = Arrays.asList("Guichaguri");
        meta.description = "Let you change the client/server tickrate";
        meta.url = "http://minecraft.curseforge.com/mc-mods/230233-tickratechanger";
        return meta;
    }

    public static boolean KEYS_AVAILABLE = false;

    public static KeyBinding KEY_5 = null;
    public static KeyBinding KEY_10 = null;
    public static KeyBinding KEY_15 = null;
    public static KeyBinding KEY_20 = null;
    public static KeyBinding KEY_40 = null;
    public static KeyBinding KEY_60 = null;
    public static KeyBinding KEY_100 = null;

    public TickrateContainer() {
        super(createMetadata());
    }

    @Override
    public boolean registerBus(EventBus bus, LoadController controller) {
        bus.register(this);
        return true;
    }

    @Subscribe
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

    @Subscribe
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        TickrateAPI.changeTickrate(TickrateChanger.DEFAULT_TICKRATE);
    }

    @Subscribe
    public void start(FMLServerStartingEvent event) {
        TickrateChanger.COMMAND = new TickrateCommand();
        event.registerServerCommand(TickrateChanger.COMMAND);
    }

    @SubscribeEvent
    public void chat(ClientChatReceivedEvent event) {
        if(event.message instanceof ChatComponentTranslation) {
            ChatComponentTranslation t = (ChatComponentTranslation)event.message;
            if(t.getKey().equals("tickratechanger.show.clientside")) {
                event.message = new ChatComponentText("");
                event.message.appendSibling(TickrateCommand.c("Your Current Client Tickrate: ", 'f', 'l'));
                event.message.appendSibling(TickrateCommand.c(TickrateAPI.getClientTickrate() + " ticks per second", 'a'));
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
        if(event.isLocal) {
            float tickrate = TickrateChanger.DEFAULT_TICKRATE;
            try {
                GameRules rules = MinecraftServer.getServer().getEntityWorld().getGameRules();
                if(rules.hasRule(TickrateChanger.GAME_RULE)) {
                    tickrate = Float.parseFloat(rules.getString(TickrateChanger.GAME_RULE));
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
                GameRules rules = MinecraftServer.getServer().getEntityWorld().getGameRules();
                if(rules.hasRule(TickrateChanger.GAME_RULE)) {
                    tickrate = Float.parseFloat(rules.getString(TickrateChanger.GAME_RULE));
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

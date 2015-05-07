package me.guichaguri.tickratechanger;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import java.util.Arrays;
import me.guichaguri.tickratechanger.TickrateMessageHandler.TickrateMessage;
import me.guichaguri.tickratechanger.api.TickrateAPI;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import cpw.mods.fml.common.DummyModContainer;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.LoadController;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.relauncher.Side;

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

        TickrateChanger.CONFIG_FILE = event.getSuggestedConfigurationFile();
        Configuration cfg = new Configuration(TickrateChanger.CONFIG_FILE);
        TickrateChanger.DEFAULT_TICKRATE = (float)cfg.get("default", "tickrate", 20.0,
                "Default tickrate. The game will always initialize with this value.").getDouble(20);
        cfg.save();
    }

    @Subscribe
    public void init(FMLInitializationEvent event) {
        FMLCommonHandler.instance().bus().register(this);
        MinecraftForge.EVENT_BUS.register(this);
        TickrateAPI.changeTickrate(TickrateChanger.DEFAULT_TICKRATE);
    }

    @Subscribe
    public void start(FMLServerStartingEvent event) {
        event.registerServerCommand(new TickrateCommand());
    }

    @SubscribeEvent
    public void chat(ClientChatReceivedEvent event) {
        if(event.message instanceof ChatComponentTranslation) {
            ChatComponentTranslation t = (ChatComponentTranslation)event.message;
            if(t.getKey().equals("tickratechanger.show.clientside")) {
                event.message = new ChatComponentText("");
                event.message.appendSibling(TickrateCommand.c("Your Current Client Tickrate: ", 'f', 'l'));
                event.message.appendSibling(TickrateCommand.c(TickrateChanger.TICKS_PER_SECOND + " ticks per second", 'a'));
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
            TickrateAPI.changeServerTickrate(TickrateChanger.DEFAULT_TICKRATE);
            TickrateAPI.changeClientTickrate(null, TickrateChanger.DEFAULT_TICKRATE);
        } else {
            TickrateAPI.changeClientTickrate(null, 20F);
        }
    }

    @SubscribeEvent
    public void connect(PlayerLoggedInEvent event) {
        if(FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER) {
            TickrateAPI.changeClientTickrate(event.player, TickrateChanger.DEFAULT_TICKRATE);
        }
    }

}

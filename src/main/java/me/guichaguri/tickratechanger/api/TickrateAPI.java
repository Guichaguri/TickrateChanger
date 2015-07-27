package me.guichaguri.tickratechanger.api;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import java.util.List;
import me.guichaguri.tickratechanger.TickrateChanger;
import me.guichaguri.tickratechanger.TickrateMessageHandler.TickrateMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.common.config.Configuration;

/**
 * @author Guilherme Chaguri
 */
public class TickrateAPI {

    /**
     * Let you change the client & server tickrate
     * Can only be called from server-side. Can also be called from client-side if is singleplayer.
     * @param ticksPerSecond Tickrate to be set
     */
    public static void changeTickrate(float ticksPerSecond) {
        changeServerTickrate(ticksPerSecond);
        changeClientTickrate(ticksPerSecond);
    }


    /**
     * Let you change the server tickrate
     * Can only be called from server-side. Can also be called from client-side if is singleplayer.
     * @param ticksPerSecond Tickrate to be set
     */
    public static void changeServerTickrate(float ticksPerSecond) {
        TickrateChanger.INSTANCE.updateServerTickrate(ticksPerSecond);
    }

    /**
     * Let you change the all clients tickrate
     * Can be called either from server-side or client-side
     * @param ticksPerSecond Tickrate to be set
     */
    public static void changeClientTickrate(float ticksPerSecond) {
        MinecraftServer server = MinecraftServer.getServer();
        if((server != null) && (server.getConfigurationManager() != null)) { // Is a server or singleplayer
            for(EntityPlayer p : (List<EntityPlayer>)server.getConfigurationManager().playerEntityList) {
                changeClientTickrate(p, ticksPerSecond);
            }
        } else { // Is in menu or a player connected in a server. We can say this is client.
            changeClientTickrate(null, ticksPerSecond);
        }
    }

    /**
     * Let you change the all clients tickrate
     * Can be called either from server-side or client-side.
     * Will only take effect in the client-side if the player is Minecraft.thePlayer
     * @param player The Player
     * @param ticksPerSecond Tickrate to be set
     */
    public static void changeClientTickrate(EntityPlayer player, float ticksPerSecond) {
        if((player == null) || (player.worldObj.isRemote)) { // Client
            if(FMLCommonHandler.instance().getSide() != Side.CLIENT) return;
            if((player != null) && (player != Minecraft.getMinecraft().thePlayer)) return;
            TickrateChanger.INSTANCE.updateClientTickrate(ticksPerSecond);
        } else { // Server
            TickrateChanger.NETWORK.sendTo(new TickrateMessage(ticksPerSecond), (EntityPlayerMP)player);
        }
    }

    /**
     * Let you change the server tickrate
     * Can only be called from server-side. Can also be called from client-side if is singleplayer.
     * This will not update the tickrate from the server/clients.
     * @param ticksPerSecond Tickrate to be set
     * @param save If will be saved in the config file
     */
    public static void changeDefaultTickrate(float ticksPerSecond, boolean save) {
        TickrateChanger.DEFAULT_TICKRATE = ticksPerSecond;
        if(save) {
            Configuration cfg = new Configuration(TickrateChanger.CONFIG_FILE);
            cfg.get("default", "tickrate", 20.0, "Default tickrate. The game will always initialize with this value.").set(ticksPerSecond);
            cfg.save();
        }
    }

    /**
     * Let you change the map tickrate
     * Can only be called from server-side. Can also be called from client-side if is singleplayer.
     * This will not update the tickrate from the server/clients
     * @param ticksPerSecond Tickrate to be set
     */
    public static void changeMapTickrate(float ticksPerSecond) {
        World world = MinecraftServer.getServer().getEntityWorld();
        world.getGameRules().setOrCreateGameRule(TickrateChanger.GAME_RULE, ticksPerSecond + "");
    }

    /**
     * Checks if the tickrate is valid
     * @param ticksPerSecond Tickrate to be checked
     */
    public static boolean isValidTickrate(float ticksPerSecond) {
        return ticksPerSecond > 0F;
    }
}

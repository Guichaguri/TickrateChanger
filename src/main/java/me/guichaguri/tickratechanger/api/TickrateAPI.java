package me.guichaguri.tickratechanger.api;

import java.util.List;
import me.guichaguri.tickratechanger.TickrateChanger;
import me.guichaguri.tickratechanger.TickrateMessageHandler.TickrateMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

/**
 * @author Guilherme Chaguri
 */
public class TickrateAPI {

    /**
     * Let you change the client & server tickrate
     * Can be called from server-side or client-side if is singleplayer
     * @param ticksPerSecond
     */
    public static void changeTickrate(float ticksPerSecond) {
        changeServerTickrate(ticksPerSecond);
        changeClientTickrate(ticksPerSecond);
    }


    /**
     * Let you change the server tickrate
     * Can only be called from server-side or client-side if is singleplayer
     * @param ticksPerSecond
     */
    public static void changeServerTickrate(float ticksPerSecond) {
        TickrateChanger.INSTANCE.updateServerTickrate(ticksPerSecond);
    }

    /**
     * Let you change the all clients tickrate
     * Can be called either from server-side or client-side
     * @param ticksPerSecond
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
     * @param ticksPerSecond
     */
    public static void changeClientTickrate(EntityPlayer p, float ticksPerSecond) {
        if((p == null) || (p.worldObj.isRemote)) { // Client
            if((p != null) && (p != Minecraft.getMinecraft().thePlayer)) return;
            TickrateChanger.INSTANCE.updateClientTickrate(ticksPerSecond);
        } else { // Server
            TickrateChanger.NETWORK.sendTo(new TickrateMessage(ticksPerSecond), (EntityPlayerMP)p);
        }
    }

    /**
     * Checks if the tickrate is valid
     * @param ticksPerSecond
     */
    public static boolean isValidTickrate(float ticksPerSecond) {
        return ticksPerSecond > 0F;
    }
}

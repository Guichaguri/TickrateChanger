package me.guichaguri.tickratechanger;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Map;
import me.guichaguri.tickratechanger.api.TickrateAPI;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Timer;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.IFMLCallHook;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.TransformerExclusions;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Guilherme Chaguri
 */
@TransformerExclusions({"me.guichaguri.tickratechanger"})
public class TickrateChanger implements IFMLLoadingPlugin, IFMLCallHook {

    public static TickrateChanger INSTANCE;
    public static Logger LOGGER = LogManager.getLogger("Tickrate Changer");
    public static SimpleNetworkWrapper NETWORK;
    public static File CONFIG_FILE = null;

    public static final String MODID = "tickratechanger";
    public static final String VERSION = "1.0.2c";

    public static final String GAME_RULE = "tickrate";

    // Default tickrate - can be changed in the config file
    public static float DEFAULT_TICKRATE = 20;
    // Stored client-side tickrate
    public static float TICKS_PER_SECOND = 20;
    // Server-side tickrate in miliseconds
    public static long MILISECONDS_PER_TICK = 50L;
    // Min Tickrate
    public static float MIN_TICKRATE = 0.1F;
    // Max Tickrate
    public static float MAX_TICKRATE = 1000;

    public TickrateChanger() {
        INSTANCE = this;
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[]{"me.guichaguri.tickratechanger.TickrateTransformer"};
    }
    @Override
    public String getModContainerClass() {
        return "me.guichaguri.tickratechanger.TickrateContainer";
    }
    @Override
    public String getSetupClass() {
        return "me.guichaguri.tickratechanger.TickrateChanger";
    }
    @Override
    public void injectData(Map<String, Object> data) {

    }
    @Override
    public String getAccessTransformerClass() {
        return null;
    }

    @Override
    public Void call() throws Exception {
        return null;
    }

    private Field clientTimer = null;
    @SideOnly(Side.CLIENT)
    public void updateClientTickrate(float tickrate) {
        if(!TickrateAPI.isValidTickrate(tickrate)) {
            TickrateChanger.LOGGER.info("Ignoring invalid tickrate: " + tickrate);
            return;
        }
        LOGGER.info("Updating client tickrate to " + tickrate);
        TICKS_PER_SECOND = tickrate;
        Minecraft mc = Minecraft.getMinecraft();
        if(mc == null) return; // Oops!
        try {
            if(clientTimer == null) {
                TickrateChanger.LOGGER.info("Creating reflection instances...");
                for(Field f : mc.getClass().getDeclaredFields()) {
                    if(f.getType() == Timer.class) {
                        clientTimer = f;
                        clientTimer.setAccessible(true);
                        break;
                    }
                }
            }
            clientTimer.set(mc, new Timer(TICKS_PER_SECOND));
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    public void updateServerTickrate(float tickrate) {
        if(!TickrateAPI.isValidTickrate(tickrate)) {
            TickrateChanger.LOGGER.info("Ignoring invalid tickrate: " + tickrate);
            return;
        }
        LOGGER.info("Updating server tickrate to " + tickrate);
        MILISECONDS_PER_TICK = (long)(1000L / tickrate);
    }
}

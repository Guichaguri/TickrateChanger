package me.guichaguri.tickratechanger;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;
import me.guichaguri.tickratechanger.TickrateMessageHandler.TickrateMessage;

/**
 * @author Guilherme Chaguri
 */
public class TickrateMessageHandler implements IMessageHandler<TickrateMessage, IMessage> {

    @Override
    public IMessage onMessage(TickrateMessage msg, MessageContext context) {
        float tickrate = msg.getTickrate();
        if(tickrate < TickrateChanger.MIN_TICKRATE) {
            TickrateChanger.LOGGER.info("Tickrate forced to change from " + tickrate + " to " +
                    TickrateChanger.MIN_TICKRATE + ", because the value is too low" +
                    " (You can change the minimum tickrate in the config file)");
            tickrate = TickrateChanger.MIN_TICKRATE;
        } else if(tickrate > TickrateChanger.MAX_TICKRATE) {
            TickrateChanger.LOGGER.info("Tickrate forced to change from " + tickrate + " to " +
                    TickrateChanger.MAX_TICKRATE + ", because the value is too high" +
                    " (You can change the maximum tickrate in the config file)");
            tickrate = TickrateChanger.MAX_TICKRATE;
        }
        if(FMLCommonHandler.instance().getSide() != Side.SERVER) {
            TickrateChanger.INSTANCE.updateClientTickrate(tickrate);
        }
        return null;
    }

    public static class TickrateMessage implements IMessage {
        private float tickrate;
        public TickrateMessage() {}
        public TickrateMessage(float tickrate) {
            this.tickrate = tickrate;
        }

        public float getTickrate() {
            return tickrate;
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            tickrate = buf.readFloat();
        }
        @Override
        public void toBytes(ByteBuf buf) {
            buf.writeFloat(tickrate);
        }
    }
}

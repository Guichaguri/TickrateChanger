package me.guichaguri.tickratechanger;

import io.netty.buffer.ByteBuf;
import me.guichaguri.tickratechanger.TickrateMessageHandler.TickrateMessage;
import me.guichaguri.tickratechanger.api.TickrateAPI;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

/**
 * @author Guilherme Chaguri
 */
public class TickrateMessageHandler implements IMessageHandler<TickrateMessage, IMessage> {

    @Override
    public IMessage onMessage(TickrateMessage msg, MessageContext context) {
        if(context.side == Side.SERVER) {
            EntityPlayerMP player = context.getServerHandler().player;
            if(!TickrateChanger.COMMAND.checkPermission(player.mcServer, player)) return null;
        }

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

        if(context.side == Side.CLIENT) {
            TickrateChanger.getInstance().updateClientTickrate(tickrate, TickrateChanger.SHOW_MESSAGES);
        } else {
            TickrateAPI.changeTickrate(tickrate, TickrateChanger.SHOW_MESSAGES);

            if(TickrateChanger.SHOW_MESSAGES) {
                context.getServerHandler().player.sendMessage(TickrateCommand.successTickrateMsg(tickrate));
            }
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

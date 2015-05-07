package me.guichaguri.tickratechanger;

import io.netty.buffer.ByteBuf;
import me.guichaguri.tickratechanger.TickrateMessageHandler.TickrateMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * @author Guilherme Chaguri
 */
public class TickrateMessageHandler implements IMessageHandler<TickrateMessage, IMessage> {

    @Override
    public IMessage onMessage(TickrateMessage msg, MessageContext context) {
        TickrateChanger.INSTANCE.updateClientTickrate(msg.getTickrate());
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

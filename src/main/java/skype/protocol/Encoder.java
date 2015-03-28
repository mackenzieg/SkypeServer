package skype.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class Encoder extends MessageToByteEncoder<DefinedPacket> {

    private Protocol protocol;

    public Encoder(Protocol protocol) {
        this.protocol = protocol;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, DefinedPacket msg, ByteBuf out) throws Exception {
        Protocol.ProtocolData prot = protocol.OUTBOUND;
        DefinedPacket.writeVarInt(prot.getId(msg.getClass()), out);
        msg.write(out);
    }
}

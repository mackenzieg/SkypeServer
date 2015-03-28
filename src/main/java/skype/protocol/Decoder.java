package skype.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import skype.protocol.packet.Ping;

import java.util.List;

public class Decoder extends ByteToMessageDecoder {

    private Protocol protocol;

    public Decoder(Protocol protocol) {
        this.protocol = protocol;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        Protocol.ProtocolData prot = protocol.INBOUND;
        ByteBuf copy = in.copy();
        int packetId = DefinedPacket.readVarInt(in);
        DefinedPacket packet = null;
        if (prot.hasPacket(packetId)) {
            packet = prot.createPacket(packetId);
            packet.read(in);
            if (in.readableBytes() != 0) {
                throw new BadPacketException("Did not read all bytes from packet " + packet.getClass() + " " + packetId + " Protocol " + protocol + " Direction " + prot);
            }
        } else {
            in.skipBytes(in.readableBytes());
        }
        out.add(new PacketWrapper(packet, copy));
    }

}

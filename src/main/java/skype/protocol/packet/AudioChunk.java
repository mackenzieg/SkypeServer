package skype.protocol.packet;

import io.netty.buffer.ByteBuf;
import skype.protocol.AbstractPacketHandler;
import skype.protocol.DefinedPacket;

public class AudioChunk extends DefinedPacket {

    /*
    +-------------+-----------+
    | Packet id   |           |
    +-------------+-----------+
    | Audio chunk |           |
    +-------------+-----------+
     */

    private byte[] data;

    @Override
    public void read(ByteBuf buf) {
        this.data = readArray(buf);
    }

    @Override
    public void write(ByteBuf buf) {
        writeArray(this.data, buf);
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception {
        handler.handle(this);
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

}

package skype.protocol.packet;

import io.netty.buffer.ByteBuf;
import skype.protocol.AbstractPacketHandler;
import skype.protocol.DefinedPacket;

public class Ping extends DefinedPacket {

    /*
    +-----------+
    | Packet id |
    +-----------+
    | Time sent |
    +-----------+
     */

    private long time;

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    @Override
    public void read(ByteBuf buf) {
        this.time = buf.readLong();
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeLong(this.time);
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception {
        handler.handle(this);
    }

}

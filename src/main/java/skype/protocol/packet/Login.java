package skype.protocol.packet;

import io.netty.buffer.ByteBuf;
import skype.protocol.AbstractPacketHandler;
import skype.protocol.DefinedPacket;

public class Login extends DefinedPacket {

    /*
    +-------------+
    |  Packet id  |
    +-------------+
    | Client name |
    +-------------+
     */

    private String data;

    @Override
    public void read(ByteBuf buf) {
        this.data = readString(buf);
    }

    @Override
    public void write(ByteBuf buf) {
        writeString(this.data, buf);
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception {
        handler.handle(this);
    }

    public String getName() {
        return data;
    }

    public void setName(String name) {
        this.data = name;
    }

}

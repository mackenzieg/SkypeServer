package skype.protocol;

import io.netty.buffer.ByteBuf;

public class PacketWrapper {

    public final DefinedPacket packet;
    public final ByteBuf buf;
    private boolean released;

    public PacketWrapper(DefinedPacket packet, ByteBuf buf) {
        this.packet = packet;
        this.buf = buf;
    }

    public void trySingleRelease() {
        if (!released) {
            buf.release();
            released = true;
        }
    }

    public boolean isReleased() {
        return released;
    }

    public void setReleased(boolean released) {
        this.released = released;
    }
}


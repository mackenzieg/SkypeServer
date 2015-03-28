package skype.protocol;

import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public abstract class DefinedPacket {

    public static void writeString(String s, ByteBuf buf) {
        byte[] b = s.getBytes(StandardCharsets.UTF_8);
        writeVarInt(b.length, buf);
        buf.writeBytes(b);
    }

    public static String readString(ByteBuf buf) {
        int len = readVarInt(buf);
        byte[] b = new byte[len];
        buf.readBytes(b);
        return new String(b, StandardCharsets.UTF_8);
    }

    public static void writeArrayLegacy(byte[] b, ByteBuf buf, boolean allowExtended) {
        writeVarShort(buf, b.length);
        buf.writeBytes(b);
    }

    public static byte[] readArrayLegacy(ByteBuf buf) {
        int len = readVarShort(buf);
        byte[] ret = new byte[len];
        buf.readBytes(ret);
        return ret;
    }

    public static void writeArray(byte[] b, ByteBuf buf) {
        writeVarInt(b.length, buf);
        buf.writeBytes(b);
    }

    public static byte[] readArray(ByteBuf buf) {
        byte[] ret = new byte[readVarInt(buf)];
        buf.readBytes(ret);
        return ret;
    }

    public static void writeStringArray(List<String> s, ByteBuf buf) {
        writeVarInt(s.size(), buf);
        for (String str : s) {
            writeString(str, buf);
        }
    }

    public static List<String> readStringArray(ByteBuf buf) {
        int len = readVarInt(buf);
        List<String> ret = new ArrayList<String>(len);
        for (int i = 0; i < len; i++) {
            ret.add(readString(buf));
        }
        return ret;
    }

    public static int readVarInt(ByteBuf input) {
        return readVarInt(input, 5);
    }

    public static int readVarInt(ByteBuf input, int maxBytes) {
        int out = 0;
        int bytes = 0;
        byte in;
        while (true) {
            in = input.readByte(); // error here
            out |= (in & 0x7F) << (bytes++ * 7);

            if (bytes > maxBytes) {
                throw new RuntimeException("VarInt too big");
            }
            if ((in & 0x80) != 0x80) {
                break;
            }

        }
        return out;
    }

    public static void writeVarInt(int value, ByteBuf output) {
        int part;
        while (true) {
            part = value & 0x7F;
            value >>>= 7;
            if (value != 0) {
                part |= 0x80;
            }
            output.writeByte(part);
            if (value == 0) {
                break;
            }
        }
    }

    public static int readVarShort(ByteBuf buf) {
        int low = buf.readUnsignedShort();
        int high = 0;
        if ((low & 0x8000) != 0) {
            low = low & 0x7FFF;
            high = buf.readUnsignedByte();
        }
        return ((high & 0xFF) << 15) | low;
    }

    public static void writeVarShort(ByteBuf buf, int toWrite) {
        int low = toWrite & 0x7FFF;
        int high = (toWrite & 0x7F8000) >> 15;
        if (high != 0) {
            low = low | 0x8000;
        }
        buf.writeShort(low);
        if (high != 0) {
            buf.writeByte(high);
        }
    }

    public void read(ByteBuf buf) {
        throw new UnsupportedOperationException("Packet must implement read method");
    }

    public void write(ByteBuf buf) {
        throw new UnsupportedOperationException("Packet must implement write method");
    }

    public abstract void handle(AbstractPacketHandler handler) throws Exception;

}

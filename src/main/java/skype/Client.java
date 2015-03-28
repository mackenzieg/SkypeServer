package skype;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.ReadTimeoutException;
import org.json.JSONException;
import org.json.JSONObject;
import skype.protocol.*;
import skype.protocol.packet.Kick;
import skype.protocol.packet.Ping;

import java.io.IOException;
import java.util.UUID;

public class Client extends ChannelInboundHandlerAdapter {

    private final SkypeManager skypeManager;
    private ChannelWrapper channelWrapper;
    private final PacketHandler packetHandler;
    private String name;
    private final UUID uuid;
    private Protocol.ProtocolData out;
    private final Ping ping = new Ping();
    private long lastPing = System.currentTimeMillis();
    private long connectionTime;
    private boolean registered = false;
    private boolean waitingReceipt = false;

    public Client(SkypeManager skypeManager, ChannelWrapper channelWrapper, UUID uuid, Protocol.ProtocolData out) {
        this.skypeManager = skypeManager;
        this.channelWrapper = channelWrapper;
        this.packetHandler = new PacketHandler(this);
        this.out = out;
        this.uuid = uuid;
        this.connectionTime = System.currentTimeMillis();
    }

    public SkypeManager getSkypeManager() {
        return skypeManager;
    }

    public boolean isWaitingReceipt() {
        return waitingReceipt;
    }

    public void setWaitingReceipt(boolean waitingReceipt) {
        this.waitingReceipt = waitingReceipt;
    }

    public boolean isRegistered() {
        return registered;
    }

    public void setRegistered(boolean isRegistered) {
        this.registered = isRegistered;
    }

    public void tick() {
        if (canPing()) {
            this.lastPing = System.currentTimeMillis();
            ping.setTime(System.currentTimeMillis());
            this.sendPacket(ping);
            waitingReceipt = true;
        } else {
            if (System.currentTimeMillis() - lastPing >= 100) {
                this.kick(this.uuid, this.registered ? SkypeConstants.CONNECTIONTIMEOUT : SkypeConstants.TOOLONGCONNECT);
            }
        }
    }

    public boolean canPing() {
        if (waitingReceipt || !registered)
            return false;
        return true;
    }

    public long getLastPing() {
        return lastPing;
    }

    public void setLastPing(long lastPing) {
        this.lastPing = lastPing;
    }

    public long getConnectionTime() {
        return connectionTime;
    }

    public void setConnectionTime(long connectionTime) {
        this.connectionTime = connectionTime;
    }

    public void cleanUp() {
        if (this.channelWrapper.isClosed())
            this.channelWrapper.close();
        this.skypeManager.clientDisconnect(this);
    }

    public void kick(UUID uuid, String reason) {
        Skype.log(this.getUuid() + " kicked reason: " + reason);
        Kick kick = (Kick) this.out.createPacket(4);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("uuid", uuid.toString());
            jsonObject.put("reason", reason);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        kick.setData(jsonObject.toString());
        this.sendPacket(kick);
        if (uuid.equals(this.uuid)) {
            this.cleanUp();
        }
    }

    public ChannelWrapper getChannelWrapper() {
        return channelWrapper;
    }

    public void sendPacket(DefinedPacket packet) {
        this.channelWrapper.write(packet);
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        channelWrapper = new ChannelWrapper(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        this.kick(this.uuid, SkypeConstants.CONNECTIONTIMEOUT);
        //TODO output a client has left
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        PacketWrapper packet = (PacketWrapper) msg;
        try {
            if (packet.packet != null) {
                packet.packet.handle(packetHandler);
            }
        } finally {
            packet.trySingleRelease();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (ctx.channel().isActive()) {
            if (cause instanceof ReadTimeoutException) {
                //TODO error logger remember the cause
            } else if (cause instanceof BadPacketException) {
                //TODO error logger remember the cause
            } else if (cause instanceof IOException) {
                //TODO error logger remember the cause
            } else {
                //TODO error logger remember the cause
            }
            ctx.close();
        }
    }


}

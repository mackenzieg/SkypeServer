package skype;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import skype.protocol.*;
import skype.protocol.packet.AudioChunk;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class SkypeManager extends ChannelInitializer<Channel> {

    private Protocol protocol;
    private ConcurrentLinkedQueue<Client> clients = new ConcurrentLinkedQueue<Client>();
    public volatile boolean running = true;

    public SkypeManager() {
        this.protocol = Protocol.DATA;
        this.loop();
    }

    public ConcurrentLinkedQueue<Client> getClients() {
        return clients;
    }

    public void loop() {

        Thread thread = new Thread() {
            @Override
            public void run() {
                while (running) {
                    for (Client client : clients) {
                        client.tick();
                    }
                    try {
                        Thread.sleep(20);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

        };
        thread.start();

    }

    public void handleAudio(Client sender, byte[] buff) {
        AudioChunk audioChunk = new AudioChunk();
        audioChunk.setData(buff);
        for(Client client : this.clients) {
            if(!client.equals(sender))
                client.sendPacket(audioChunk);
        }
    }

    public Client getClient(UUID uuid) {
        for (Client client : this.clients) {
            if (client.getUuid().equals(uuid))
                return client;
        }
        return null;
    }

    public void kickClient(Client client) {

    }

    public void clientDisconnect(Client client) {
        this.clients.remove(client);
    }

    @Override
    public void initChannel(Channel ch) throws Exception {
        Client client = new Client(this, new ChannelWrapper(ch), UUID.randomUUID(), protocol.OUTBOUND);
        ch.pipeline().addLast("frame_decoder", new Varint21FrameDecoder());
        ch.pipeline().addLast("packet_decoder", new Decoder(this.protocol));
        ch.pipeline().addLast("frame_encoder", new Varint21LengthFieldPrepender());
        ch.pipeline().addLast("packet_encoder", new Encoder(this.protocol));
        ch.pipeline().addLast("packet_handler", client);
        this.clients.add(client);
    }

}

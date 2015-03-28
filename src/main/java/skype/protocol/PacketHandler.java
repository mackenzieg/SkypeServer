package skype.protocol;

import org.json.JSONArray;
import org.json.JSONObject;
import skype.Client;
import skype.Skype;
import skype.protocol.packet.*;

import java.util.UUID;

public class PacketHandler extends AbstractPacketHandler {

    private Client client;

    public PacketHandler(Client client) {
        this.client = client;
    }

    @Override
    public void handle(Login login) throws Exception {
        ClientId clientId = new ClientId();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("uuid", this.client.getUuid().toString());
        clientId.setData(jsonObject.toString());
        this.client.sendPacket(clientId);
        ConnectedClient connectedClient = new ConnectedClient();
        JSONObject json;
        JSONArray jsonArray = new JSONArray();
        for (Client client : this.client.getSkypeManager().getClients()) {
            json = new JSONObject();
            json.put("name", client.getName());
            json.put("uuid", client.getUuid());
            jsonArray.put(json);
        }
        json = new JSONObject();
        json.put("users", (Object) jsonArray);
        connectedClient.setData(json.toString());
        this.client.sendPacket(connectedClient);
        this.client.setRegistered(true);
    }

    @Override
    public void handle(Disconnect disconnect) throws Exception {
        JSONObject jsonObject = new JSONObject(disconnect.getData());
        Client client = this.client.getSkypeManager().getClient(UUID.fromString(jsonObject.getString("uuid")));
        if(client.equals(this.client))
            this.client.cleanUp();
        Skype.log(client.getUuid().toString() + " disconnected reason: " + jsonObject.getString("reason"));
    }

    @Override
    public void handle(AudioChunk audioChunk) throws Exception {
        this.client.getSkypeManager().handleAudio(this.client, audioChunk.getData());
    }

    @Override
    public void handle(Ping ping) throws Exception {
        this.client.setWaitingReceipt(false);
        this.client.setLastPing(ping.getTime());
    }

}

package com.myname.packapunch.network;

import com.myname.packapunch.UpgradeConfig;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

public class ClientboundSyncConfigPacket {
    private final List<String> upgrades;

    public ClientboundSyncConfigPacket(List<String> upgrades) {
        this.upgrades = upgrades;
    }

    public static void encode(ClientboundSyncConfigPacket msg, FriendlyByteBuf buf) {
        buf.writeCollection(msg.upgrades, FriendlyByteBuf::writeUtf);
    }

    public static ClientboundSyncConfigPacket decode(FriendlyByteBuf buf) {
        return new ClientboundSyncConfigPacket(buf.readList(FriendlyByteBuf::readUtf));
    }

    public static void handle(ClientboundSyncConfigPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // This runs on the client main thread
            UpgradeConfig.syncFromServer(msg.upgrades);
        });
        ctx.get().setPacketHandled(true);
    }
}

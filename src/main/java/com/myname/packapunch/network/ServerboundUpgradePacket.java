package com.myname.packapunch.network;

import com.myname.packapunch.PackAPunchMod;
import com.myname.packapunch.blockentity.PackAPunchBlockEntity;
import com.myname.packapunch.menu.PackAPunchMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * ╔══════════════════════════════════════════════════════════╗
 * ║      SERVERBOUNDUPGRADEPACKET — CLIENT→SERVER PACKET    ║
 * ╚══════════════════════════════════════════════════════════╝
 */
@SuppressWarnings("null")
public class ServerboundUpgradePacket {

    private final BlockPos pos;

    public ServerboundUpgradePacket(BlockPos pos) {
        this.pos = pos;
    }

    public BlockPos getPos() {
        return pos;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
    }

    public static ServerboundUpgradePacket decode(FriendlyByteBuf buf) {
        return new ServerboundUpgradePacket(buf.readBlockPos());
    }

    public static void handle(ServerboundUpgradePacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) return;

            if (!(player.containerMenu instanceof PackAPunchMenu menu)) {
                PackAPunchMod.LOGGER.warn("[PackAPunch] {} sent upgrade packet but has no PackAPunchMenu open!", player.getName().getString());
                return;
            }

            if (!menu.getBlockPos().equals(msg.pos)) {
                PackAPunchMod.LOGGER.warn("[PackAPunch] {} sent upgrade packet for wrong position! Expected {}, got {}", player.getName().getString(), menu.getBlockPos(), msg.pos);
                return;
            }

            if (!(player.level().getBlockEntity(msg.pos) instanceof PackAPunchBlockEntity pap)) {
                PackAPunchMod.LOGGER.warn("[PackAPunch] No PackAPunchBlockEntity found at {} for player {}", msg.pos, player.getName().getString());
                return;
            }

            pap.tryUpgrade(player);
        });
        ctx.setPacketHandled(true);
    }
}

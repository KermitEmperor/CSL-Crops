package net.kermir.cslcrops.network;

import net.kermir.cslcrops.Cslcrops;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketChannel {
    private static SimpleChannel INSTANCE;

    private static int packetID = 0;
    private static int id() {
        return packetID++;
    }

    public static void register() {
        SimpleChannel net = NetworkRegistry.ChannelBuilder
                .named(new ResourceLocation(Cslcrops.MODID, "packetchannel"))
                .networkProtocolVersion(() -> "1.0")
                .clientAcceptedVersions(s -> true)
                .serverAcceptedVersions(s -> true)
                .simpleChannel();

        INSTANCE = net;

        net.messageBuilder(SyncDataPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(SyncDataPacket::encode)
                .decoder(SyncDataPacket::new)
                .consumerMainThread(SyncDataPacket::handle)
                .add();

        net.messageBuilder(ReqBlockTempData.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .encoder(ReqBlockTempData::encode)
                .decoder(ReqBlockTempData::new)
                .consumerMainThread(ReqBlockTempData::handle)
                .add();

        net.messageBuilder(RecBlockTempData.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(RecBlockTempData::encode)
                .decoder(RecBlockTempData::new)
                .consumerMainThread(RecBlockTempData::handle)
                .add();
    }


    public static <MSG> void sendToAllClients(MSG message) {
        INSTANCE.send(PacketDistributor.ALL.noArg(), message);
    }

    public static <MSG> void sendToClient(MSG message, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.sendToServer(message);
    }
}

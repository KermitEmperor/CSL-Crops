package net.kermir.cslcrops.network;

import net.kermir.cslcrops.data.CropData;
import net.kermir.cslcrops.data.CropsNSeedsData;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.function.Supplier;

public class SyncDataPacket {
    public HashMap<String , CropData> crop_map;
    public HashMap<String, String> seeds_list; // Seed resloc string, block/crop resloc string

    //TODO Not a TODO but a reminder, This packet can become too big

    public SyncDataPacket(HashMap<String , CropData> crop_map, HashMap<String, String> seeds_list) {
        this.crop_map = crop_map;
        this.seeds_list = seeds_list;
    }

    public SyncDataPacket(FriendlyByteBuf buf) {
        this.crop_map = (HashMap<String, CropData>) buf.readMap(FriendlyByteBuf::readUtf, buffer -> CropData.fromNBT(buffer.readNbt()));
        this.seeds_list = (HashMap<String, String>) buf.readMap(FriendlyByteBuf::readUtf, FriendlyByteBuf::readUtf);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeMap(crop_map, FriendlyByteBuf::writeUtf, ((friendlyByteBuf, data) -> friendlyByteBuf.writeNbt(data.serializeNBT())));
        buf.writeMap(seeds_list, FriendlyByteBuf::writeUtf, FriendlyByteBuf::writeUtf);
    }


    //TODO this packet
    @SuppressWarnings({"UnusedReturnValue", "unused"})
    public boolean handle(Supplier<NetworkEvent.Context> contextSupplier) {

        if (Minecraft.getInstance().player != null) {
            CropsNSeedsData.CROPS_MAP = this.crop_map;
            CropsNSeedsData.SEEDS_LIST = this.seeds_list;
        }

        return true;
    }
}

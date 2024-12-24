package net.kermir.cslcrops.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import net.kermir.cslcrops.Cslcrops;
import net.kermir.cslcrops.network.PacketChannel;
import net.kermir.cslcrops.network.SyncDataPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.util.LogicalSidedProvider;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.loading.FMLEnvironment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CropsNSeedsData extends SimpleJsonResourceReloadListener {
    public static HashMap<String , CropData> CROPS_MAP;
    public static HashMap<String, String> SEEDS_LIST; // Seed resloc string, block/crop resloc string
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    public static final CropsNSeedsData instance = new CropsNSeedsData();

    private CropsNSeedsData() {
        super(GSON, "csl_crops");
        CROPS_MAP = new HashMap<>();
        SEEDS_LIST = new HashMap<>();
    }


    @Override
    protected void apply(Map<ResourceLocation, JsonElement> elementMap, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        CROPS_MAP = new HashMap<>();
        SEEDS_LIST = new HashMap<>();
        elementMap.forEach((resourceLocation, jsonElement) -> {
            String savedAsName = resourceLocation.toString().split(":")[1].replace('/', ':');

            CropData cropData = new CropData(jsonElement);

            CROPS_MAP.put(savedAsName, cropData);
            if (cropData.getSeedItem() != null) SEEDS_LIST.put(cropData.getSeedItem().toString(), savedAsName);
            else Cslcrops.LOGGER.warn("NO VALID SEED ASSIGNED TO {}", savedAsName);

            Cslcrops.LOGGER.info("E: {} || {}", savedAsName, jsonElement);
        });

        //TODO this
        if (FMLEnvironment.dist == Dist.DEDICATED_SERVER)
            try {
                PacketChannel.sendToAllClients(new SyncDataPacket(CROPS_MAP, SEEDS_LIST));
            } catch (Exception e) {}
    }
}
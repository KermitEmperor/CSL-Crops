package net.kermir.cslcrops.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import net.kermir.cslcrops.Cslcrops;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonListener extends SimpleJsonResourceReloadListener {
    public static HashMap<String , CropData> CROPS_LIST;
    public static List<String> SEEDS_LIST;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    public static final JsonListener instance = new JsonListener();

    private JsonListener() {
        super(GSON, "csl_crops");
        CROPS_LIST = new HashMap<>();
        SEEDS_LIST = new ArrayList<>();
    }


    @Override
    protected void apply(Map<ResourceLocation, JsonElement> elementMap, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        CROPS_LIST = new HashMap<>();
        SEEDS_LIST = new ArrayList<>();
        elementMap.forEach((resourceLocation, jsonElement) -> {
            String savedAsName = resourceLocation.toString().split(":")[1].replace('/', ':');

            CropData cropData = new CropData(jsonElement);

            CROPS_LIST.put(savedAsName, cropData);
            if (cropData.getSeedItem() != null) SEEDS_LIST.add(cropData.getSeedItem().toString());
            else Cslcrops.LOGGER.warn("NO VALID SEED ASSIGNED TO {}", savedAsName);

            Cslcrops.LOGGER.info("E: {} || {}", savedAsName, jsonElement);
        });
    }
}
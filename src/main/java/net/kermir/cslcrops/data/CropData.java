package net.kermir.cslcrops.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import com.momosoftworks.coldsweat.api.util.Temperature;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class CropData implements INBTSerializable<CompoundTag> {
    private Temperature.Units type; //C or F
    private ResourceLocation transformCold;
    private ResourceLocation transformHot;//The Block to transform to when frozen
    private ResourceLocation seedItem;
    private Integer maxTemp; //above this temp the plant dies
    private Integer minTemp; //below this temp the plant freezes

    public CropData(JsonElement element) {
        JsonObject jsonObject = element.getAsJsonObject();
        this.maxTemp = jsonObject.has("max") ? jsonObject.get("max").getAsInt() : null;
        this.minTemp = jsonObject.has("min") ? jsonObject.get("min").getAsInt() : null;

        //Defaults to C because F makes no sense as a European :)

        if (jsonObject.has("type")) {
            String val = jsonObject.get("type").getAsString().toUpperCase();
            try {
                this.type = Temperature.Units.valueOf(val);
            } catch (Exception e) {
                this.type = Temperature.Units.C;
            }
        } else {
            this.type = Temperature.Units.C;
        }

        String supposedTransform = jsonObject.has("transforms_hot") ? jsonObject.get("transforms_hot").getAsString() : null;
        if (supposedTransform != null) {
            ResourceLocation location = new ResourceLocation(supposedTransform);
            //we do this to ensure we get air if the given block is invalid
            this.transformHot = ForgeRegistries.BLOCKS.getKey(ForgeRegistries.BLOCKS.getValue(location));
        } else {
            this.transformHot = null;
        }

        //We can reuse it tbh
        supposedTransform = jsonObject.has("transforms_cold") ? jsonObject.get("transforms_cold").getAsString() : null;
        if (supposedTransform != null) {
            ResourceLocation location = new ResourceLocation(supposedTransform);
            //we do this to ensure we get air if the given block is invalid
            this.transformCold = ForgeRegistries.BLOCKS.getKey(ForgeRegistries.BLOCKS.getValue(location));
        } else {
            this.transformCold = null;
        }

        String supposedSeed = jsonObject.has("seed") ? jsonObject.get("seed").getAsString() : null;
        if (supposedSeed != null) {
            ResourceLocation location = new ResourceLocation(supposedSeed);
            //we do this to ensure we don't get air
            ResourceLocation targetLoc = ForgeRegistries.ITEMS.getKey(ForgeRegistries.ITEMS.getValue(location));
            //noinspection DataFlowIssue
            if (!targetLoc.getPath().equals("air")) {
                this.seedItem = targetLoc;
            } else {
                this.seedItem = null;
            }
        } else {
            this.seedItem = null;
        }
    }

    public boolean isWarmer(double num, Temperature.Units unit) {
        return this.maxTemp != null && this.maxTemp < Temperature.convert(num, unit, this.type, true);
    }

    public boolean isColder(double num, Temperature.Units unit) {
        return this.minTemp != null && this.minTemp > Temperature.convert(num, unit, this.type, true);
    }

    public void onCold(double num, Temperature.Units unit, Consumer<ResourceLocation> consumer) {
        if (!(isColder(num, unit))) return;
        if (transformCold == null) return;
        consumer.accept(transformCold);
    }

    public void onHot(double num, Temperature.Units unit, Consumer<ResourceLocation> consumer) {
        if (!(isWarmer(num, unit))) return;
        if (transformHot == null) return;
        consumer.accept(transformHot);
    }

    //PLEASE ONLY USE IN RENDERING
    public @Nullable Integer getMaxTemp() {
        return maxTemp;
    }

    //PLEASE ONLY USE IN RENDERING
    public @Nullable Integer getMinTemp() {
        return minTemp;
    }

    public Temperature.Units getType() {
        return type;
    }

    public @Nullable ResourceLocation getSeedItem() {
        return seedItem;
    }


    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();

        nbt.putString("type", this.type.toString());
        if (this.transformCold != null) nbt.putString("transforms_cold", this.transformCold.toString());
        if (this.transformHot != null) nbt.putString("transforms_hot", this.transformHot.toString());
        if (this.seedItem != null) nbt.putString("seed", this.seedItem.toString());
        if (this.minTemp != null) nbt.putInt("min", this.minTemp);
        if (this.maxTemp != null) nbt.putInt("max", this.maxTemp);

        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        this.type = Temperature.Units.valueOf(nbt.getString("type").toUpperCase());

        if (nbt.contains("transforms_cold")) this.transformCold = new ResourceLocation(nbt.getString("transforms_cold"));
        if (nbt.contains("transforms_hot")) this.transformHot = new ResourceLocation(nbt.getString("transforms_hot"));
        if (nbt.contains("seed")) this.seedItem = new ResourceLocation(nbt.getString("seed"));

        if (nbt.contains("min")) this.minTemp = nbt.getInt("min");
        if (nbt.contains("max")) this.maxTemp = nbt.getInt("max");
    }

    public static CropData fromNBT(CompoundTag tag) {
        return new CropData(NbtOps.INSTANCE.convertTo(JsonOps.INSTANCE, tag));
    }
}

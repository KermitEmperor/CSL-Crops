package net.kermir.cslcrops.network;

import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.config.spec.ClientSettingsConfig;
import net.kermir.cslcrops.compat.jade.CropComponentProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class RecBlockTempData {
    public BlockPos cropPos;
    public double temperature;

    public RecBlockTempData(BlockPos cropPos, double temperature) {
        this.cropPos = cropPos;
        this.temperature = temperature;
    }

    public RecBlockTempData(FriendlyByteBuf buf) {
        this.cropPos = buf.readBlockPos();
        this.temperature = buf.readDouble();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.cropPos);
        buf.writeDouble(this.temperature);
    }

    @SuppressWarnings({"UnusedReturnValue", "unused"})
    public boolean handle(Supplier<NetworkEvent.Context> contextSupplier) {

        if (!FMLEnvironment.dist.isClient()) return true;

        CropComponentProvider.curTemperature = (int) Math.round(Temperature.convert(this.temperature, Temperature.Units.MC, ClientSettingsConfig.USE_CELSIUS.get() ? Temperature.Units.C : Temperature.Units.F, true));
        CropComponentProvider.curBlockPos = this.cropPos;

        return true;
    }
}
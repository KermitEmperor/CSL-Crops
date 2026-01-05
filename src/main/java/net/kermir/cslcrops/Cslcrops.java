package net.kermir.cslcrops;

import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.util.world.WorldHelper;
import net.kermir.cslcrops.data.CropData;
import net.kermir.cslcrops.data.CropsNSeedsData;
import net.kermir.cslcrops.network.PacketChannel;
import net.kermir.cslcrops.network.SyncDataPacket;
import net.kermir.cslcrops.tooltip.ClientTempTooltipComponent;
import net.kermir.cslcrops.tooltip.TempTooltipComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.SaplingGrowTreeEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Cslcrops.MODID)
public class Cslcrops {

    // Define mod id in a common place for everything to reference
    public static final String MODID = "cslcrops";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
    public Cslcrops() {

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerTooltip);

        IEventBus ForgeEventBus = MinecraftForge.EVENT_BUS;
        ForgeEventBus.register(this);
        ForgeEventBus.addListener(this::onTooltip);
        ForgeEventBus.addListener(this::onCropGrowth);
        ForgeEventBus.addListener(this::onTreeGrowth);
        ForgeEventBus.addListener(this::onPlayerJoin);
        ForgeEventBus.addListener(this::onPlayerLeave);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        PacketChannel.register();
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            // Some client setup code
            LOGGER.info("HELLO FROM CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        }

    }

    @SuppressWarnings("DataFlowIssue")
    public void onTooltip(RenderTooltipEvent.GatherComponents event) {

        String resLoc = ForgeRegistries.ITEMS.getKey(event.getItemStack().getItem()).toString();
        if (CropsNSeedsData.SEEDS_LIST.containsKey(resLoc)) {
            CropData data = CropsNSeedsData.CROPS_MAP.get(CropsNSeedsData.SEEDS_LIST.get(resLoc));
            event.getTooltipElements().add(1,Either.right(new TempTooltipComponent(data)));
        }
    }

    public void registerTooltip(RegisterClientTooltipComponentFactoriesEvent event) {

        //Factory Design sucks ass
        event.register(TempTooltipComponent.class, ClientTempTooltipComponent::new);
    }

    @SuppressWarnings("DataFlowIssue")
    public void onCropGrowth(BlockEvent.CropGrowEvent.Pre event) {
        if (event.getLevel() != null) {
            String blockResLoc = ForgeRegistries.BLOCKS.getKey(event.getState().getBlock()).toString();

            onTreeAndPlant((Level) event.getLevel(), blockResLoc ,event.getPos(), event);
        }
    }

    @SuppressWarnings("DataFlowIssue")
    public void onTreeGrowth(SaplingGrowTreeEvent event) {
        if (event.getLevel() != null) {
            String blockResLoc = ForgeRegistries.BLOCKS.getKey(event.getLevel().getBlockState(event.getPos()).getBlock()).toString();

            onTreeAndPlant((Level) event.getLevel(), blockResLoc ,event.getPos(), event);
        }
    }


    @SuppressWarnings("DataFlowIssue")
    private void onTreeAndPlant(Level level, String blockResLoc, BlockPos blockPos, Event event) {
        if (CropsNSeedsData.CROPS_MAP.containsKey(blockResLoc)) {
            double temp = WorldHelper.getTemperatureAt(level, blockPos);
            CropData data = CropsNSeedsData.CROPS_MAP.get(blockResLoc);

            if (data.isColder(temp, Temperature.Units.MC)) event.setResult(Event.Result.DENY);
            data.onCold(temp, Temperature.Units.MC, (resourceLocation ->
                level.setBlock(blockPos, ForgeRegistries.BLOCKS.getValue(resourceLocation).defaultBlockState(), 2)
            ));

            if (data.isWarmer(temp, Temperature.Units.MC)) event.setResult(Event.Result.DENY);
            data.onHot(temp, Temperature.Units.MC, (resourceLocation ->
                level.setBlock(blockPos, ForgeRegistries.BLOCKS.getValue(resourceLocation).defaultBlockState(), 2)
            ));
        }
    }

    private void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (FMLEnvironment.dist == Dist.DEDICATED_SERVER)
            PacketChannel.sendToClient(new SyncDataPacket(CropsNSeedsData.CROPS_MAP, CropsNSeedsData.SEEDS_LIST), (ServerPlayer) event.getEntity());
    }

    private void onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent event) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            //Its to save memory but is it really necessary
            Cslcrops.LOGGER.info("Clearing unneeded data");
            CropsNSeedsData.CROPS_MAP.clear();
            CropsNSeedsData.SEEDS_LIST.clear();
        }
    }

    @SubscribeEvent
    public void jsonReaiding(AddReloadListenerEvent event) {
        event.addListener(CropsNSeedsData.instance);
    }
}

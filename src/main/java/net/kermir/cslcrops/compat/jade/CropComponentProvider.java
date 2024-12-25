package net.kermir.cslcrops.compat.jade;

import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.config.spec.ClientSettingsConfig;
import net.kermir.cslcrops.Cslcrops;
import net.kermir.cslcrops.data.CropData;
import net.kermir.cslcrops.data.CropsNSeedsData;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.registries.ForgeRegistries;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.Element;
import snownee.jade.api.ui.IElement;

public class CropComponentProvider implements IBlockComponentProvider {
    public static CropComponentProvider INSTANCE = new CropComponentProvider();
    public static final ResourceLocation WIDGETS = new ResourceLocation(Cslcrops.MODID, "textures/gui/tooltip/widgets.png");
    public static IElement gauge = new Element() {
        @Override
        public Vec2 getSize() {
            return new Vec2(8,9);
        }

        @Override
        public void render(GuiGraphics guiGraphics, float X, float Y, float U, float V) {
            guiGraphics.blit(WIDGETS, (int)X+1, (int)Y, 0, 12, 0, 5, 9, 32, 24);
        }
    };
    public static IElement cold = new Element() {
        @Override
        public Vec2 getSize() {
            return new Vec2(8,6);
        }

        @Override
        public void render(GuiGraphics guiGraphics, float X, float Y, float U, float V) {
            guiGraphics.blit(WIDGETS, (int) X, (int)Y + 1, 0, 0, 0, 6, 6, 32, 24);
        }
    };
    public static IElement hot = new Element() {
        @Override
        public Vec2 getSize() {
            return new Vec2(6,6);
        }

        @Override
        public void render(GuiGraphics guiGraphics, float X, float Y, float U, float V) {
            guiGraphics.blit(WIDGETS, (int) X, (int)Y + 1, 0, 6, 0, 6, 6, 32, 24);
        }
    };

    @Override
    public void appendTooltip(ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
        @SuppressWarnings("DataFlowIssue") String blockResLoc = ForgeRegistries.BLOCKS.getKey(blockAccessor.getBlock()).toString();

        if (CropsNSeedsData.CROPS_MAP.containsKey(blockResLoc) && blockAccessor.getLevel() != null) {
            int temperature = (int) Math.round(Temperature.convert(Temperature.getTemperatureAt(blockAccessor.getPosition(), blockAccessor.getLevel()), Temperature.Units.MC, ClientSettingsConfig.USE_CELSIUS.get() ? Temperature.Units.C : Temperature.Units.F, true));
            String tempType = ClientSettingsConfig.USE_CELSIUS.get() ? "°C" : "°F";
            CropData data = CropsNSeedsData.CROPS_MAP.get(blockResLoc);

            ChatFormatting formatting = ChatFormatting.GREEN;

            if (data.getMinTemp() != null && quickConvert(data.getMinTemp(), data.getType()) > temperature) formatting = ChatFormatting.AQUA;
            if (data.getMaxTemp() != null && quickConvert(data.getMaxTemp(), data.getType()) < temperature) formatting = ChatFormatting.RED;


            iTooltip.add(Component.literal(String.format("%s", temperature)).withStyle(formatting));
            iTooltip.append(gauge);

            if (data.getMinTemp() != null) {
                int minTemp = Mth.floor(quickConvert(data.getMinTemp(), data.getType()));
                String textCold = String.format("%s", minTemp);
                iTooltip.append(Component.literal(textCold).withStyle(Style.EMPTY.withColor(0x3B81CC)));
                iTooltip.append(cold);
            }

            if (data.getMaxTemp() != null) {
                int maxTemp = Mth.ceil(quickConvert(data.getMaxTemp(), data.getType()));
                String textHot = String.format("%s", maxTemp);
                iTooltip.append(Component.literal(textHot).withStyle(Style.EMPTY.withColor(0xD65F37)));
                iTooltip.append(hot);
            }

            iTooltip.append(Component.literal(" "+tempType).withStyle(ChatFormatting.WHITE));

        }
    }

    private double quickConvert(double temp, Temperature.Units from) {
        return Temperature.convert(temp, from, ClientSettingsConfig.USE_CELSIUS.get() ? Temperature.Units.C : Temperature.Units.F, true);
    }

    @Override
    public ResourceLocation getUid() {
        return new ResourceLocation(Cslcrops.MODID, "temp_data");
    }

    @Override
    public int getDefaultPriority() {
        return 1;
    }
}

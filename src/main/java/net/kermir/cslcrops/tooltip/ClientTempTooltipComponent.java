package net.kermir.cslcrops.tooltip;

import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.config.spec.ClientSettingsConfig;
import net.kermir.cslcrops.Cslcrops;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

@SuppressWarnings("NullableProblems")
public class ClientTempTooltipComponent implements ClientTooltipComponent {
    private String textCold = "";
    private int textColdLength = 0;
    private String textHot = "";
    private int textHotLength = 0;
    private final Minecraft mc;

    public static final ResourceLocation WIDGETS = new ResourceLocation(Cslcrops.MODID, "textures/gui/tooltip/widgets.png");

    public ClientTempTooltipComponent(TempTooltipComponent tempTooltipComponent) {
        mc = Minecraft.getInstance();

        String tempType = ClientSettingsConfig.USE_CELSIUS.get() ? "°C" : "°F";

        if (tempTooltipComponent.minTemp != null) {
            int minTemp = Mth.ceil(Temperature.convert(tempTooltipComponent.minTemp, tempTooltipComponent.type, ClientSettingsConfig.USE_CELSIUS.get() ? Temperature.Units.C : Temperature.Units.F, true));
            textCold = String.format("%s %s", minTemp, tempType);
            textColdLength = mc.font.width(textCold);
        }

        if (tempTooltipComponent.maxTemp != null) {
            int maxTemp = Mth.floor(Temperature.convert(tempTooltipComponent.maxTemp, tempTooltipComponent.type, ClientSettingsConfig.USE_CELSIUS.get() ? Temperature.Units.C : Temperature.Units.F, true));
            textHot = String.format("%s %s", maxTemp, tempType);
            textHotLength = mc.font.width(textHot);
        }
    }

    @Override
    public int getHeight() {
        return Minecraft.getInstance().font.lineHeight+1;
    }

    @Override
    public int getWidth(Font font) {
        //the number 25 was choosen by the councill
        return this.textColdLength+this.textHotLength+25;
    }

    @Override
    public void renderText(@NotNull Font pFont, int pMouseX, int pMouseY, Matrix4f pMatrix, MultiBufferSource.BufferSource pBufferSource) {
        ClientTooltipComponent.super.renderText(pFont, pMouseX, pMouseY, pMatrix, pBufferSource);
        //pFont.drawInBatch(this.text, (float)pMouseX, (float)pMouseY, -1, true, pMatrix, pBufferSource, Font.DisplayMode.NORMAL, 0, 15728880);
    }

    @Override
    public void renderImage(Font pFont, int pX, int pY, GuiGraphics pGuiGraphics) {
        pGuiGraphics.blit(WIDGETS, pX, pY + 1, 0, 0, 0, 6, 6, 32, 24);
        pGuiGraphics.drawString(mc.font, textCold, pX + 8, pY + 1, 0x3B81CC);
        pGuiGraphics.blit(WIDGETS, pX+10+textColdLength, pY, 0, 12, 0, 5, 9, 32, 24);
        pGuiGraphics.drawString(mc.font, textHot, pX + 10 + textColdLength + 8, pY + 1, 0xD65F37);
        pGuiGraphics.blit(WIDGETS, pX+10+textColdLength+8+textHotLength+1, pY+1, 0, 6, 0, 6, 6, 32, 24);
    }
}

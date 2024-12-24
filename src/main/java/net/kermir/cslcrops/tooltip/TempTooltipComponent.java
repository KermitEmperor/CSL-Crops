package net.kermir.cslcrops.tooltip;

import com.momosoftworks.coldsweat.api.util.Temperature;
import net.kermir.cslcrops.data.CropData;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

public class TempTooltipComponent implements TooltipComponent {
    public Temperature.Units type; //C or F
    public Integer maxTemp; //above this temp the plant dies
    public Integer minTemp; //below this temp the plant freezes

    public TempTooltipComponent(CropData data) {
        this.type = data.getType();
        this.maxTemp = data.getMaxTemp();
        this.minTemp = data.getMinTemp();
    }
}

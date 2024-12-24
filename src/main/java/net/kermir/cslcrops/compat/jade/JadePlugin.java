package net.kermir.cslcrops.compat.jade;

import net.kermir.cslcrops.Cslcrops;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class JadePlugin implements IWailaPlugin {
    @Override
    public void register(IWailaCommonRegistration registration) {
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(CropComponentProvider.INSTANCE, Block.class);
    }
}

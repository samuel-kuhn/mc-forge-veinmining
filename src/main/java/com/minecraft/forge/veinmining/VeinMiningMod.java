package com.minecraft.forge.veinmining;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

@Mod(VeinMiningMod.MOD_ID)
public class VeinMiningMod {
    public static final String MOD_ID = "veinmining";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static void logInfo(String message) {
        LOGGER.info(message);
    }

    public VeinMiningMod() {
        MinecraftForge.EVENT_BUS.register(this);
    }
}
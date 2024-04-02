package com.gregtechceu.gtceu;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.client.ClientProxy;
import com.gregtechceu.gtceu.common.CommonProxy;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.Platform;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.DistExecutor;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(GTCEu.MOD_ID)
public class GTCEu {
    public static final String MOD_ID = "gtceu";
    public static final String NAME = "GregTechCEu";
    public static final Logger LOGGER = LoggerFactory.getLogger(NAME);

    public GTCEu(IEventBus modBus) {
        GTCEu.init();
        GTCEuAPI.instance = this;
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientProxy.init();
        }
        new CommonProxy(modBus);
    }

    public static void init() {
        LOGGER.info("{} is initializing on platform: {}", NAME, Platform.platformName());
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, FormattingUtil.toLowerCaseUnder(path));
    }

    public static String appendIdString(String id) {
        return id.indexOf(':') == -1 ? (MOD_ID + ":" + id) : id;
    }

    public static ResourceLocation appendId(String id) {
        String[] strings = new String[]{"gtceu", id};
        int i = id.indexOf(':');
        if (i >= 0) {
            strings[1] = id.substring(i + 1);
            if (i >= 1) {
                strings[0] = id.substring(0, i);
            }
        }
        return new ResourceLocation(strings[0], strings[1]);
    }

    public static boolean isKubeJSLoaded() {
        return LDLib.isModLoaded(GTValues.MODID_KUBEJS);
    }

    public static boolean isCreateLoaded() {
        return LDLib.isModLoaded(GTValues.MODID_CREATE);
    }

    public static boolean isIrisOculusLoaded() {
        return LDLib.isModLoaded(GTValues.MODID_IRIS) || LDLib.isModLoaded(GTValues.MODID_OCULUS);
    }

    public static boolean isSodiumRubidiumEmbeddiumLoaded() {
        return LDLib.isModLoaded(GTValues.MODID_SODIUM) || LDLib.isModLoaded(GTValues.MODID_RUBIDIUM) ||LDLib.isModLoaded(GTValues.MODID_EMBEDDIUM);
    }

    public static boolean isRebornEnergyLoaded() {
        return Platform.isForge() || LDLib.isModLoaded(GTValues.MODID_REBORN_ENERGY);
    }

    public static boolean isAE2Loaded() {
        return LDLib.isModLoaded(GTValues.MODID_APPENG);
    }

    public static boolean isAlmostUnifiedLoaded() {
        return LDLib.isModLoaded(GTValues.MODID_ALMOSTUNIFIED);
    }

    @Deprecated(forRemoval = true, since = "1.0.21")
    public static boolean isHighTier() {
        return GTCEuAPI.isHighTier();
    }
}

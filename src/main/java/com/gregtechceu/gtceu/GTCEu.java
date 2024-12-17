package com.gregtechceu.gtceu;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.client.ClientProxy;
import com.gregtechceu.gtceu.common.CommonProxy;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.Platform;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(GTCEu.MOD_ID)
public class GTCEu {

    public static final String MOD_ID = "gtceu";
    public static final String NAME = "GregTechCEu";
    public static final Logger LOGGER = LoggerFactory.getLogger(NAME);

    public GTCEu() {
        GTCEu.init();
        GTCEuAPI.instance = this;
        DistExecutor.unsafeRunForDist(() -> ClientProxy::new, () -> CommonProxy::new);
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
        String[] strings = new String[] { "gtceu", id };
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

    public static boolean isIrisOculusLoaded() {
        return LDLib.isModLoaded(GTValues.MODID_IRIS) || LDLib.isModLoaded(GTValues.MODID_OCULUS);
    }

    public static boolean isSodiumRubidiumEmbeddiumLoaded() {
        return LDLib.isModLoaded(GTValues.MODID_SODIUM) || LDLib.isModLoaded(GTValues.MODID_RUBIDIUM) ||
                LDLib.isModLoaded(GTValues.MODID_EMBEDDIUM);
    }

    public static boolean isAE2Loaded() {
        return LDLib.isModLoaded(GTValues.MODID_APPENG);
    }

    public static boolean isCuriosLoaded() {
        return LDLib.isModLoaded(GTValues.MODID_CURIOS);
    }

    public static boolean isShimmerLoaded() {
        return LDLib.isModLoaded(GTValues.MODID_SHIMMER);
    }

    public static boolean isJAVDLoaded() {
        return LDLib.isModLoaded(GTValues.MODID_JAVD);
    }

    public static boolean isFTBTeamsLoaded() {
        return LDLib.isModLoaded(GTValues.MODID_FTB_TEAMS);
    }

    public static boolean isArgonautsLoaded() {
        return LDLib.isModLoaded(GTValues.MODID_ARGONAUTS);
    }

    @Deprecated(forRemoval = true, since = "1.0.21")
    public static boolean isHighTier() {
        return GTCEuAPI.isHighTier();
    }
}

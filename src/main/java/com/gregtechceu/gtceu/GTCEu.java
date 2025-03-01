package com.gregtechceu.gtceu;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.client.ClientProxy;
import com.gregtechceu.gtceu.common.CommonProxy;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.server.ServerLifecycleHooks;

import dev.emi.emi.config.EmiConfig;
import me.shedaniel.rei.api.client.REIRuntime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

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
        LOGGER.info("{} is initializing...", NAME);
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

    /**
     * @return if we're running in a production environment
     */
    public static boolean isProd() {
        return FMLLoader.isProduction();
    }

    /**
     * @return if we're not running in a production environment
     */
    public static boolean isDev() {
        return !isProd();
    }

    /**
     * @return if we're running data generation
     */
    public static boolean isDataGen() {
        return FMLLoader.getLaunchHandler().isData();
    }

    /**
     * A friendly reminder that the server instance is populated on the server side only, so null/side check it!
     * 
     * @return the current minecraft server instance
     */
    public static MinecraftServer getMinecraftServer() {
        return ServerLifecycleHooks.getCurrentServer();
    }

    /**
     * @param modId the mod id to check for
     * @return if the mod whose id is {@code modId} is loaded or not
     */
    public static boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    /**
     * For async stuff use this, otherwise use {@link GTCEu isClientSide}
     * 
     * @return if the current thread is the client thread
     */
    public static boolean isClientThread() {
        return isClientSide() && Minecraft.getInstance().isSameThread();
    }

    /**
     * @return if the FML environment is a client
     */
    public static boolean isClientSide() {
        return FMLEnvironment.dist.isClient();
    }

    /**
     * This check isn't the same for client and server!
     * 
     * @return if it's safe to access the current instance {@link net.minecraft.world.level.Level Level} on client or if
     *         it's safe to access any level on server.
     */
    public static boolean canGetServerLevel() {
        if (isClientSide()) {
            return Minecraft.getInstance().level != null;
        }
        var server = getMinecraftServer();
        return server != null &&
                !(server.isStopped() || server.isShutdown() || !server.isRunning() || server.isCurrentlySaving());
    }

    /**
     * @return the path to the minecraft instance directory
     */
    public static Path getGameDir() {
        return FMLPaths.GAMEDIR.get();
    }

    public static class Mods {

        public static boolean isJEILoaded() {
            return !(isModLoaded(GTValues.MODID_EMI) || isModLoaded(GTValues.MODID_REI)) &&
                    isModLoaded(GTValues.MODID_JEI);
        }

        public static boolean isREILoaded() {
            return isModLoaded(GTValues.MODID_REI) && (!isClientSide() || REIRuntime.getInstance().isOverlayVisible());
        }

        public static boolean isEMILoaded() {
            return isModLoaded(GTValues.MODID_EMI) && (!isClientSide() || EmiConfig.enabled);
        }

        public static boolean isKubeJSLoaded() {
            return isModLoaded(GTValues.MODID_KUBEJS);
        }

        public static boolean isIrisOculusLoaded() {
            return isModLoaded(GTValues.MODID_IRIS) || isModLoaded(GTValues.MODID_OCULUS);
        }

        public static boolean isSodiumRubidiumEmbeddiumLoaded() {
            return isModLoaded(GTValues.MODID_SODIUM) || isModLoaded(GTValues.MODID_RUBIDIUM) ||
                    isModLoaded(GTValues.MODID_EMBEDDIUM);
        }

        public static boolean isAE2Loaded() {
            return isModLoaded(GTValues.MODID_APPENG);
        }

        public static boolean isCuriosLoaded() {
            return isModLoaded(GTValues.MODID_CURIOS);
        }

        public static boolean isShimmerLoaded() {
            return isModLoaded(GTValues.MODID_SHIMMER);
        }

        public static boolean isJAVDLoaded() {
            return isModLoaded(GTValues.MODID_JAVD);
        }

        public static boolean isFTBTeamsLoaded() {
            return isModLoaded(GTValues.MODID_FTB_TEAMS);
        }

        public static boolean isArgonautsLoaded() {
            return isModLoaded(GTValues.MODID_ARGONAUTS);
        }
    }
}

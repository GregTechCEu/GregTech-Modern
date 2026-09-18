package brachy.modularui;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.factory.UIFactories;
import brachy.modularui.factory.inventory.InventoryTypes;
import brachy.modularui.network.ModularNetwork;
import brachy.modularui.screen.ModularContainerMenu;
import brachy.modularui.test.TestRegistration;
import brachy.modularui.theme.ThemeManager;
import brachy.modularui.utils.RegistryAccessContainer;

import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import com.mojang.brigadier.Command;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.fml.util.thread.EffectiveSide;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.data.loading.DatagenModLoader;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.function.Predicate;

@Mod(ModularUI.MOD_ID)
public class ModularUI {

    public static final String MOD_ID = "modularui";
    public static final String NAME = "Modular UI";
    public static final Logger LOGGER = LogManager.getLogger(NAME);
    public static final boolean UNIT_TEST = Boolean.getBoolean("unit.testing");

    private static final Identifier TEMPLATE_LOCATION = Identifier.fromNamespaceAndPath(MOD_ID, "");

    public ModularUI(IEventBus modBus, ModContainer modContainer) {
        // uncomment if mod bus event listeners are added to this class
        // modBus.register(this);
        IEventBus forgeBus = NeoForge.EVENT_BUS;
        forgeBus.addListener(this::onRegisterDataReloadListener);
        forgeBus.addListener(this::onTick);
        forgeBus.addListener(this::onRegisterCommand);
        forgeBus.addListener(this::onPlayerLeave);

        modContainer.registerConfig(ModConfig.Type.CLIENT, ModularUIConfig.CONFIG, ModularUI.MOD_ID + ".toml");

        /* MUI Initialization */
        UIFactories.init();
        InventoryTypes.init();

        ModularUIMenuTypes.register(modBus);
        if (ModularUI.isDev()) {
            TestRegistration.register(modBus);
        }
    }

    public static Identifier id(String path) {
        return TEMPLATE_LOCATION.withPath(path);
    }

    /**
     * @return whether we're running in a production environment
     */
    public static boolean isProd() {
        return FMLLoader.isProduction();
    }

    /**
     * @return whether we're not running in a production environment
     */
    public static boolean isDev() {
        return !isProd();
    }

    public static boolean isTestEnv() {
        return UNIT_TEST;
    }

    /**
     * @return if we're running data generation
     */
    public static boolean isDataGen() {
        return DatagenModLoader.isRunningDataGen();
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
     * For async stuff use this, otherwise use {@link #isClientSide}
     *
     * @return if the current thread is the client thread
     * @see #isClientSide()
     */
    @SuppressWarnings("ConstantValue")
    public static boolean isClientThread() {
        return isTestEnv() || (isClientSide() && EffectiveSide.get().isClient());
    }

    /**
     * @return if the game is the <strong>PHYSICAL</strong> client, e.g. not a dedicated server.
     * @apiNote Do not use this to check if you're currently on the server thread for side-specific actions!
     * It does <strong>NOT</strong> work for that. Use {@link #isClientThread()} instead.
     * @see #isClientThread()
     */
    public static boolean isClientSide() {
        return isTestEnv() || FMLEnvironment.dist.isClient();
    }

    /**
     * This check isn't the same for client and server!
     *
     * @return if it's safe to access the current instance {@link net.minecraft.world.level.Level Level} on client or if
     * it's safe to access any level on server.
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

    private void onTick(PlayerTickEvent.Post event) {
        if (event.getEntity().containerMenu instanceof ModularContainerMenu containerMenu) {
            containerMenu.onUpdate();
        }
    }

    private void onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!ModularUI.isClientSide()) {
            ModularNetwork.SERVER.onPlayerLeave(event.getEntity());
        }
    }

    private void onRegisterDataReloadListener(AddReloadListenerEvent event) {
        RegistryAccessContainer.update(event.getRegistryAccess(), event.getConditionContext());
    }

    private void onRegisterCommand(RegisterCommandsEvent event) {
        var command = Commands.literal("mui")
                .then(Commands.literal("reload_themes")
                        .executes(ctx -> {
                            ThemeManager.reload();
                            // TODO translations for this
                            ctx.getSource().sendSuccess(() -> Component.literal("ModularUI Themes reloaded").withStyle(Text.GREEN), true);
                            return Command.SINGLE_SUCCESS;
                        }));
        event.getDispatcher().register(command);
    }

    public enum Mods {

        //BLUR(ModIds.BLUR),
        //BOGOSORTER(ModIds.BOGOSORTER),
        CURIOS(ModIds.CURIOS),
        EMI(ModIds.EMI),
        JEI(ModIds.JEI),
        REI(ModIds.REI),
        //MODNAMETOOLTIP(ModIds.MODNAMETOOLTIP),
        //NEA(ModIds.NEA),
        SODIUM(ModIds.SODIUM),
        IRIS(ModIds.IRIS);

        public static boolean isSodiumLikeLoaded() {
            return SODIUM.isLoaded();
        }

        public static boolean isIrisLikeLoaded() {
            return IRIS.isLoaded();
        }

        public static boolean isRecipeViewerLoaded() {
            return JEI.isLoaded() || EMI.isLoaded() || REI.isLoaded();
        }

        public final String id;
        private boolean loaded = false;
        private boolean initialized = false;
        private final Predicate<ModContainer> extraLoadedCheck;

        Mods(String id) {
            this(id, null);
        }

        Mods(String id, @Nullable Predicate<ModContainer> extraLoadedCheck) {
            this.id = id;
            this.extraLoadedCheck = extraLoadedCheck;
        }

        public boolean isLoaded() {
            if (!this.initialized) {
                var modContainer = ModList.get().getModContainerById(this.id);
                this.loaded = modContainer.isPresent();
                if (this.loaded && this.extraLoadedCheck != null) {
                    this.loaded = this.extraLoadedCheck.test(modContainer.get());
                }
                this.initialized = true;
            }
            return this.loaded;
        }
    }

    public static class ModIds {

        public static final String BLUR = "blur";
        public static final String BOGOSORTER = "bogosorter";
        public static final String CURIOS = "curios";
        public static final String EMI = "emi";
        public static final String JEI = "jei";
        public static final String REI = "roughlyenoughitems";
        public static final String MODNAMETOOLTIP = "modnametooltip";
        public static final String IRIS = "iris";
        public static final String SODIUM = "sodium";
    }
}

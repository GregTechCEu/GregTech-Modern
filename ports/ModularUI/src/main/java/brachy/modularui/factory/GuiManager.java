package brachy.modularui.factory;

import brachy.modularui.ModularUI;
import brachy.modularui.api.IMuiScreen;
import brachy.modularui.api.MCHelper;
import brachy.modularui.api.RecipeViewerSettings;
import brachy.modularui.api.UIFactory;
import brachy.modularui.core.extensions.IRegistryFriendlyByteBufExtension;
import brachy.modularui.core.mixins.common.ServerPlayerAccessor;
import brachy.modularui.network.ModularNetwork;
import brachy.modularui.network.packets.OpenGuiPacket;
import brachy.modularui.screen.ContainerScreenWrapper;
import brachy.modularui.screen.ModularContainerMenu;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.ModularScreen;
import brachy.modularui.screen.ScreenWrapper;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.ModularSyncManager;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.WidgetTree;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

@EventBusSubscriber(modid = ModularUI.MOD_ID)
public class GuiManager {

    private static final Object2ObjectMap<Identifier, UIFactory<?>> FACTORIES = new Object2ObjectOpenHashMap<>(
            16);

    private static final List<Player> openedContainers = new ArrayList<>(4);

    public static void registerFactory(UIFactory<?> factory) {
        Objects.requireNonNull(factory);
        Identifier name = Objects.requireNonNull(factory.getFactoryName());
        if (FACTORIES.containsKey(name)) {
            throw new IllegalArgumentException("Factory with name '" + name + "' is already registered!");
        }
        FACTORIES.put(name, factory);
    }

    public static @NotNull UIFactory<?> getFactory(Identifier name) {
        UIFactory<?> factory = FACTORIES.get(name);
        if (factory == null) throw new NoSuchElementException("No UI factory for name '" + name + "' found!");
        return factory;
    }

    public static boolean hasFactory(Identifier name) {
        return FACTORIES.containsKey(name);
    }

    public static <T extends GuiData> void open(@NotNull UIFactory<T> factory, @NotNull T guiData, ServerPlayer player) {
        if (player instanceof FakePlayer || openedContainers.contains(player)) return;
        openedContainers.add(player);
        // create panel, collect sync handlers and create menu
        UISettings settings = new UISettings(RecipeViewerSettings.DUMMY);
        settings.defaultCanInteractWith(factory, guiData);
        ModularSyncManager msm = new ModularSyncManager(false);
        PanelSyncManager syncManager = new PanelSyncManager(msm, true);
        ModularPanel<?> panel = factory.createPanel(guiData, syncManager, settings);
        WidgetTree.collectSyncValues(syncManager, panel);
        guiData.addSyncHandlers(syncManager);

        // create the menu
        ((ServerPlayerAccessor) player).invokeNextContainerCounter();
        if (player.containerMenu != player.inventoryMenu) {
            player.closeContainer();
        }
        int windowId = ((ServerPlayerAccessor) player).getContainerCounter();
        ModularContainerMenu menu = settings.hasCustomContainer() ? settings.createContainer(windowId) : factory.createContainer(windowId);
        menu.construct(player, msm, settings, panel.getName(), guiData);

        // sync to client
        RegistryFriendlyByteBuf buffer = IRegistryFriendlyByteBufExtension.createEmpty(player.registryAccess());
        factory.writeGuiData(guiData, buffer);
        int nid = ModularNetwork.SERVER.activate(player, msm);
        PacketDistributor.sendToPlayer(player, new OpenGuiPacket<>(windowId, nid, factory, buffer));
        // open the menu // this mimics forge behaviour
        ((ServerPlayerAccessor) player).invokeInitMenu(menu);
        player.containerMenu = menu;
        // init mui syncer
        msm.onOpen();
        // finally invoke event
        NeoForge.EVENT_BUS.post(new PlayerContainerEvent.Open(player, menu));
    }

    @ApiStatus.Internal
    @OnlyIn(Dist.CLIENT)
    public static <T extends GuiData> void openFromClient(int windowId, int networkId, @NotNull UIFactory<T> factory,
                                                          @NotNull RegistryFriendlyByteBuf data, @NotNull Player player) {
        T guiData = factory.readGuiData(player, data);
        UISettings settings = new UISettings();
        settings.defaultCanInteractWith(factory, guiData);
        ModularSyncManager msm = new ModularSyncManager(true);
        PanelSyncManager syncManager = new PanelSyncManager(msm, true);
        ModularPanel<?> panel = factory.createPanel(guiData, syncManager, settings);
        WidgetTree.collectSyncValues(syncManager, panel);
        guiData.addSyncHandlers(syncManager);
        ModularScreen screen = factory.createScreen(guiData, panel);
        screen.getContext().setSettings(settings);
        ModularContainerMenu container = settings.hasCustomContainer() ? settings.createContainer(windowId) : factory.createContainer(windowId);
        container.construct(player, msm, settings, panel.getName(), guiData);
        IMuiScreen wrapper = settings.hasCustomGui() ? settings.createGui(container, screen) : factory.createScreenWrapper(container, screen);
        if (!(wrapper.wrappedScreen() instanceof AbstractContainerScreen<?> guiContainer)) {
            throw new IllegalStateException("The wrapping screen must be a GuiContainer for synced GUIs!");
        }
        if (guiContainer.getMenu() != container)
            throw new IllegalStateException("Custom Containers are not yet allowed!");
        ModularNetwork.CLIENT.activate(networkId, msm);
        MCHelper.setScreen(wrapper.wrappedScreen());
        player.containerMenu = guiContainer.getMenu();
        msm.onOpen();
    }

    @OnlyIn(Dist.CLIENT)
    public static <T extends GuiData> void openFromClient(@NotNull UIFactory<T> factory, @NotNull T guiData) {
        // notify server to open the gui
        // server will send packet back to actually open the gui
        RegistryFriendlyByteBuf buffer = IRegistryFriendlyByteBufExtension.createEmpty(MCHelper.getMc().getConnection().registryAccess());
        factory.writeGuiData(guiData, buffer);
        PacketDistributor.sendToServer(new OpenGuiPacket<>(0, 0, factory, buffer));
    }

    @OnlyIn(Dist.CLIENT)
    static void openScreen(ModularScreen screen, UISettings settings) {
        if (screen.getScreenWrapper() != null && MCHelper.getCurrentScreen() == screen.getScreenWrapper().wrappedScreen()) {
            // already open
            return;
        }
        screen.getContext().setSettings(settings);
        Screen guiScreen;
        if (settings.hasCustomContainer()) {
            ModularContainerMenu container = settings.createContainer(0);
            container.constructClientOnly();
            guiScreen = new ContainerScreenWrapper(container, screen);
        } else {
            guiScreen = new ScreenWrapper(screen);
        }
        MCHelper.setScreen(guiScreen);
    }

    @SubscribeEvent
    public static void onTick(ServerTickEvent.Post event) {
        openedContainers.clear();
    }
}

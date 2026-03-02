package com.gregtechceu.gtceu.api.mui.factory;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.mui.InWorldMUIOpenEvent;
import com.gregtechceu.gtceu.api.mui.base.IMuiScreen;
import com.gregtechceu.gtceu.api.mui.base.MCHelper;
import com.gregtechceu.gtceu.api.mui.base.RecipeViewerSettings;
import com.gregtechceu.gtceu.api.mui.base.UIFactory;
import com.gregtechceu.gtceu.api.mui.value.sync.ModularSyncManager;
import com.gregtechceu.gtceu.api.mui.value.sync.PanelSyncManager;
import com.gregtechceu.gtceu.api.mui.widget.WidgetTree;
import com.gregtechceu.gtceu.client.mui.screen.*;
import com.gregtechceu.gtceu.common.network.GTNetwork;
import com.gregtechceu.gtceu.common.network.InWorldContainerSynchronizer;
import com.gregtechceu.gtceu.common.network.ModularNetwork;
import com.gregtechceu.gtceu.common.network.packets.ui.OpenGuiPacket;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

@Mod.EventBusSubscriber(modid = GTCEu.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class GuiManager {

    private static final Object2ObjectMap<ResourceLocation, UIFactory<?>> FACTORIES = new Object2ObjectOpenHashMap<>(
            16);

    private static final List<Player> openedContainers = new ArrayList<>(4);
    private static final Object2ObjectMap<ServerPlayer, List<ModularContainerMenu>> openedInWorldContainers = new Object2ObjectOpenHashMap<>();
    private static final List<ModularContainerMenu> clientInWorldContainers = new ArrayList<>();

    public static void registerFactory(UIFactory<?> factory) {
        Objects.requireNonNull(factory);
        ResourceLocation name = Objects.requireNonNull(factory.getFactoryName());
        if (FACTORIES.containsKey(name)) {
            throw new IllegalArgumentException("Factory with name '" + name + "' is already registered!");
        }
        FACTORIES.put(name, factory);
    }

    public static @NotNull UIFactory<?> getFactory(ResourceLocation name) {
        UIFactory<?> factory = FACTORIES.get(name);
        if (factory == null) throw new NoSuchElementException("No UI factory for name '" + name + "' found!");
        return factory;
    }

    public static boolean hasFactory(ResourceLocation name) {
        return FACTORIES.containsKey(name);
    }

    public static <T extends GuiData> void open(@NotNull UIFactory<T> factory, @NotNull T guiData,
                                                ServerPlayer player) {
        open(factory, false, guiData, player);
    }

    public static <T extends GuiData> void open(@NotNull UIFactory<T> factory, boolean inWorldUI, @NotNull T guiData,
                                                ServerPlayer player) {
        if (player instanceof FakePlayer || openedContainers.contains(player)) return;
        if (!inWorldUI) openedContainers.add(player);
        // create panel, collect sync handlers and create menu
        UISettings settings = new UISettings(RecipeViewerSettings.DUMMY);
        settings.defaultCanInteractWith(factory, guiData);
        ModularSyncManager msm = new ModularSyncManager(false);
        PanelSyncManager syncManager = new PanelSyncManager(msm, true);
        ModularPanel panel = factory.createPanel(guiData, syncManager, settings);
        WidgetTree.collectSyncValues(syncManager, panel);

        // create the menu
        player.nextContainerCounter();
        if (player.containerMenu != player.inventoryMenu && !inWorldUI) {
            player.closeContainer();
        }
        int windowId = player.containerCounter;
        ModularContainerMenu menu = settings.hasCustomContainer() ? settings.createContainer(windowId) :
                factory.createContainer(windowId);
        menu.construct(player, msm, settings, panel.getName(), guiData);

        // sync to client
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        factory.writeGuiData(guiData, buffer);
        int nid = ModularNetwork.SERVER.activate(msm);
        GTNetwork.sendToPlayer(player, new OpenGuiPacket<>(windowId, nid, factory, buffer, inWorldUI));
        // open the menu // this mimics forge behaviour
        if (!inWorldUI) {
            player.initMenu(menu);
            player.containerMenu = menu;
        } else {
            List<ModularContainerMenu> tmp = openedInWorldContainers.computeIfAbsent(player, p -> new ArrayList<>());
            menu.inWorldID = tmp.size();
            tmp.add(menu);
            menu.setSynchronizer(new InWorldContainerSynchronizer(player));
        }
        msm.onOpen();
        // finally invoke event
        MinecraftForge.EVENT_BUS.post(new PlayerContainerEvent.Open(player, menu));
    }

    @ApiStatus.Internal
    @OnlyIn(Dist.CLIENT)
    public static <T extends GuiData> void openFromClient(int windowId, int networkId, boolean inWorldUI,
                                                          @NotNull UIFactory<T> factory,
                                                          @NotNull FriendlyByteBuf data, @NotNull LocalPlayer player) {
        T guiData = factory.readGuiData(player, data);
        UISettings settings = new UISettings();
        if (inWorldUI) settings.getRecipeViewerSettings().disable();
        settings.defaultCanInteractWith(factory, guiData);
        ModularSyncManager msm = new ModularSyncManager(true);
        PanelSyncManager syncManager = new PanelSyncManager(msm, true);
        ModularPanel panel = factory.createPanel(guiData, syncManager, settings);
        WidgetTree.collectSyncValues(syncManager, panel);
        ModularScreen screen = factory.createScreen(guiData, panel);
        screen.getContext().setSettings(settings);
        ModularContainerMenu container = settings.hasCustomContainer() ? settings.createContainer(windowId) :
                factory.createContainer(windowId);
        container.construct(player, msm, settings, panel.getName(), guiData);
        IMuiScreen wrapper = settings.hasCustomGui() ? settings.createGui(container, screen) :
                factory.createScreenWrapper(container, screen);
        if (!(wrapper.getWrappedScreen() instanceof AbstractContainerScreen<?> guiContainer)) {
            throw new IllegalStateException("The wrapping screen must be a GuiContainer for synced GUIs!");
        }
        if (guiContainer.getMenu() != container)
            throw new IllegalStateException("Custom Containers are not yet allowed!");
        ModularNetwork.CLIENT.activate(networkId, msm);
        if (inWorldUI) {
            container.inWorldID = clientInWorldContainers.size();
            clientInWorldContainers.add(container);
            MinecraftForge.EVENT_BUS
                    .post(new InWorldMUIOpenEvent(guiData, wrapper.getWrappedScreen(), screen, container));
        } else {
            MCHelper.setScreen(wrapper.getWrappedScreen());
            player.containerMenu = guiContainer.getMenu();
        }
        msm.onOpen();
    }

    @OnlyIn(Dist.CLIENT)
    public static <T extends GuiData> void openFromClient(@NotNull UIFactory<T> factory, @NotNull T guiData) {
        openFromClient(factory, guiData, false);
    }

    @OnlyIn(Dist.CLIENT)
    public static <T extends GuiData> void openFromClient(@NotNull UIFactory<T> factory, @NotNull T guiData,
                                                          boolean inWorldUI) {
        // notify server to open the gui
        // server will send packet back to actually open the gui
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        factory.writeGuiData(guiData, buffer);
        GTNetwork.sendToServer(new OpenGuiPacket<>(0, 0, factory, buffer, inWorldUI));
    }

    @OnlyIn(Dist.CLIENT)
    static void openScreen(ModularScreen screen, UISettings settings) {
        if (screen.getScreenWrapper() != null &&
                MCHelper.getCurrentScreen() == screen.getScreenWrapper().getWrappedScreen()) {
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
    public static void onTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            openedContainers.clear();
            openedInWorldContainers.values().forEach(arr -> arr.forEach(ModularContainerMenu::onUpdate));
            openedInWorldContainers.values().forEach(arr -> arr.forEach(ModularContainerMenu::broadcastChanges));
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            clientInWorldContainers.forEach(ModularContainerMenu::onUpdate);
            clientInWorldContainers.forEach(ModularContainerMenu::broadcastChanges);
        }
    }

    @SubscribeEvent
    public static void onOpenContainer(PlayerContainerEvent.Open event) {
        if (event.getContainer() instanceof ModularContainerMenu modularContainer) {
            modularContainer.opened();
        }
    }

    @SubscribeEvent
    public static void onCloseContainer(PlayerContainerEvent.Close event) {
        if (event.getContainer() instanceof ModularContainerMenu modularContainer) {
            modularContainer.removed(event.getEntity());
        }
    }

    public static ModularContainerMenu getClientInWorldMenu(int inWorldID) {
        return clientInWorldContainers.get(inWorldID);
    }

    public static ModularContainerMenu getServerInWorldMenu(ServerPlayer player, int inWorldID) {
        return openedInWorldContainers.get(player).get(inWorldID);
    }
}

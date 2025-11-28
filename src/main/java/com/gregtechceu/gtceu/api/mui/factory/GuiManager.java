package com.gregtechceu.gtceu.api.mui.factory;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.mui.InWorldMUIOpenEvent;
import com.gregtechceu.gtceu.api.mui.base.IMuiScreen;
import com.gregtechceu.gtceu.api.mui.base.MCHelper;
import com.gregtechceu.gtceu.api.mui.base.UIFactory;
import com.gregtechceu.gtceu.api.mui.base.XeiSettings;
import com.gregtechceu.gtceu.api.mui.value.sync.ModularSyncManager;
import com.gregtechceu.gtceu.api.mui.value.sync.PanelSyncManager;
import com.gregtechceu.gtceu.api.mui.widget.WidgetTree;
import com.gregtechceu.gtceu.client.mui.screen.*;
import com.gregtechceu.gtceu.common.network.GTNetwork;
import com.gregtechceu.gtceu.common.network.packets.ui.OpenGuiPacket;
import com.gregtechceu.gtceu.common.network.packets.ui.SContainerSetContent;
import com.gregtechceu.gtceu.common.network.packets.ui.SContainerSetData;
import com.gregtechceu.gtceu.common.network.packets.ui.SContainerSetSlot;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerSynchronizer;
import net.minecraft.world.item.ItemStack;
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
import java.util.function.Function;

@Mod.EventBusSubscriber(modid = GTCEu.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class GuiManager {

    private static final Object2ObjectMap<ResourceLocation, UIFactory<?>> FACTORIES = new Object2ObjectOpenHashMap<>(
            16);

    private static final List<Player> openedContainers = new ArrayList<>(4);
    private static final Object2ObjectMap<ServerPlayer, List<ModularContainerMenu>> openedInWorldContainers = new Object2ObjectOpenHashMap<>();
    private static final List<ModularContainerMenu> clientInWorldContainers = new ArrayList<>();
    private static final Function<ServerPlayer, ContainerSynchronizer> inWorldSynchronizer = player -> new ContainerSynchronizer() {

        @Override
        public void sendInitialData(@NotNull AbstractContainerMenu container, @NotNull NonNullList<ItemStack> items,
                                    @NotNull ItemStack carriedItem, int @NotNull [] initialData) {
            GTNetwork.sendToPlayer(player, new SContainerSetContent(getInWorldId(container),
                    container.incrementStateId(), items, carriedItem));

            for (int i = 0; i < initialData.length; ++i) {
                this.broadcastDataValue(container, i, initialData[i]);
            }
        }

        @Override
        public void sendSlotChange(@NotNull AbstractContainerMenu container, int slot, @NotNull ItemStack itemStack) {
            GTNetwork.sendToPlayer(player,
                    new SContainerSetSlot(getInWorldId(container), container.incrementStateId(), slot, itemStack));
        }

        @Override
        public void sendCarriedChange(@NotNull AbstractContainerMenu containerMenu, @NotNull ItemStack stack) {
            GTNetwork.sendToPlayer(player, new SContainerSetSlot(getInWorldId(containerMenu), containerMenu.incrementStateId(), -1, stack));
        }

        @Override
        public void sendDataChange(@NotNull AbstractContainerMenu container, int id, int value) {
            this.broadcastDataValue(container, id, value);
        }

        private void broadcastDataValue(AbstractContainerMenu container, int id, int value) {
            GTNetwork.sendToPlayer(player, new SContainerSetData(getInWorldId(container), id, value));
        }

        private int getInWorldId(AbstractContainerMenu container) {
            if (container instanceof ModularContainerMenu modularContainer) return modularContainer.inWorldID;
            return -1;
        }
    };

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
        UISettings settings = new UISettings(XeiSettings.DUMMY);
        settings.defaultCanInteractWith(factory, guiData);
        PanelSyncManager syncManager = new PanelSyncManager(false);
        ModularPanel panel = factory.createPanel(guiData, syncManager, settings);
        WidgetTree.collectSyncValues(syncManager, panel);

        // create the menu
        player.nextContainerCounter();
        if (player.containerMenu != player.inventoryMenu && !inWorldUI) {
            player.closeContainer();
        }
        int windowId = player.containerCounter;
        ModularContainerMenu menu = settings.hasContainer() ? settings.createContainer(windowId) :
                factory.createContainer(windowId);
        menu.construct(player, syncManager, settings, panel.getName(), guiData);

        // sync to client
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        factory.writeGuiData(guiData, buffer);
        GTNetwork.sendToPlayer(player, new OpenGuiPacket<>(windowId, factory, buffer, inWorldUI));
        // open the menu // this mimics forge behaviour
        if (!inWorldUI) {
            player.initMenu(menu);
            player.containerMenu = menu;
        } else {
            List<ModularContainerMenu> tmp = openedInWorldContainers.computeIfAbsent(player, p -> new ArrayList<>());
            menu.inWorldID = tmp.size();
            tmp.add(menu);
            menu.setSynchronizer(inWorldSynchronizer.apply(player));
        }
        // finally invoke event
        MinecraftForge.EVENT_BUS.post(new PlayerContainerEvent.Open(player, menu));
    }

    @ApiStatus.Internal
    @OnlyIn(Dist.CLIENT)
    public static <T extends GuiData> void openFromClient(int windowId, boolean inWorldUI,
                                                          @NotNull UIFactory<T> factory,
                                                          @NotNull FriendlyByteBuf data, @NotNull LocalPlayer player) {
        T guiData = factory.readGuiData(player, data);
        UISettings settings = new UISettings();
        settings.defaultCanInteractWith(factory, guiData);
        PanelSyncManager syncManager = new PanelSyncManager(true);
        ModularPanel panel = factory.createPanel(guiData, syncManager, settings);
        WidgetTree.collectSyncValues(syncManager, panel);
        ModularScreen screen = factory.createScreen(guiData, panel);
        screen.getContext().setSettings(settings);
        ModularContainerMenu container = settings.hasContainer() ? settings.createContainer(windowId) :
                factory.createContainer(windowId);
        container.construct(player, syncManager, settings, panel.getName(), guiData);
        IMuiScreen wrapper = factory.createScreenWrapper(container, screen);
        if (!(wrapper.getWrappedScreen() instanceof AbstractContainerScreen<?> guiContainer)) {
            throw new IllegalStateException("The wrapping screen must be a GuiContainer for synced GUIs!");
        }
        if (guiContainer.getMenu() != container)
            throw new IllegalStateException("Custom Containers are not yet allowed!");
        if (inWorldUI) {
            container.inWorldID = clientInWorldContainers.size();
            clientInWorldContainers.add(container);
            MinecraftForge.EVENT_BUS
                    .post(new InWorldMUIOpenEvent(guiData, wrapper.getWrappedScreen(), screen, container));
        } else {
            MCHelper.setScreen(wrapper.getWrappedScreen());
            player.containerMenu = guiContainer.getMenu();
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static <T extends GuiData> void openFromClient(@NotNull UIFactory<T> factory, @NotNull T guiData) {
        openFromClient(factory, guiData, false);
    }

    @OnlyIn(Dist.CLIENT)
    public static <T extends GuiData> void openFromClient(@NotNull UIFactory<T> factory, @NotNull T guiData,
                                                          boolean inWorldUI) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        factory.writeGuiData(guiData, buffer);
        GTNetwork.sendToServer(new OpenGuiPacket<>(0, factory, buffer, inWorldUI));
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
        if (settings.hasContainer()) {
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
        if (event.getContainer() instanceof ModularContainerMenu modular) {
            ModularSyncManager syncManager = modular.getSyncManager();
            if (syncManager != null) {
                syncManager.onOpen();
            }
        }
    }

    public static ModularContainerMenu getClientInWorldMenu(int inWorldID) {
        return clientInWorldContainers.get(inWorldID);
    }

    public static ModularContainerMenu getServerInWorldMenu(ServerPlayer player, int inWorldID) {
        return openedInWorldContainers.get(player).get(inWorldID);
    }
}

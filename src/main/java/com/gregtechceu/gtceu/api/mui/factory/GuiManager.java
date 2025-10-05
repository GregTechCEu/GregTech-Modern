package com.gregtechceu.gtceu.api.mui.factory;

import com.gregtechceu.gtceu.GTCEu;
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
import com.gregtechceu.gtceu.core.mixins.ServerPlayerAccessor;

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
        if (factory == null) throw new NoSuchElementException();
        return factory;
    }

    public static boolean hasFactory(ResourceLocation name) {
        return FACTORIES.containsKey(name);
    }

    public static <T extends GuiData> void open(@NotNull UIFactory<T> factory, @NotNull T guiData,
                                                ServerPlayer player) {
        if (player instanceof FakePlayer || openedContainers.contains(player)) return;
        openedContainers.add(player);
        // create panel, collect sync handlers and create container
        UISettings settings = new UISettings(XeiSettings.DUMMY);
        settings.defaultCanInteractWith(factory, guiData);
        PanelSyncManager syncManager = new PanelSyncManager();
        ModularPanel panel = factory.createPanel(guiData, syncManager, settings);
        WidgetTree.collectSyncValues(syncManager, panel);

        // create the menu
        player.nextContainerCounter();
        player.closeContainer();
        int windowId = player.containerCounter;
        ModularContainerMenu container = settings.hasContainer() ? settings.createContainer(windowId) :
                factory.createContainer(windowId);
        container.construct(player, syncManager, settings, panel.getName(), guiData);

        // sync to client
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        factory.writeGuiData(guiData, buffer);
        GTNetwork.sendToPlayer(player, new OpenGuiPacket(windowId, factory, buffer));
        // open the menu // this mimics forge behaviour
        player.containerMenu = container;
        player.containerMenu.addSlotListener(((ServerPlayerAccessor) player).getContainerListener());
        // finally invoke event
        MinecraftForge.EVENT_BUS.post(new PlayerContainerEvent.Open(player, container));
    }

    @OnlyIn(Dist.CLIENT)
    public static <T extends GuiData> void open(int windowId, @NotNull UIFactory<T> factory,
                                                @NotNull FriendlyByteBuf data, @NotNull LocalPlayer player) {
        T guiData = factory.readGuiData(player, data);
        UISettings settings = new UISettings();
        settings.defaultCanInteractWith(factory, guiData);
        PanelSyncManager syncManager = new PanelSyncManager();
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
        MCHelper.setScreen(wrapper.getWrappedScreen());
        player.containerMenu = guiContainer.getMenu();
    }

    @OnlyIn(Dist.CLIENT)
    static void openScreen(ModularScreen screen, UISettings settings) {
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
}

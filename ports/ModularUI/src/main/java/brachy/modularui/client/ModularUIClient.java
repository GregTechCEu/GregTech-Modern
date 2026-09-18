package brachy.modularui.client;

import brachy.modularui.ModularUI;
import brachy.modularui.ModularUIMenuTypes;
import brachy.modularui.animation.AnimatorManager;
import brachy.modularui.api.drawable.IIcon;
import brachy.modularui.drawable.ClientTooltipComponentIcon;
import brachy.modularui.drawable.DelegateIcon;
import brachy.modularui.drawable.DrawableTooltipComponent;
import brachy.modularui.drawable.GuiSpriteManager;
import brachy.modularui.drawable.HoverableIcon;
import brachy.modularui.drawable.Icon;
import brachy.modularui.drawable.InteractableIcon;
import brachy.modularui.drawable.TooltipComponentIcon;
import brachy.modularui.drawable.text.KeyIcon;
import brachy.modularui.drawable.text.TextIcon;
import brachy.modularui.network.ModularNetwork;
import brachy.modularui.screen.ContainerScreenWrapper;
import brachy.modularui.screen.ModularContainerMenu;
import brachy.modularui.test.TestHandler;
import brachy.modularui.theme.ThemeManager;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;

import brachy.modularui.utils.CursorHandler;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLConstructModEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

import it.unimi.dsi.fastutil.floats.FloatUnaryOperator;
import lombok.Getter;

import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.LevelEvent;

import java.util.function.Function;

@Mod(value = ModularUI.MOD_ID, dist = Dist.CLIENT)
public class ModularUIClient {

    @Getter
    private static final DeltaTracker.Timer timer60Fps = new DeltaTracker.Timer(60f, 0, FloatUnaryOperator.identity());

    public ModularUIClient(IEventBus modBus, ModContainer modContainer) {
        modBus.addListener(this::onPreInit);
        modBus.addListener(this::onInit);
        modBus.addListener(this::registerScreens);
        modBus.addListener(this::onRegisterClientTooltipComponents);
        modBus.addListener(this::onRegisterAssetReloadListeners);

        IEventBus forgeBus = NeoForge.EVENT_BUS;
        forgeBus.addListener(this::onUnloadWorld);

        if (!ModularUI.isDataGen()) {
            CursorHandler.init();
            AnimatorManager.init();
        }
    }

    protected void onPreInit(FMLConstructModEvent event) {
        TestHandler.onPreInit();
    }

    protected void onInit(FMLCommonSetupEvent event) {
        if (!ModularUI.isDataGen()) {
            // enable stencil bits, must call on render thread
            RenderSystem.recordRenderCall(() -> Minecraft.getInstance().getMainRenderTarget().enableStencil());
        }
    }

    @SuppressWarnings("deprecation")
    public void registerScreens(final RegisterMenuScreensEvent event) {
        event.<ModularContainerMenu, ContainerScreenWrapper>register(ModularUIMenuTypes.MODULAR_CONTAINER.get(),
                ContainerScreenWrapper::new);
    }

    private void onRegisterClientTooltipComponents(RegisterClientTooltipComponentFactoriesEvent event) {
        Function<IIcon, ClientTooltipComponent> factory = DrawableTooltipComponent::new;
        event.register(Icon.class, factory);
        event.register(DelegateIcon.class, factory);
        event.register(HoverableIcon.class, factory);
        event.register(InteractableIcon.class, factory);
        event.register(KeyIcon.class, factory);
        event.register(TextIcon.class, factory);
        event.register(ClientTooltipComponentIcon.class, ClientTooltipComponentIcon::getClientTooltipComponent);
        event.register(TooltipComponentIcon.class, TooltipComponentIcon::clientComponent);
    }

    public void onRegisterAssetReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(ThemeManager.INSTANCE);
        event.registerReloadListener(new GuiSpriteManager(Minecraft.getInstance().getTextureManager()));
    }

    private void onUnloadWorld(LevelEvent.Unload event) {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            ModularNetwork.CLIENT.onPlayerLeave(player);

            if (Minecraft.getInstance().hasSingleplayerServer()) {
                // we need to handle single player here, since PlayerLoggedOutEvent is not triggered for some reason
                ModularNetwork.SERVER.onPlayerLeave(player);
            }
        }
    }
}

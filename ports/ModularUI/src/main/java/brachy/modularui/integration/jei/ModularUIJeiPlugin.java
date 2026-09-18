package brachy.modularui.integration.jei;

import brachy.modularui.ModularUI;
import brachy.modularui.integration.jei.handler.JeiScreenHandler;
import brachy.modularui.screen.ContainerScreenWrapper;
import brachy.modularui.screen.ScreenWrapper;

import org.jspecify.annotations.NullMarked;
import net.minecraft.resources.Identifier;

import lombok.Getter;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.runtime.IJeiRuntime;

import javax.annotation.ParametersAreNonnullByDefault;

@NullMarked
@ParametersAreNonnullByDefault
@JeiPlugin
public class ModularUIJeiPlugin implements IModPlugin {

    @Getter
    private static IJeiRuntime runtime = null;
    public static IJeiHelpers jeiHelpers;

    public static boolean hasRuntime() {
        return runtime != null;
    }

    @Override
    public Identifier getPluginUid() {
        return ModularUI.id("jei_plugin");
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        runtime = jeiRuntime;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        jeiHelpers = registration.getJeiHelpers();
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        if (ModularUI.Mods.REI.isLoaded() || ModularUI.Mods.EMI.isLoaded()) return;

        JeiScreenHandler.register(ScreenWrapper.class, registration);
        JeiScreenHandler.register(ContainerScreenWrapper.class, registration);
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        if (ModularUI.Mods.REI.isLoaded() || ModularUI.Mods.EMI.isLoaded()) return;

        //JeiContainerHandler.register(ModularContainerMenu.class, registration);
    }

}

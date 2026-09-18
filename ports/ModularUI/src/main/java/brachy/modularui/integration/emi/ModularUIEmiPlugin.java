package brachy.modularui.integration.emi;

import brachy.modularui.ModularUI;
import brachy.modularui.integration.emi.handler.EmiScreenHandler;
import brachy.modularui.screen.ContainerScreenWrapper;
import brachy.modularui.screen.ScreenWrapper;

import brachy.modularui.test.TestMachine;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;

@EmiEntrypoint
public class ModularUIEmiPlugin implements EmiPlugin {

    @Override
    public void register(EmiRegistry registry) {
        EmiScreenHandler.register(ScreenWrapper.class, registry);
        EmiScreenHandler.register(ContainerScreenWrapper.class, registry);
        if (ModularUI.isDev()) {
            TestMachine.EMI.register(registry);
        }
    }

}

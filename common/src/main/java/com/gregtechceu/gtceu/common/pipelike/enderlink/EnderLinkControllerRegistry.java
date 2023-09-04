package com.gregtechceu.gtceu.common.pipelike.enderlink;

import com.gregtechceu.gtceu.api.misc.SideLocal;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.EnderLinkControllerMachine;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;


@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class EnderLinkControllerRegistry {
    private static SideLocal<Map<UUID, EnderLinkControllerMachine>> registeredControllers = new SideLocal<>(Object2ObjectOpenHashMap::new);

    public static void registerController(EnderLinkControllerMachine controller) {
        registeredControllers.get().put(controller.getUuid(), controller);
    }

    public static void unregisterController(EnderLinkControllerMachine controller) {
        unregisterController(controller.getUuid());
    }

    public static void unregisterController(@Nullable UUID uuid) {
        if (uuid == null)
            return;

        registeredControllers.get().remove(uuid);
    }

    public static boolean isRegistered(UUID uuid) {
        return registeredControllers.get().containsKey(uuid);
    }

    public static Optional<EnderLinkControllerMachine> getController(@Nullable UUID uuid) {
        if (uuid == null)
            return Optional.empty();

        return Optional.ofNullable(registeredControllers.get().get(uuid));
    }
}

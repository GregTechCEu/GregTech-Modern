package com.gregtechceu.gtceu.utils.dev;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import com.sun.jna.platform.win32.*;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

@ApiStatus.Internal
public class ResourceReloadDetector {

    private static final Path gradleDir = findGradleDir();

    @ApiStatus.Internal
    public static CompletableFuture<Void> regenerateResourcesOnReload(Supplier<CompletableFuture<Void>> reloadFuture) {
        if (!ConfigHolder.INSTANCE.dev.autoRebuildResources || !GTCEu.isDev() || gradleDir == null) {
            return reloadFuture.get();
        }
        ProcessBuilder builder = switch (Util.getPlatform()) {
            case WINDOWS -> new ProcessBuilder("cmd.exe", "/c", "gradlew.bat", ":processResources");
            default -> new ProcessBuilder("./gradlew", ":processResources");
        };
        builder.directory(gradleDir.toFile());
        builder.inheritIO();
        Process process;
        try {
            process = builder.start();
        } catch (IOException exception) {
            GTCEu.LOGGER.error("Cound not run ./gradlew :processResources", exception);
            GTCEu.LOGGER.error("Message the GTCEu developers about this!");
            return reloadFuture.get();
        }
        Minecraft.getInstance().player.sendSystemMessage(Component.translatable("gtceu.debug.resource_rebuild.start"));
        Instant start = Instant.now();
        // wait for the resource reload to finish, then send chat message, then let MC actually reload resources
        return process.toHandle().onExit()
                .thenRun(() -> Minecraft.getInstance().player
                        .sendSystemMessage(Component.translatable("gtceu.debug.resource_rebuild.done",
                                Duration.between(start, Instant.now()))))
                .thenCompose($ -> reloadFuture.get());
    }

    private static @Nullable Path findGradleDir() {
        Path path = Path.of(".").toAbsolutePath();
        do {
            if (Files.isRegularFile(path.resolve("settings.gradle")) ||
                    Files.isRegularFile(path.resolve("settings.gradle.kts"))) {
                return path;
            }
            path = path.getParent();
        } while (path.getParent() != null);

        return null;
    }
}

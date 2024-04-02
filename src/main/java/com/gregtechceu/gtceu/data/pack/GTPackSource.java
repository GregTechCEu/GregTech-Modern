package com.gregtechceu.gtceu.data.pack;

import lombok.RequiredArgsConstructor;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Function;

@RequiredArgsConstructor
public class GTPackSource implements RepositorySource {

    private final String name;
    private final PackType type;
    private final Pack.Position position;
    private final Function<String, PackResources> resources;

    @Override
    public void loadPacks(Consumer<Pack> onLoad) {
        onLoad.accept(Pack.readMetaAndCreate(name,
            Component.literal(name),
            true,
            new Pack.ResourcesSupplier() {
                @NotNull
                @Override
                public PackResources openPrimary(@NotNull String name) {
                    return resources.apply(name);
                }

                @NotNull
                @Override
                public PackResources openFull(@NotNull String name, @NotNull Pack.Info info) {
                    return openPrimary(name);
                }
            },
            type,
            position,
            PackSource.BUILT_IN)
        );
    }
}

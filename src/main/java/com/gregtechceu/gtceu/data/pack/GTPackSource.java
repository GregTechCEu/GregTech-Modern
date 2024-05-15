package com.gregtechceu.gtceu.data.pack;

import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;

import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

@RequiredArgsConstructor
public class GTPackSource implements RepositorySource {

    private final String name;
    private final PackType type;
    private final Pack.Position position;
    private final Function<PackLocationInfo, PackResources> resources;

    @Override
    public void loadPacks(Consumer<Pack> onLoad) {
        onLoad.accept(Pack.readMetaAndCreate(
                new PackLocationInfo(name, Component.literal(name), PackSource.BUILT_IN, Optional.empty()),
                new Pack.ResourcesSupplier() {

                    @Override
                    public PackResources openPrimary(PackLocationInfo info) {
                        return resources.apply(info);
                    }

                    @Override
                    public PackResources openFull(PackLocationInfo info, Pack.Metadata p_325959_) {
                        return openPrimary(info);
                    }
                },
                type,
                new PackSelectionConfig(true, position, false)));
    }
}

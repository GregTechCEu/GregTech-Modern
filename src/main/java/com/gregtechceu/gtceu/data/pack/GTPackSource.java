package com.gregtechceu.gtceu.data.pack;

import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;

import lombok.RequiredArgsConstructor;

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
        onLoad.accept(readMetaAndCreate(name,
                Component.literal(name),
                true,
                resources::apply,
                type,
                position,
                PackSource.BUILT_IN));
    }

    public static Pack readMetaAndCreate(String id, Component title, boolean required, Pack.ResourcesSupplier resources,
                                         PackType packType, Pack.Position defaultPosition, PackSource packSource) {
        Pack.Info info = Pack.readPackInfo(id, resources);
        return info != null ? Pack.create(id, title, required, resources,
                info, packType, defaultPosition, true, packSource) : null;
    }
}

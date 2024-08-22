package com.gregtechceu.gtceu.api.data.chemical.material.properties;

import com.gregtechceu.gtceu.api.graphnet.logic.NetLogicData;
import com.gregtechceu.gtceu.api.graphnet.pipenet.IPipeNetNodeHandler;
import com.gregtechceu.gtceu.api.graphnet.pipenet.WorldPipeNetNode;
import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.IPipeMaterialStructure;
import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.IPipeStructure;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;

import it.unimi.dsi.fastutil.objects.Object2ObjectRBTreeMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class PipeNetProperties implements IMaterialProperty, IPipeNetNodeHandler {

    protected final Map<IPipeNetMaterialProperty.MaterialPropertyKey<?>, IPipeNetMaterialProperty> properties = new Object2ObjectRBTreeMap<>(
            Comparator.comparing(IPipeNetMaterialProperty.MaterialPropertyKey::getSerializedName));

    public void setProperty(IPipeNetMaterialProperty property) {
        this.properties.put(property.getKey(), property);
    }

    public boolean hasProperty(IPipeNetMaterialProperty.MaterialPropertyKey<?> key) {
        return this.properties.containsKey(key);
    }

    public Collection<IPipeNetMaterialProperty> getRegisteredProperties() {
        return properties.values();
    }

    public <T extends IPipeNetMaterialProperty> T getProperty(IPipeNetMaterialProperty.MaterialPropertyKey<T> key) {
        return key.cast(this.properties.get(key));
    }

    public void removeProperty(IPipeNetMaterialProperty.MaterialPropertyKey<?> key) {
        this.properties.remove(key);
    }

    public boolean generatesStructure(IPipeStructure structure) {
        for (IPipeNetMaterialProperty p : properties.values()) {
            if (p.generatesStructure(structure)) return true;
        }
        return false;
    }

    @Override
    public @NotNull Collection<WorldPipeNetNode> getOrCreateFromNets(ServerLevel level, BlockPos pos,
                                                                     IPipeStructure structure) {
        List<WorldPipeNetNode> list = new ObjectArrayList<>();
        for (IPipeNetMaterialProperty p : properties.values()) {
            if (p.supportsStructure(structure)) {
                WorldPipeNetNode node = p.getOrCreateFromNet(level, pos, structure);
                if (node != null) list.add(node);
            }
        }
        return list;
    }

    @Override
    public @NotNull Collection<WorldPipeNetNode> getFromNets(ServerLevel level, BlockPos pos,
                                                             IPipeStructure structure) {
        List<WorldPipeNetNode> list = new ObjectArrayList<>();
        for (IPipeNetMaterialProperty p : properties.values()) {
            if (p.supportsStructure(structure)) {
                WorldPipeNetNode node = p.getFromNet(level, pos, structure);
                if (node != null) list.add(node);
            }
        }
        return list;
    }

    @Override
    public void removeFromNets(ServerLevel level, BlockPos pos, IPipeStructure structure) {
        for (IPipeNetMaterialProperty p : properties.values()) {
            if (p.supportsStructure(structure)) p.removeFromNet(level, pos, structure);
        }
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, BlockGetter level, @NotNull List<Component> tooltip,
                               @NotNull TooltipFlag flagIn, IPipeStructure structure) {
        for (IPipeNetMaterialProperty p : properties.values()) {
            if (p.supportsStructure(structure))
                p.addInformation(stack, level, tooltip, flagIn, (IPipeMaterialStructure) structure);
        }
    }

    @Override
    public void verifyProperty(MaterialProperties properties) {
        for (IPipeNetMaterialProperty p : this.properties.values()) {
            p.verifyProperty(properties);
        }
    }

    protected static final class MaterialPropertyComparator implements Comparator<IPipeNetMaterialProperty> {

        @Override
        public int compare(IPipeNetMaterialProperty o1, IPipeNetMaterialProperty o2) {
            return 0;
        }
    }

    public interface IPipeNetMaterialProperty extends IMaterialProperty {

        @Nullable
        WorldPipeNetNode getOrCreateFromNet(LevelAccessor world, BlockPos pos, IPipeStructure structure);

        @Nullable
        WorldPipeNetNode getFromNet(LevelAccessor world, BlockPos pos, IPipeStructure structure);

        void mutateData(NetLogicData data, IPipeStructure structure);

        void removeFromNet(LevelAccessor world, BlockPos pos, IPipeStructure structure);

        boolean generatesStructure(IPipeStructure structure);

        boolean supportsStructure(IPipeStructure structure);

        void addInformation(@NotNull ItemStack stack, BlockGetter worldIn, @NotNull List<Component> tooltip,
                            @NotNull TooltipFlag flagIn, IPipeMaterialStructure structure);

        MaterialPropertyKey<?> getKey();

        class MaterialPropertyKey<T extends IPipeNetMaterialProperty> implements StringRepresentable {

            private final @NotNull String name;

            public MaterialPropertyKey(@NotNull String name) {
                this.name = name;
            }

            @Override
            public @NotNull String getSerializedName() {
                return name;
            }

            T cast(IPipeNetMaterialProperty property) {
                return (T) property;
            }
        }
    }
}

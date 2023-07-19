package com.gregtechceu.gtceu.api.gui.misc;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.UnificationEntry;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.data.tag.TagUtil;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockfluid.BedrockFluidVeinSaveData;
import com.gregtechceu.gtceu.api.gui.texture.ProspectingTexture;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.side.fluid.FluidHelper;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.Fluid;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author KilaBash
 * @date 2023/7/10
 * @implNote ProspectorMode
 */
public abstract class ProspectorMode<T> {
    public static ProspectorMode<String> ORE = new ProspectorMode<>("metaitem.prospector.mode.ores", 16) {
        private final Map<BlockState, String> BLOCK_CACHE = new HashMap<>();
        private final Map<String, IGuiTexture> ICON_CACHE = new HashMap<>();

        @Override
        public void scan(String[][][] storage, LevelChunk chunk) {
            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
            var oreTag = TagUtil.createBlockTag("ores");
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    for (int y = chunk.getMaxBuildHeight() - 1; y >= chunk.getMinBuildHeight(); y--) {
                        pos.set(x, y, z);
                        var state = chunk.getBlockState(pos);
                        if (state.is(oreTag)) {
                            var itemName = BLOCK_CACHE.computeIfAbsent(state, blockState -> {
                                var name = BuiltInRegistries.BLOCK.getKey(blockState.getBlock()).toString();
                                var entry = ChemicalHelper.getUnificationEntry(blockState.getBlock());
                                if (entry != null && entry.material != null) {
                                    name = "material_" + entry.material.getName();
                                }
                                return name;
                            });
                            storage[x][z] = ArrayUtils.add(storage[x][z], itemName);
                        }
                    }
                }
            }
        }

        @Override
        public int getItemColor(String item) {
            if (item.startsWith("material_")) {
                var mat = GTMaterials.get(item.substring(9));
                if (mat != null) {
                    return mat.getMaterialRGB();
                }
            }
            return BuiltInRegistries.BLOCK.get(new ResourceLocation(item)).defaultMapColor().col;
        }

        @Override
        public IGuiTexture getItemIcon(String item) {
            return ICON_CACHE.computeIfAbsent(item, name -> {
                if (name.startsWith("material_")) {
                    var mat = GTMaterials.get(name.substring(9));
                    if (mat != null) {
                        var list = new ArrayList<ItemStack>();
                        for (TagPrefix oreTag : TagPrefix.ORES.keySet()) {
                            for (var block : ChemicalHelper.getBlocks(new UnificationEntry(oreTag, mat))) {
                                list.add(new ItemStack(block));
                            }
                        }
                        return new ItemStackTexture(list.toArray(ItemStack[]::new)).scale(0.8f);
                    }
                }
                return new ItemStackTexture(new ItemStack(BuiltInRegistries.BLOCK.get(new ResourceLocation(name)))).scale(0.8f);
            });
        }

        @Override
        public String getDescriptionId(String item) {
            if (item.startsWith("material_")) {
                var mat = GTMaterials.get(item.substring(9));
                if (mat != null) {
                    return mat.getUnlocalizedName();
                }
            }
            return BuiltInRegistries.BLOCK.get(new ResourceLocation(item)).getDescriptionId();
        }

        @Override
        public String getUniqueID(String item) {
            return item;
        }

        @Override
        public void serialize(String item, FriendlyByteBuf buf) {
            buf.writeUtf(item);
        }

        @Override
        public String deserialize(FriendlyByteBuf buf) {
            return buf.readUtf();
        }

        @Override
        public Class<String> getItemClass() {
            return String.class;
        }

        @Override
        public void appendTooltips(String[] items, List<Component> tooltips, String selected) {
            Map<String, Integer> counter = new HashMap<>();
            for (var item : items) {
                if (ProspectingTexture.SELECTED_ALL.equals(selected) || selected.equals(getUniqueID(item))) {
                    counter.put(item, counter.getOrDefault(item, 0) + 1);
                }
            }
            counter.forEach((item, count) -> tooltips.add(Component.translatable(getDescriptionId(item)).append(" --- " + count)));
        }
    };

    public record FluidInfo(Fluid fluid, int left, int yield) {

    }

    public static ProspectorMode<FluidInfo> FLUID = new ProspectorMode<>("metaitem.prospector.mode.fluid", 1) {
        @Override
        public void scan(FluidInfo[][][] storage, LevelChunk chunk) {
            if (chunk.getLevel() instanceof ServerLevel serverLevel) {
                var fluidVein = BedrockFluidVeinSaveData.getOrCreate(serverLevel).getFluidVeinWorldEntry(chunk.getPos().x, chunk.getPos().z);
                if (fluidVein.getDefinition() != null) {
                    var left = 100 * fluidVein.getOperationsRemaining() / BedrockFluidVeinSaveData.MAXIMUM_VEIN_OPERATIONS;
                    storage[0][0] = new FluidInfo[] {
                            new FluidInfo(fluidVein.getDefinition().getStoredFluid().get(), left, fluidVein.getFluidYield()),
                    };
                }
            }
        }

        @Override
        public int getItemColor(FluidInfo item) {
            return FluidHelper.getColor(FluidStack.create(item.fluid, item.yield));
        }

        @Override
        public IGuiTexture getItemIcon(FluidInfo item) {
            return new ItemStackTexture(item.fluid.getBucket());
        }

        @Override
        public String getDescriptionId(FluidInfo item) {
            return FluidStack.create(item.fluid, item.yield).getDisplayName().getString();
        }

        @Override
        public String getUniqueID(FluidInfo item) {
            return BuiltInRegistries.FLUID.getKey(item.fluid).toString();
        }

        @Override
        public void serialize(FluidInfo item, FriendlyByteBuf buf) {
            buf.writeUtf(BuiltInRegistries.FLUID.getKey(item.fluid).toString());
            buf.writeVarInt(item.left);
            buf.writeVarInt(item.yield);
        }

        @Override
        public FluidInfo deserialize(FriendlyByteBuf buf) {
            return new FluidInfo(BuiltInRegistries.FLUID.get(new ResourceLocation(buf.readUtf())), buf.readVarInt(), buf.readVarInt());
        }

        @Override
        public Class<FluidInfo> getItemClass() {
            return FluidInfo.class;
        }

        @Override
        public void appendTooltips(FluidInfo[] items, List<Component> tooltips, String selected) {
            for (FluidInfo item : items) {
                tooltips.add(Component.translatable(getDescriptionId(item)).append(" --- %s (%s%%)".formatted(item.yield, item.left)));
            }
        }

    };

    public final String unlocalizedName;
    public final int cellSize;

    ProspectorMode(@Nonnull String unlocalizedName, int cellSize) {
        this.unlocalizedName = unlocalizedName;
        this.cellSize = cellSize;
    }

    public abstract void scan(T[][][] storage, LevelChunk chunk);
    public abstract int getItemColor(T item);
    public abstract IGuiTexture getItemIcon(T item);
    public abstract String getDescriptionId(T item);
    public abstract String getUniqueID(T item);
    public abstract void serialize(T item, FriendlyByteBuf buf);
    public abstract T deserialize(FriendlyByteBuf buf);
    public abstract Class<T> getItemClass();
    public abstract void appendTooltips(T[] items, List<Component> tooltips, String selected);
}

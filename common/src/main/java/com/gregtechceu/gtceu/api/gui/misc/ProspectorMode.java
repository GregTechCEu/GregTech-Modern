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
                                var name = Registry.BLOCK.getKey(blockState.getBlock()).toString();
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
            return Registry.BLOCK.get(new ResourceLocation(item)).defaultMaterialColor().col;
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
                return new ItemStackTexture(new ItemStack(Registry.BLOCK.get(new ResourceLocation(name)))).scale(0.8f);
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
            return Registry.BLOCK.get(new ResourceLocation(item)).getDescriptionId();
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

    public static ProspectorMode<FluidStack> FLUID = new ProspectorMode<>("metaitem.prospector.mode.fluid", 1) {
        @Override
        public void scan(FluidStack[][][] storage, LevelChunk chunk) {
            if (chunk.getLevel() instanceof ServerLevel serverLevel) {
                var fluidVein = BedrockFluidVeinSaveData.getOrCreate(serverLevel).getFluidVeinWorldEntry(chunk.getPos().x, chunk.getPos().z);
                if (fluidVein.getDefinition() != null) {
                    var amount = fluidVein.getFluidYield() * fluidVein.getOperationsRemaining() / BedrockFluidVeinSaveData.MAXIMUM_VEIN_OPERATIONS;
                    storage[0][0] = new FluidStack[] {
                            FluidStack.create(fluidVein.getDefinition().getStoredFluid().get(), amount)
                    };
                }
            }
        }

        @Override
        public int getItemColor(FluidStack item) {
            return FluidHelper.getColor(item);
        }

        @Override
        public IGuiTexture getItemIcon(FluidStack item) {
            return new ItemStackTexture(item.getFluid().getBucket());
        }

        @Override
        public String getDescriptionId(FluidStack item) {
            return item.getDisplayName().getString();
        }

        @Override
        public String getUniqueID(FluidStack item) {
            return Registry.FLUID.getKey(item.getFluid()).toString();
        }

        @Override
        public void serialize(FluidStack item, FriendlyByteBuf buf) {
            item.writeToBuf(buf);
        }

        @Override
        public FluidStack deserialize(FriendlyByteBuf buf) {
            return FluidStack.readFromBuf(buf);
        }

        @Override
        public Class<FluidStack> getItemClass() {
            return FluidStack.class;
        }

        @Override
        public void appendTooltips(FluidStack[] items, List<Component> tooltips, String selected) {
            Map<Fluid, Long> counter = new HashMap<>();
            for (var item : items) {
                if (ProspectingTexture.SELECTED_ALL.equals(selected) || selected.equals(getUniqueID(item))) {
                    counter.put(item.getFluid(), counter.getOrDefault(item.getFluid(), 0L) + item.getAmount());
                }
            }
            counter.forEach((item, count) -> tooltips.add(Component.translatable(getDescriptionId(FluidStack.create(item, count))).append(" --- " + count)));
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

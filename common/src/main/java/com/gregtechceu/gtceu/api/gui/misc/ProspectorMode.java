package com.gregtechceu.gtceu.api.gui.misc;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.UnificationEntry;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.data.tag.TagUtil;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author KilaBash
 * @date 2023/7/10
 * @implNote ProspectorMode
 */
public abstract class ProspectorMode {
    public static ProspectorMode ORE = new ProspectorMode("metaitem.prospector.mode.ores", 16) {
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
        public int getItemColor(String itemName) {
            if (itemName.startsWith("material_")) {
                var mat = GTMaterials.get(itemName.substring(9));
                if (mat != null) {
                    return mat.getMaterialRGB();
                }
            }
            return Registry.BLOCK.get(new ResourceLocation(itemName)).defaultMaterialColor().col;
        }

        @Override
        public IGuiTexture getItemIcon(String itemName) {
            return ICON_CACHE.computeIfAbsent(itemName, name -> {
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
        public String getDescriptionId(String itemName) {
            if (itemName.startsWith("material_")) {
                var mat = GTMaterials.get(itemName.substring(9));
                if (mat != null) {
                    return mat.getUnlocalizedName();
                }
            }
            return Registry.BLOCK.get(new ResourceLocation(itemName)).getDescriptionId();
        }
    };

    public static ProspectorMode FLUID = new ProspectorMode("metaitem.prospector.mode.fluid", 1) {
        @Override
        public void scan(String[][][] storage, LevelChunk chunk) {
        }

        @Override
        public int getItemColor(String itemName) {
            return -1;
        }

        @Override
        public IGuiTexture getItemIcon(String itemName) {
            return IGuiTexture.EMPTY;
        }

        @Override
        public String getDescriptionId(String itemName) {
            return "null";
        }
    };

    public final String unlocalizedName;
    public final int cellSize;

    ProspectorMode(@Nonnull String unlocalizedName, int cellSize) {
        this.unlocalizedName = unlocalizedName;
        this.cellSize = cellSize;
    }

    public abstract void scan(String[][][] storage, LevelChunk chunk);

    public abstract int getItemColor(String itemName);

    public abstract IGuiTexture getItemIcon(String itemName);

    public abstract String getDescriptionId(String itemName);
}

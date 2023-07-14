package com.gregtechceu.gtceu.api.gui.misc;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.UnificationEntry;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.data.tag.TagUtil;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockfluid.BedrockFluidVeinSavedData;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockore.BedrockOreVeinSavedData;
import com.gregtechceu.gtceu.api.gui.texture.ProspectingTexture;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.utils.GradientUtil;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.side.fluid.FluidHelper;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import com.mojang.datafixers.util.Pair;
import com.sun.security.jgss.InquireType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.Fluid;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
            var oreTag = CustomTags.ORE_BLOCKS;
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
            return Registry.BLOCK.get(new ResourceLocation(item)) != Blocks.AIR ? Registry.BLOCK.get(new ResourceLocation(item)).getDescriptionId() : item;
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
            Set<String> keys = tooltips.stream().map(component -> component.plainCopy().getContents() instanceof TranslatableContents translatable ? translatable.getKey() : component.plainCopy().getContents().toString()).map(s -> s.replace('.', '_')).collect(Collectors.toSet());
            Map<String, Integer> plainToCount = tooltips.stream()
                    .map(component -> Pair.of(component.plainCopy().getContents() instanceof TranslatableContents translatable ? translatable.getKey() : component.plainCopy().getContents().toString(), component.getSiblings().size() > 0 ? Integer.parseInt(component.getSiblings().get(component.getSiblings().size() - 1).getString().substring(5)) : 0))
                    .distinct()
                    .collect(HashMap::new, (map, entry) -> {
                        String key = entry.getFirst().replace('.', '_');
                        map.put(key, map.getOrDefault(key, 0) + entry.getSecond());
                    }, (map1, map2) -> {
                        for (Map.Entry<String, Integer> entry : map2.entrySet()) {
                            map1.put(entry.getKey(), map1.getOrDefault(entry.getKey(), 0) + entry.getValue());
                        }
                    });
            counter.forEach((item, count) -> {
                if (item.contains(":")) {
                    item = item.replaceFirst("(.*?):(.*?)$", "block.$1.$2");
                }
                int lastCount = 0;
                String key = item.replace('.', '_');
                if (keys.contains(key)) {
                    lastCount = plainToCount.get(key);
                    tooltips.remove(Component.translatable(getDescriptionId(item)).append(" --- " + lastCount));
                } else {
                    keys.add(key);
                    plainToCount.put(key, plainToCount.getOrDefault(key, 0) + count);
                }
                tooltips.add(Component.translatable(getDescriptionId(item)).append(" --- " + (count + lastCount)));
            });
        }
    };

    public record FluidInfo(Fluid fluid, int left, int yield) {

    }

    public static ProspectorMode<FluidInfo> FLUID = new ProspectorMode<>("metaitem.prospector.mode.fluid", 1) {
        @Override
        public void scan(FluidInfo[][][] storage, LevelChunk chunk) {
            if (chunk.getLevel() instanceof ServerLevel serverLevel) {
                var fluidVein = BedrockFluidVeinSavedData.getOrCreate(serverLevel).getFluidVeinWorldEntry(chunk.getPos().x, chunk.getPos().z);
                if (fluidVein.getDefinition() != null) {
                    var left = 100 * fluidVein.getOperationsRemaining() / BedrockFluidVeinSavedData.MAXIMUM_VEIN_OPERATIONS;
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
            return Registry.FLUID.getKey(item.fluid).toString();
        }

        @Override
        public void serialize(FluidInfo item, FriendlyByteBuf buf) {
            buf.writeUtf(Registry.FLUID.getKey(item.fluid).toString());
            buf.writeVarInt(item.left);
            buf.writeVarInt(item.yield);
        }

        @Override
        public FluidInfo deserialize(FriendlyByteBuf buf) {
            return new FluidInfo(Registry.FLUID.get(new ResourceLocation(buf.readUtf())), buf.readVarInt(), buf.readVarInt());
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

    public record OreInfo(Material material, int weight, int left, int yield) {

    }

    public static ProspectorMode<OreInfo> BEDROCK_ORE = new ProspectorMode<>("metaitem.prospector.mode.bedrock_ore", 1) {
        @Override
        public void scan(OreInfo[][][] storage, LevelChunk chunk) {
            if (chunk.getLevel() instanceof ServerLevel serverLevel) {
                var oreVein = BedrockOreVeinSavedData.getOrCreate(serverLevel).getOreVeinWorldEntry(chunk.getPos().x, chunk.getPos().z);
                if (oreVein.getDefinition() != null) {
                    var left = 100 * oreVein.getOperationsRemaining() / BedrockOreVeinSavedData.MAXIMUM_VEIN_OPERATIONS;
                    for (var entry : oreVein.getDefinition().getBedrockVeinMaterials()) {
                        storage[0][0] = ArrayUtils.add(storage[0][0], new OreInfo(entry.getValue(), entry.getKey(), left, oreVein.getOreYield()));
                    }
                }
            }
        }

        @Override
        public int getItemColor(OreInfo item) {
            return item.material.getMaterialRGB();
        }


        @Override
        public IGuiTexture getItemIcon(OreInfo item) {
            Material material = item.material;
            ItemStack stack = ChemicalHelper.get(TagPrefix.get(ConfigHolder.INSTANCE.machines.bedrockOreDropTagPrefix), material);
            if (stack.isEmpty()) stack = ChemicalHelper.get(TagPrefix.crushed, material); // backup 1: crushed; if raw ore doesn't exist
            if (stack.isEmpty()) stack = ChemicalHelper.get(TagPrefix.gem, material); // backup 2: gem; if crushed ore doesn't exist
            if (stack.isEmpty()) stack = ChemicalHelper.get(TagPrefix.ore, material); // backup 3: just fallback to normal ore...
            return new ItemStackTexture(stack).scale(0.8f);
        }

        @Override
        public String getDescriptionId(OreInfo item) {
            return item.material.getUnlocalizedName();
        }

        @Override
        public String getUniqueID(OreInfo item) {
            return item.material.getName();
        }

        @Override
        public void serialize(OreInfo item, FriendlyByteBuf buf) {
            buf.writeUtf(GTRegistries.MATERIALS.getKey(item.material));
            buf.writeVarInt(item.weight);
            buf.writeVarInt(item.left);
            buf.writeVarInt(item.yield);
        }

        @Override
        public OreInfo deserialize(FriendlyByteBuf buf) {
            return new OreInfo(GTRegistries.MATERIALS.get(buf.readUtf()), buf.readVarInt(), buf.readVarInt(), buf.readVarInt());
        }

        @Override
        public Class<OreInfo> getItemClass() {
            return OreInfo.class;
        }

        @Override
        public void appendTooltips(OreInfo[] items, List<Component> tooltips, String selected) {
            int totalWeight = Arrays.stream(items).mapToInt(OreInfo::weight).sum();
            for (OreInfo item : items) {
                float chance = (float) item.weight / totalWeight * 100;
                tooltips.add(Component.translatable(getDescriptionId(item)).append(" (").append(Component.translatable("gtceu.gui.content.chance_1", String.format("%.1f", chance) + "%")).append(") --- %s (%s%%)".formatted(item.yield, item.left)));
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

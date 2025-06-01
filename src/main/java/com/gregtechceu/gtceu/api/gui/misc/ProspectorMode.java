package com.gregtechceu.gtceu.api.gui.misc;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialEntry;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.data.tag.TagUtil;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockfluid.BedrockFluidVeinSavedData;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockfluid.FluidVeinWorldEntry;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockore.BedrockOreVeinSavedData;
import com.gregtechceu.gtceu.api.gui.texture.ProspectingTexture;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTUtil;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.texture.ProgressTexture;
import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib.side.fluid.forge.FluidHelperImpl;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;

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
                                var entry = ChemicalHelper.getMaterialEntry(blockState.getBlock());
                                if (!entry.isEmpty()) {
                                    name = "material_" + entry.material();
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
                if (!mat.isNull()) {
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
                    if (!mat.isNull()) {
                        var list = new ArrayList<ItemStack>();
                        for (TagPrefix oreTag : TagPrefix.ORES.keySet()) {
                            for (var block : ChemicalHelper.getBlocks(new MaterialEntry(oreTag, mat))) {
                                list.add(new ItemStack(block));
                            }
                        }
                        return new ItemStackTexture(list.toArray(ItemStack[]::new)).scale(0.8f);
                    }
                }
                return new ItemStackTexture(new ItemStack(BuiltInRegistries.BLOCK.get(new ResourceLocation(name))))
                        .scale(0.8f);
            });
        }

        @Override
        public String getDescriptionId(String item) {
            if (item.startsWith("material_")) {
                var mat = GTMaterials.get(item.substring(9));
                if (!mat.isNull()) {
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
        public void appendTooltips(List<String[]> items, List<Component> tooltips, String selected) {
            Object2IntOpenHashMap<String> counter = new Object2IntOpenHashMap<>();
            for (var array : items) {
                for (String item : array) {
                    if (ProspectingTexture.SELECTED_ALL.equals(selected) || selected.equals(getUniqueID(item))) {
                        counter.addTo(item, 1);
                    }
                }
            }
            counter.forEach((item, count) -> tooltips
                    .add(Component.translatable(getDescriptionId(item)).append(" --- " + count)));
        }
    };

    @Accessors(fluent = true)
    @AllArgsConstructor
    public static final class FluidInfo {

        @Getter
        private final Fluid fluid;
        @Getter
        private final int yield;
        @Getter
        @Setter
        private int left;

        public static FluidInfo fromNbt(CompoundTag tag) {
            Fluid fluid = BuiltInRegistries.FLUID.get(new ResourceLocation(tag.getString("fluid")));
            int left = tag.getInt("left");
            int yield = tag.getInt("yield");
            return new FluidInfo(fluid, yield, left);
        }

        public CompoundTag toNbt() {
            CompoundTag tag = new CompoundTag();
            tag.putString("fluid", BuiltInRegistries.FLUID.getKey(fluid).toString());
            tag.putInt("left", left);
            tag.putInt("yield", yield);
            return tag;
        }

        public static FluidInfo fromVeinWorldEntry(@NotNull FluidVeinWorldEntry savedData) {
            if (savedData.getDefinition() == null) {
                return null;
            }
            return new FluidInfo(savedData.getDefinition().getStoredFluid().get(),
                    savedData.getFluidYield(),
                    100 * savedData.getOperationsRemaining() / BedrockFluidVeinSavedData.MAXIMUM_VEIN_OPERATIONS);
        }
    }

    public static ProspectorMode<FluidInfo> FLUID = new ProspectorMode<>("metaitem.prospector.mode.fluid", 1) {

        @Override
        public void scan(FluidInfo[][][] storage, LevelChunk chunk) {
            if (chunk.getLevel() instanceof ServerLevel serverLevel) {
                var fluidVein = BedrockFluidVeinSavedData.getOrCreate(serverLevel)
                        .getFluidVeinWorldEntry(chunk.getPos().x, chunk.getPos().z);
                if (fluidVein.getDefinition() != null) {
                    storage[0][0] = new FluidInfo[] {
                            FluidInfo.fromVeinWorldEntry(fluidVein)
                    };
                }
            }
        }

        @Override
        public int getItemColor(FluidInfo item) {
            var fluidStack = new FluidStack(item.fluid, item.yield);
            if (fluidStack.getFluid() == Fluids.LAVA) {
                return 0xFFFF7000;
            }
            return GTUtil.getFluidColor(fluidStack);
        }

        @Override
        public IGuiTexture getItemIcon(FluidInfo item) {
            return new ItemStackTexture(item.fluid.getBucket());
        }

        @Override
        public String getDescriptionId(FluidInfo item) {
            return new FluidStack(item.fluid, item.yield).getDisplayName().getString();
        }

        @Override
        public String getUniqueID(FluidInfo item) {
            return BuiltInRegistries.FLUID.getKey(item.fluid).toString();
        }

        @Override
        public void serialize(FluidInfo item, FriendlyByteBuf buf) {
            buf.writeUtf(BuiltInRegistries.FLUID.getKey(item.fluid).toString());
            buf.writeVarInt(item.yield);
            buf.writeVarInt(item.left);
        }

        @Override
        public FluidInfo deserialize(FriendlyByteBuf buf) {
            return new FluidInfo(BuiltInRegistries.FLUID.get(new ResourceLocation(buf.readUtf())), buf.readVarInt(),
                    buf.readVarInt());
        }

        @Override
        public Class<FluidInfo> getItemClass() {
            return FluidInfo.class;
        }

        @Override
        public void appendTooltips(List<FluidInfo[]> items, List<Component> tooltips, String selected) {
            for (var array : items) {
                for (FluidInfo item : array) {
                    tooltips.add(Component.translatable(getDescriptionId(item))
                            .append(" --- %s (%s%%)".formatted(item.yield, item.left)));
                }
            }
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void drawSpecialGrid(GuiGraphics graphics, FluidInfo[] items, int x, int y, int width, int height) {
            if (items.length > 0) {
                var item = items[0];
                double progress = item.left * 1.0 / Math.max(Math.min(item.left, 100), 1);
                float drawnU = (float) ProgressTexture.FillDirection.DOWN_TO_UP.getDrawnU(progress);
                float drawnV = (float) ProgressTexture.FillDirection.DOWN_TO_UP.getDrawnV(progress);
                float drawnWidth = (float) ProgressTexture.FillDirection.DOWN_TO_UP.getDrawnWidth(progress);
                float drawnHeight = (float) ProgressTexture.FillDirection.DOWN_TO_UP.getDrawnHeight(progress);
                DrawerHelper.drawFluidForGui(graphics,
                        FluidHelperImpl.toFluidStack(new FluidStack(item.fluid(), item.left)), 100,
                        (int) (x + drawnU * width), (int) (y + drawnV * height), ((int) (width * drawnWidth)),
                        ((int) (height * drawnHeight)));
            }
        }
    };

    public record OreInfo(Material material, int weight, int left, int yield) {

    }

    public static ProspectorMode<OreInfo> BEDROCK_ORE = new ProspectorMode<>("metaitem.prospector.mode.bedrock_ore",
            1) {

        @Override
        public void scan(OreInfo[][][] storage, LevelChunk chunk) {
            if (chunk.getLevel() instanceof ServerLevel serverLevel) {
                var oreVein = BedrockOreVeinSavedData.getOrCreate(serverLevel).getOreVeinWorldEntry(chunk.getPos().x,
                        chunk.getPos().z);
                if (oreVein.getDefinition() != null) {
                    var left = 100 * oreVein.getOperationsRemaining() / BedrockOreVeinSavedData.MAXIMUM_VEIN_OPERATIONS;
                    for (var entry : oreVein.getDefinition().materials()) {
                        storage[0][0] = ArrayUtils.add(storage[0][0],
                                new OreInfo(entry.material(), entry.weight(), left, oreVein.getOreYield()));
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
            ItemStack stack = ChemicalHelper.get(TagPrefix.get(ConfigHolder.INSTANCE.machines.bedrockOreDropTagPrefix),
                    material);
            if (stack.isEmpty()) stack = ChemicalHelper.get(TagPrefix.crushed, material); // backup 1: crushed; if raw
                                                                                          // ore doesn't exist
            if (stack.isEmpty()) stack = ChemicalHelper.get(TagPrefix.gem, material); // backup 2: gem; if crushed ore
                                                                                      // doesn't exist
            if (stack.isEmpty()) stack = ChemicalHelper.get(TagPrefix.ore, material); // backup 3: ore; if gem doesn't
                                                                                      // exist
            if (stack.isEmpty()) stack = ChemicalHelper.get(TagPrefix.dust, material); // backup 4: just fallback to
                                                                                       // dust...
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
            buf.writeResourceLocation(item.material.getResourceLocation());
            buf.writeVarInt(item.weight);
            buf.writeVarInt(item.left);
            buf.writeVarInt(item.yield);
        }

        @Override
        public OreInfo deserialize(FriendlyByteBuf buf) {
            ResourceLocation materialId = buf.readResourceLocation();
            return new OreInfo(
                    GTCEuAPI.materialManager.getRegistry(materialId.getNamespace()).get(materialId.getPath()),
                    buf.readVarInt(), buf.readVarInt(), buf.readVarInt());
        }

        @Override
        public Class<OreInfo> getItemClass() {
            return OreInfo.class;
        }

        @Override
        public void appendTooltips(List<OreInfo[]> items, List<Component> tooltips, String selected) {
            for (var array : items) {
                int totalWeight = Arrays.stream(array).mapToInt(OreInfo::weight).sum();
                for (OreInfo item : array) {
                    float chance = (float) item.weight / totalWeight * 100;
                    tooltips.add(Component.translatable(getDescriptionId(item)).append(" (")
                            .append(Component.translatable("gtceu.gui.content.chance_base",
                                    FormattingUtil.formatNumber2Places(chance)))
                            .append(") --- %s (%s%%)".formatted(item.yield, item.left)));
                }
            }
        }
    };

    public final String unlocalizedName;
    public final int cellSize;

    ProspectorMode(@NotNull String unlocalizedName, int cellSize) {
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

    public abstract void appendTooltips(List<T[]> items, List<Component> tooltips, String selected);

    @OnlyIn(Dist.CLIENT)
    public void drawSpecialGrid(GuiGraphics graphics, T[] items, int x, int y, int width, int height) {}
}

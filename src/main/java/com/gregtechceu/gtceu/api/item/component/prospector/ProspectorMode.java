package com.gregtechceu.gtceu.api.item.component.prospector;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialEntry;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockfluid.BedrockFluidVeinSavedData;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockfluid.FluidVeinWorldEntry;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockore.BedrockOreVeinSavedData;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.mui.drawable.CycleDrawable;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.core.mixins.BlockStateAccessor;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTUtil;
import com.gregtechceu.gtceu.utils.TagUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.fluids.FluidStack;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.drawable.FluidDrawable;
import brachy.modularui.drawable.GuiDraw;
import brachy.modularui.drawable.ItemDrawable;
import brachy.modularui.screen.viewport.GuiContext;
import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@SuppressWarnings("deprecation")
public abstract class ProspectorMode<T> {

    public static ProspectorMode<Either<Material, BlockState>> ORE = new ProspectorMode<>(
            "behavior.prospector.mode.ores", 16) {

        private static final String MATERIAL_PREFIX = "material_";

        private final Map<BlockState, Either<Material, BlockState>> BLOCK_CACHE = new HashMap<>();
        private final Map<Either<Material, BlockState>, IDrawable> ICON_CACHE = new HashMap<>();

        @Override
        public void scan(Either<Material, BlockState>[][][] storage, LevelChunk chunk) {
            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
            var oreTag = TagUtil.createBlockTag("ores");
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    for (int y = chunk.getMaxBuildHeight() - 1; y >= chunk.getMinBuildHeight(); y--) {
                        pos.set(x, y, z);
                        BlockState state = chunk.getBlockState(pos);
                        if (!state.is(oreTag)) continue;

                        Either<Material, BlockState> item = BLOCK_CACHE.computeIfAbsent(state, blockState -> {
                            MaterialEntry entry = ChemicalHelper.getMaterialEntry(blockState.getBlock());
                            if (!entry.isEmpty()) {
                                return Either.left(entry.material());
                            }
                            return Either.right(blockState);
                        });
                        storage[x][z] = ArrayUtils.add(storage[x][z], item);
                    }
                }
            }
        }

        @Override
        public int getItemColor(Either<Material, BlockState> item) {
            return item.map(Material::getMaterialARGB,
                    state -> ((BlockStateAccessor) state).gtceu$getDefaultMapColor().col | 0xFF000000);
        }

        @Override
        public IDrawable getItemIcon(Either<Material, BlockState> item) {
            return ICON_CACHE.computeIfAbsent(item, either -> {
                List<ItemLike> items = either.map(material -> {
                    List<ItemLike> oreItems = ChemicalHelper.getItems(new MaterialEntry(TagPrefix.rawOre, material));
                    if (oreItems.isEmpty()) {
                        oreItems = new ArrayList<>();
                        for (TagPrefix oreTag : TagPrefix.ORES.keySet()) {
                            oreItems.addAll(ChemicalHelper.getItems(new MaterialEntry(oreTag, material)));
                        }
                    }
                    return oreItems;
                }, state -> {
                    MaterialEntry entry = ChemicalHelper.getMaterialEntry(state.getBlock());
                    List<ItemLike> oreItems = ChemicalHelper.getItems(entry);
                    if (oreItems.isEmpty()) {
                        oreItems = List.of(state.getBlock().asItem());
                        if (oreItems.getFirst().asItem() == Items.AIR) {
                            oreItems = List.of(Items.BARRIER);
                        }
                    }
                    return oreItems;
                });
                ItemDrawable[] drawables = items.stream()
                        .map(itemLike -> itemLike.asItem().getDefaultInstance())
                        .map(ItemDrawable::new)
                        .toArray(ItemDrawable[]::new);

                return new CycleDrawable(drawables);
            });
        }

        @Override
        public Component getDescription(Either<Material, BlockState> item) {
            return item.map(Material::getLocalizedName, state -> state.getBlock().getName());
        }

        @Override
        public String getUniqueId(Either<Material, BlockState> item) {
            return item.map(material -> MATERIAL_PREFIX + material.getResourceLocation(),
                    state -> state.getBlockHolder().unwrapKey()
                            .map(ResourceKey::location)
                            .map(ResourceLocation::toString)
                            .orElse("Unknown entry ???"));
        }

        @Override
        public void serialize(Either<Material, BlockState> item, FriendlyByteBuf buf) {
            item.ifLeft(material -> {
                buf.writeBoolean(true);
                buf.writeResourceLocation(material.getResourceLocation());
            }).ifRight(state -> {
                buf.writeBoolean(false);
                buf.writeNbt(NbtUtils.writeBlockState(state));
            });
        }

        @Override
        public Either<Material, BlockState> deserialize(FriendlyByteBuf buf) {
            if (buf.readBoolean()) {
                return Either.left(GTRegistries.MATERIALS.get(buf.readResourceLocation()));
            } else {
                CompoundTag tag = buf.readNbt();
                assert tag != null;
                return Either.right(NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), tag));
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        public Class<Either<Material, BlockState>> getItemClass() {
            return (Class<Either<Material, BlockState>>) (Class<?>) Either.class;
        }

        @Override
        public void appendTooltips(List<Either<Material, BlockState>[]> items, List<Component> tooltips,
                                   String selected) {
            Object2IntOpenHashMap<Either<Material, BlockState>> counter = new Object2IntOpenHashMap<>();
            for (Either<Material, BlockState>[] array : items) {
                for (Either<Material, BlockState> item : array) {
                    if (selected == null || selected.equals(this.getUniqueId(item))) {
                        counter.addTo(item, 1);
                    }
                }
            }
            counter.forEach((item, count) -> tooltips
                    .add(Component.empty().append(getDescription(item)).append(" --- %s".formatted(count))));
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

        public FluidStack asStack() {
            return new FluidStack(this.fluid, this.yield);
        }

        public static FluidInfo fromNbt(CompoundTag tag) {
            Fluid fluid = BuiltInRegistries.FLUID.get(ResourceLocation.parse(tag.getString("fluid")));
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
            return new FluidInfo(savedData.getDefinition().value().getStoredFluid(),
                    savedData.getFluidYield(),
                    100 * savedData.getOperationsRemaining() / BedrockFluidVeinSavedData.MAXIMUM_VEIN_OPERATIONS);
        }
    }

    public static ProspectorMode<FluidInfo> FLUID = new ProspectorMode<>("behavior.prospector.mode.fluid", 1) {

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
            var fluidStack = item.asStack();
            if (fluidStack.getFluid() == Fluids.LAVA) {
                return 0xFFFF7000;
            }
            return GTUtil.getFluidColor(fluidStack);
        }

        @Override
        public IDrawable getItemIcon(FluidInfo item) {
            return new FluidDrawable(item.asStack());
        }

        @Override
        public Component getDescription(FluidInfo item) {
            return item.fluid.getFluidType().getDescription(new FluidStack(item.fluid, item.left));
        }

        @Override
        public String getUniqueId(FluidInfo item) {
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
            return new FluidInfo(BuiltInRegistries.FLUID.get(ResourceLocation.parse(buf.readUtf())), buf.readVarInt(),
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
                    tooltips.add(Component.empty().append(getDescription(item))
                            .append(" --- %s (%s%%)".formatted(item.yield, item.left)));
                }
            }
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void drawSpecialGrid(GuiContext context, FluidInfo[] items, int x, int y, int width, int height) {
            if (items.length == 0) {
                return;
            }
            FluidInfo item = items[0];
            float filled = item.left / Math.max(Math.min(item.left, 100.0f), 1.0f);

            GuiDraw.drawFluidTexture(context.getGraphics(), item.asStack(),
                    x * width, y + (1.0f - filled) * height, width, height * filled,
                    context.getCurrentDrawingZ());
        }
    };

    public record BedrockOreInfo(Material material, int weight, int left, int yield) {}

    public static ProspectorMode<BedrockOreInfo> BEDROCK_ORE = new ProspectorMode<>(
            "behavior.prospector.mode.bedrock_ore",
            1) {

        @Override
        public void scan(BedrockOreInfo[][][] storage, LevelChunk chunk) {
            if (chunk.getLevel() instanceof ServerLevel serverLevel) {
                var oreVein = BedrockOreVeinSavedData.getOrCreate(serverLevel).getOreVeinWorldEntry(chunk.getPos().x,
                        chunk.getPos().z);
                if (oreVein.getDefinition() != null) {
                    var left = 100 * oreVein.getOperationsRemaining() / BedrockOreVeinSavedData.MAXIMUM_VEIN_OPERATIONS;
                    for (var entry : oreVein.getDefinition().value().materials()) {
                        storage[0][0] = ArrayUtils.add(storage[0][0],
                                new BedrockOreInfo(entry.material(), entry.weight(), left, oreVein.getOreYield()));
                    }
                }
            }
        }

        @Override
        public int getItemColor(BedrockOreInfo item) {
            return item.material.getMaterialARGB();
        }

        @Override
        public IDrawable getItemIcon(BedrockOreInfo item) {
            Material material = item.material;
            ItemStack stack = GTUtil.getFirstNonEmpty(
                    ChemicalHelper.get(TagPrefix.get(ConfigHolder.INSTANCE.machines.bedrockOreDropTagPrefix), material),
                    ChemicalHelper.get(TagPrefix.crushed, material),
                    ChemicalHelper.get(TagPrefix.gem, material),
                    ChemicalHelper.get(TagPrefix.ore, material),
                    ChemicalHelper.get(TagPrefix.dust, material));
            return new ItemDrawable(stack);
        }

        @Override
        public Component getDescription(BedrockOreInfo item) {
            return item.material.getLocalizedName();
        }

        @Override
        public String getUniqueId(BedrockOreInfo item) {
            return item.material.getName();
        }

        @Override
        public void serialize(BedrockOreInfo item, FriendlyByteBuf buf) {
            buf.writeResourceLocation(item.material.getResourceLocation());
            buf.writeVarInt(item.weight);
            buf.writeVarInt(item.left);
            buf.writeVarInt(item.yield);
        }

        @Override
        public BedrockOreInfo deserialize(FriendlyByteBuf buf) {
            ResourceLocation materialId = buf.readResourceLocation();
            return new BedrockOreInfo(
                    GTRegistries.MATERIALS.get(materialId),
                    buf.readVarInt(), buf.readVarInt(), buf.readVarInt());
        }

        @Override
        public Class<BedrockOreInfo> getItemClass() {
            return BedrockOreInfo.class;
        }

        @Override
        public void appendTooltips(List<BedrockOreInfo[]> items, List<Component> tooltips, String selected) {
            for (var array : items) {
                int totalWeight = Arrays.stream(array).mapToInt(BedrockOreInfo::weight).sum();
                for (BedrockOreInfo item : array) {
                    float chance = (float) item.weight / totalWeight * 100;
                    tooltips.add(Component.empty().append(getDescription(item))
                            .append(" (")
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

    public abstract IDrawable getItemIcon(T item);

    public abstract Component getDescription(T item);

    public abstract String getUniqueId(T item);

    public abstract void serialize(T item, FriendlyByteBuf buf);

    public abstract T deserialize(FriendlyByteBuf buf);

    public abstract Class<T> getItemClass();

    public abstract void appendTooltips(List<T[]> items, List<Component> tooltips, String selected);

    @OnlyIn(Dist.CLIENT)
    public void drawSpecialGrid(GuiContext graphics, T[] items, int x, int y, int width, int height) {}
}

package com.gregtechceu.gtceu.utils;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.fluid.store.FluidStorageKeys;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.material.material.Material;
import com.gregtechceu.gtceu.api.material.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.tag.TagPrefix;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.core.mixins.MapColorAccessor;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.InputConstants;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Function;

import static com.gregtechceu.gtceu.api.material.material.properties.PropertyKey.HAZARD;
import static com.gregtechceu.gtceu.utils.FormattingUtil.DECIMAL_FORMAT_SIC_2F;

public class GTUtil {

    public static final Direction[] DIRECTIONS = Direction.values();
    public static final @Nullable Direction @NotNull [] DIRECTIONS_WITH_NULL = ArrayUtils.add(DIRECTIONS, null);

    public static final ImmutableList<BlockPos> NON_CORNER_NEIGHBOURS = Util.make(() -> {
        var builder = ImmutableList.<BlockPos>builderWithExpectedSize(18);
        BlockPos.betweenClosedStream(-1, -1, -1, 1, 1, 1)
                .filter((pos) -> (pos.getX() == 0 || pos.getY() == 0 || pos.getZ() == 0) && !pos.equals(BlockPos.ZERO))
                .map(BlockPos::immutable)
                .forEach(builder::add);
        return builder.build();
    });

    private static final Object2IntMap<String> RVN = new Object2IntArrayMap<>(GTValues.VN, GTValues.ALL_TIERS);

    /**
     * Convenience method to get from VN -> Tier
     *
     * @return the voltage tier by name, -1 if the tier name isn't valid
     */
    public static int getTierByName(String name) {
        return RVN.getOrDefault(name, -1);
    }

    @Nullable
    public static Direction determineWrenchingSide(Direction facing, float x, float y, float z) {
        Direction opposite = facing.getOpposite();
        switch (facing) {
            case DOWN, UP -> {
                if (x < 0.25) {
                    if (z < 0.25) return opposite;
                    if (z > 0.75) return opposite;
                    return Direction.WEST;
                }
                if (x > 0.75) {
                    if (z < 0.25) return opposite;
                    if (z > 0.75) return opposite;
                    return Direction.EAST;
                }
                if (z < 0.25) return Direction.NORTH;
                if (z > 0.75) return Direction.SOUTH;
                return facing;
            }
            case NORTH, SOUTH -> {
                if (x < 0.25) {
                    if (y < 0.25) return opposite;
                    if (y > 0.75) return opposite;
                    return Direction.WEST;
                }
                if (x > 0.75) {
                    if (y < 0.25) return opposite;
                    if (y > 0.75) return opposite;
                    return Direction.EAST;
                }
                if (y < 0.25) return Direction.DOWN;
                if (y > 0.75) return Direction.UP;
                return facing;
            }
            case WEST, EAST -> {
                if (z < 0.25) {
                    if (y < 0.25) return opposite;
                    if (y > 0.75) return opposite;
                    return Direction.NORTH;
                }
                if (z > 0.75) {
                    if (y < 0.25) return opposite;
                    if (y > 0.75) return opposite;
                    return Direction.SOUTH;
                }
                if (y < 0.25) return Direction.DOWN;
                if (y > 0.75) return Direction.UP;
                return facing;
            }
        }
        return null;
    }

    /**
     * Calculates on which side the neighbor is relative to the main pos.
     *
     * @param main     main pos
     * @param neighbor neighbor pos
     * @return position of neighbor relative to main or null the neighbor pos is not a neighbor
     */
    @Nullable
    public static Direction getFacingToNeighbor(@NotNull BlockPos main, @NotNull BlockPos neighbor) {
        int difX = neighbor.getX() - main.getX();
        int difY = neighbor.getY() - main.getY();
        int difZ = neighbor.getZ() - main.getZ();
        if (difX != 0) {
            if (difY != 0 || difZ != 0 || (difX != 1 && difX != -1)) return null;
            return difX > 0 ? Direction.EAST : Direction.WEST;
        }
        if (difY != 0) {
            if (difZ != 0 || (difY != 1 && difY != -1)) return null;
            return difY > 0 ? Direction.UP : Direction.DOWN;
        }
        if (difZ != 0) {
            if (difZ != 1 && difZ != -1) return null;
            return difZ > 0 ? Direction.SOUTH : Direction.NORTH;
        }
        return null;
    }

    public static float getExplosionPower(long voltage) {
        return getTierByVoltage(voltage) + 1;
    }

    /**
     * @param array Array sorted with natural order
     * @param value Value to search for
     * @return Index of the nearest value lesser or equal than {@code value},
     *         or {@code -1} if there's no entry matching the condition
     */
    public static int nearestLesserOrEqual(long @NotNull [] array, long value) {
        int low = 0, high = array.length - 1;
        while (true) {
            int median = (low + high) / 2;
            if (array[median] <= value) {
                if (low == high) return low;
                low = median + 1;
            } else {
                if (low == high) return low - 1;
                high = median - 1;
            }
        }
    }

    /**
     * @param array Array sorted with natural order
     * @param value Value to search for
     * @return Index of the nearest value lesser than {@code value},
     *         or {@code -1} if there's no entry matching the condition
     */
    public static int nearestLesser(long @NotNull [] array, long value) {
        int low = 0, high = array.length - 1;
        while (true) {
            int median = (low + high) / 2;
            if (array[median] < value) {
                if (low == high) return low;
                low = median + 1;
            } else {
                if (low == high) return low - 1;
                high = median - 1;
            }
        }
    }

    /**
     * @return Lowest tier of the voltage that can handle {@code voltage}; that is,
     *         a voltage with value greater than equal than {@code voltage}. If there's no
     *         tier that can handle it, {@code MAX} is returned.
     */
    public static byte getTierByVoltage(long voltage) {
        if (voltage > Integer.MAX_VALUE) {
            return GTValues.MAX;
        }
        return getOCTierByVoltage(voltage);
    }

    public static byte getOCTierByVoltage(long voltage) {
        if (voltage <= GTValues.V[GTValues.ULV]) {
            return GTValues.ULV;
        }
        return (byte) ((62 - Long.numberOfLeadingZeros(voltage - 1)) >> 1);
    }

    /**
     * Ex: This method turns both 1024 and 512 into HV.
     *
     * @return the highest voltage tier with value below or equal to {@code voltage}, or
     *         {@code ULV} if there's no tier below
     */
    public static byte getFloorTierByVoltage(long voltage) {
        if (voltage < GTValues.V[GTValues.LV]) {
            return GTValues.ULV;
        }
        if (voltage == GTValues.VEX[GTValues.MAX_TRUE]) {
            return GTValues.MAX_TRUE;
        }

        return (byte) ((60 - Long.numberOfLeadingZeros(voltage)) >> 1);
    }

    /**
     * Copies first non-empty ItemStack from stacks.
     *
     * @param stacks list of candidates for copying
     * @return a copy of ItemStack, or {@link ItemStack#EMPTY} if all the candidates are empty
     * @throws IllegalArgumentException if {@code stacks} is empty
     */
    public static @NotNull ItemStack copyFirst(@NotNull ItemStack... stacks) {
        if (stacks.length == 0) {
            throw new IllegalArgumentException("Empty ItemStack candidates");
        }
        for (ItemStack stack : stacks) {
            if (!stack.isEmpty()) {
                return stack.copy();
            }
        }
        return ItemStack.EMPTY;
    }

    /**
     * Copies first non-empty ItemStack from stacks, with new stack size.
     *
     * @param stacks list of candidates for copying
     * @return a copy of ItemStack, or {@link ItemStack#EMPTY} if all the candidates are empty
     * @throws IllegalArgumentException if {@code stacks} is empty
     */
    public static @NotNull ItemStack copyFirst(int newCount, @NotNull ItemStack... stacks) {
        if (stacks.length == 0) {
            throw new IllegalArgumentException("Empty ItemStack candidates");
        }
        for (ItemStack stack : stacks) {
            if (!stack.isEmpty()) {
                return stack.copyWithCount(newCount);
            }
        }
        return ItemStack.EMPTY;
    }

    public static <M> M getItem(List<? extends M> list, int index, M replacement) {
        if (index >= 0 && index < list.size())
            return list.get(index);
        return replacement;
    }

    public static <T extends WeightedEntry> @Nullable T getRandomItem(RandomSource random, List<T> randomList) {
        if (randomList.isEmpty()) return null;
        int size = randomList.size();
        int[] baseOffsets = new int[size];
        int currentIndex = 0;
        for (int i = 0; i < size; i++) {
            int weight = randomList.get(i).weight();
            if (weight <= 0) {
                throw new IllegalArgumentException("Invalid weight: " + weight);
            }
            currentIndex += weight;
            baseOffsets[i] = currentIndex;
        }
        int randomValue = random.nextInt(currentIndex);
        for (int i = 0; i < size; i++) {
            if (randomValue < baseOffsets[i]) return randomList.get(i);
        }
        throw new IllegalArgumentException("Invalid weight");
    }

    public static <T extends WeightedEntry> @Nullable T getRandomItem(List<T> randomList) {
        return getRandomItem(GTValues.RNG, randomList);
    }

    @SuppressWarnings("unchecked")
    public static <T, R> Class<T> getActualTypeParameter(Class<? extends R> thisClass, int index) {
        Type type = thisClass.getGenericSuperclass();
        return (Class<T>) ((ParameterizedType) type).getActualTypeArguments()[index];
    }

    public static boolean isShiftDown() {
        if (GTCEu.isClientSide()) {
            var id = Minecraft.getInstance().getWindow().getWindow();
            return InputConstants.isKeyDown(id, GLFW.GLFW_KEY_LEFT_SHIFT) ||
                    InputConstants.isKeyDown(id, GLFW.GLFW_KEY_LEFT_SHIFT);
        }
        return false;
    }

    public static boolean isCtrlDown() {
        if (GTCEu.isClientSide()) {
            var id = Minecraft.getInstance().getWindow().getWindow();
            return InputConstants.isKeyDown(id, GLFW.GLFW_KEY_LEFT_CONTROL) ||
                    InputConstants.isKeyDown(id, GLFW.GLFW_KEY_RIGHT_CONTROL);
        }
        return false;
    }

    public static boolean isAltDown() {
        if (GTCEu.isClientSide()) {
            var id = Minecraft.getInstance().getWindow().getWindow();
            return InputConstants.isKeyDown(id, GLFW.GLFW_KEY_LEFT_ALT) ||
                    InputConstants.isKeyDown(id, GLFW.GLFW_KEY_RIGHT_ALT);
        }
        return false;
    }

    public static String formatLongNumber(long number, long threshold) {
        return (number > threshold) ? DECIMAL_FORMAT_SIC_2F.format(number) : String.valueOf(number);
    }

    public static String formatLongNumber(long number) {
        return formatLongNumber(number, 10000);
    }

    public static String getStringRemainTime(long time, long threshold) {
        String s = Component.translatable("gtceu.jade.seconds", time % 60).getString();
        time /= 60;
        if (time > 0) {
            s = Component.translatable("gtceu.jade.minutes", time % 60).getString() + " " + s;
            time /= 60;
            if (time > 0) {
                s = Component.translatable("gtceu.jade.hours", time % 60).getString() + " " + s;
                time /= 60;
                if (time > 0) {
                    s = Component.translatable("gtceu.jade.days", time % 24).getString() + " " + s;
                    time /= 24;
                    if (time > 0) {
                        s = Component.translatable("gtceu.jade.years", formatLongNumber(time, threshold)).getString() +
                                " " + s;
                    }
                }
            }
        }
        return s;
    }

    public static String getStringRemainTime(long time) {
        return getStringRemainTime(time, 10000);
    }

    public static boolean isFluidStackAmountDivisible(FluidStack fluidStack, int divisor) {
        return fluidStack.getAmount() % divisor == 0 && fluidStack.getAmount() % divisor != fluidStack.getAmount() &&
                fluidStack.getAmount() / divisor != 0;
    }

    public static boolean isItemStackCountDivisible(ItemStack itemStack, int divisor) {
        return itemStack.getCount() % divisor == 0 && itemStack.getCount() % divisor != itemStack.getCount() &&
                itemStack.getCount() / divisor != 0;
    }

    public static int getItemBurnTime(Item item) {
        return item.getDefaultInstance().getBurnTime(RecipeType.SMELTING);
    }

    public static int getPumpBiomeModifier(Holder<Biome> biome) {
        if (biome.is(BiomeTags.IS_NETHER)) {
            return -1;
        }

        if (biome.is(BiomeTags.IS_DEEP_OCEAN) || biome.is(BiomeTags.IS_OCEAN) || biome.is(BiomeTags.IS_BEACH) ||
                biome.is(BiomeTags.IS_RIVER)) {
            return FluidType.BUCKET_VOLUME;
        } else if (biome.is(BiomeTags.IS_JUNGLE)) {
            return FluidType.BUCKET_VOLUME * 35 / 100;
        } else if (biome.is(Tags.Biomes.IS_SWAMP) || biome.is(Tags.Biomes.IS_WET)) {
            return FluidType.BUCKET_VOLUME * 4 / 5;
        } else if (biome.is(Tags.Biomes.IS_SNOWY)) {
            return FluidType.BUCKET_VOLUME * 3 / 10;
        } else if (biome.is(Tags.Biomes.IS_PLAINS) || biome.is(BiomeTags.IS_FOREST)) {
            return FluidType.BUCKET_VOLUME / 4;
        } else if (biome.is(Tags.Biomes.IS_COLD)) {
            return FluidType.BUCKET_VOLUME * 175 / 1000;
        } else if (biome.is(Tags.Biomes.IS_SANDY)) {
            return FluidType.BUCKET_VOLUME * 170 / 1000;
        }
        return FluidType.BUCKET_VOLUME / 10;
    }

    /**
     * Determines dye color nearest to specified RGB color
     */
    public static DyeColor determineDyeColor(int rgbColor) {
        return closestColor(rgbColor, DyeColor.values(), DyeColor::getTextColor);
    }

    /**
     * Determines map color nearest to specified RGB color
     */
    public static MapColor determineMapColor(int rgbColor) {
        return closestColor(rgbColor, MapColorAccessor.gtceu$getMaterialColors(),
                c -> c.calculateRGBColor(MapColor.Brightness.NORMAL));
    }

    private static <T> T closestColor(int rgbColor, T[] colors, Function<T, Integer> extractRgbColor) {
        float[] c = GradientUtil.getRGB(rgbColor);

        double min = Double.MAX_VALUE;
        T minColor = null;
        for (T color : colors) {
            float[] c2 = GradientUtil.getRGB(extractRgbColor.apply(color));

            double distance = (c[0] - c2[0]) * (c[0] - c2[0]) + (c[1] - c2[1]) * (c[1] - c2[1]) +
                    (c[2] - c2[2]) * (c[2] - c2[2]);

            if (Double.compare(min, distance) > 0) {
                minColor = color;
                min = distance;
            }
        }
        return minColor;
    }

    public static int convertRGBtoARGB(int colorValue) {
        return convertRGBtoARGB(colorValue, 0xFF);
    }

    public static int convertRGBtoARGB(int colorValue, int opacity) {
        // preserve existing opacity if present
        if (((colorValue >> 24) & 0xFF) != 0) return colorValue;
        return opacity << 24 | colorValue;
    }

    /**
     * @param material the material to use
     * @return the correct "molten" fluid for a material
     */
    @Nullable
    public static Fluid getMoltenFluid(@NotNull Material material) {
        if (material.hasProperty(PropertyKey.ALLOY_BLAST))
            return material.getProperty(PropertyKey.FLUID).getStorage().get(FluidStorageKeys.MOLTEN);
        if (!TagPrefix.ingotHot.doGenerateItem(material) && material.hasProperty(PropertyKey.FLUID))
            return material.getProperty(PropertyKey.FLUID).getStorage().get(FluidStorageKeys.LIQUID);
        return null;
    }

    public static int getFluidColor(FluidStack fluid) {
        return IClientFluidTypeExtensions.of(fluid.getFluid()).getTintColor(fluid);
    }

    public static boolean canSeeSunClearly(Level world, BlockPos blockPos) {
        if (!world.canSeeSky(blockPos.above())) {
            return false;
        }

        Holder<Biome> biome = world.getBiome(blockPos.above());
        if (world.isRaining()) {
            if (biome.value().warmEnoughToRain(blockPos.above()) || biome.value().coldEnoughToSnow(blockPos.above())) {
                return false;
            }
        }

        if (world.getBiome(blockPos.above()).is(BiomeTags.IS_END)) {
            return false;
        }

        ResourceLocation javdVoidBiome = ResourceLocation.fromNamespaceAndPath(GTValues.MODID_JAVD, "void");
        if (GTCEu.isModLoaded(GTValues.MODID_JAVD) && biome.is(javdVoidBiome)) {
            return !world.isDay();
        } else return world.isDay();
    }

    /**
     * @param state the blockstate to check
     * @return if the block is a snow layer or snow block
     */
    public static boolean isBlockSnow(@NotNull BlockState state) {
        return state.is(Blocks.SNOW) || state.is(Blocks.SNOW_BLOCK);
    }

    /**
     * Attempt to break a (single) snow layer at the given BlockPos.
     * Will also turn snow blocks into snow layers at height 7.
     *
     * @return true if the passed IBlockState was valid snow block
     */
    public static boolean tryBreakSnow(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state,
                                       boolean playSound) {
        boolean success = false;
        if (state.is(Blocks.SNOW_BLOCK)) {
            level.setBlock(pos, Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, 7),
                    Block.UPDATE_ALL_IMMEDIATE);
            success = true;
        } else if (state.getBlock() == Blocks.SNOW) {
            int layers = state.getValue(SnowLayerBlock.LAYERS);
            if (layers == 1) {
                level.destroyBlock(pos, false);
            } else {
                level.setBlock(pos, Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, layers - 1),
                        Block.UPDATE_ALL_IMMEDIATE);
            }
            success = true;
        }

        if (success && playSound) {
            level.playSound(null, pos, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 1.0f, 1.0f);
        }

        return success;
    }

    public static void appendHazardTooltips(Material material, List<Component> tooltipComponents) {
        if (!ConfigHolder.INSTANCE.gameplay.hazardsEnabled || !material.hasProperty(HAZARD)) return;

        if (GTUtil.isShiftDown()) {
            tooltipComponents.add(Component.translatable("gtceu.medical_condition.description_shift"));
            tooltipComponents.add(Component
                    .translatable("gtceu.medical_condition." + material.getProperty(HAZARD).condition.name));
            tooltipComponents.add(Component.translatable("gtceu.hazard_trigger.description"));
            tooltipComponents.add(Component
                    .translatable("gtceu.hazard_trigger." + material.getProperty(HAZARD).hazardTrigger.name()));
            return;
        }
        tooltipComponents.add(Component.translatable("gtceu.medical_condition.description"));
    }

    public static Tuple<ItemStack, MutableComponent> getMaintenanceText(byte flag) {
        return switch (flag) {
            case 0 -> new Tuple<>(ToolItemHelper.getToolItem(GTToolType.WRENCH),
                    Component.translatable("gtceu.top.maintenance.wrench"));
            case 1 -> new Tuple<>(ToolItemHelper.getToolItem(GTToolType.SCREWDRIVER),
                    Component.translatable("gtceu.top.maintenance.screwdriver"));
            case 2 -> new Tuple<>(ToolItemHelper.getToolItem(GTToolType.SOFT_MALLET),
                    Component.translatable("gtceu.top.maintenance.soft_mallet"));
            case 3 -> new Tuple<>(ToolItemHelper.getToolItem(GTToolType.HARD_HAMMER),
                    Component.translatable("gtceu.top.maintenance.hard_hammer"));
            case 4 -> new Tuple<>(ToolItemHelper.getToolItem(GTToolType.WIRE_CUTTER),
                    Component.translatable("gtceu.top.maintenance.wire_cutter"));
            default -> new Tuple<>(ToolItemHelper.getToolItem(GTToolType.CROWBAR),
                    Component.translatable("gtceu.top.maintenance.crowbar"));
        };
    }

    public static void addPotionTooltip(List<FoodProperties.PossibleEffect> effects, List<Component> list) {
        if (!effects.isEmpty()) {
            list.add(Component.translatable("gtceu.tooltip.potion.header"));
        }
        effects.forEach(eff -> {
            var effect = eff.effect();
            float probability = eff.probability();
            list.add(Component.translatable("gtceu.tooltip.potion.each",
                    Component.translatable(effect.getDescriptionId())
                            .setStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)),
                    Component.translatable("enchantment.level." + (effect.getAmplifier() + 1))
                            .setStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)),
                    Component.literal(String.valueOf(effect.getDuration()))
                            .setStyle(Style.EMPTY.withColor(ChatFormatting.RED)),
                    Component.literal(String.valueOf(100 * probability))
                            .setStyle(Style.EMPTY.withColor(ChatFormatting.GREEN))));
        });
    }

    /**
     * Returns the slot type based on the slot group and the index inside that group.
     */
    public static EquipmentSlot equipmentSlotByTypeAndIndex(EquipmentSlot.Type slotType, int slotIndex) {
        for (EquipmentSlot equipmentslot : EquipmentSlot.values()) {
            if (equipmentslot.getType() == slotType && equipmentslot.getIndex() == slotIndex) {
                return equipmentslot;
            }
        }

        throw new IllegalArgumentException("Invalid slot '" + slotType + "': " + slotIndex);
    }

    public static <T> ArrayList<T> list(T obj) {
        return new ArrayList<>(List.of(obj));
    }
}

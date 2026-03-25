package com.gregtechceu.gtceu.api.machine.multiblock;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.*;
import java.util.function.Predicate;

public final class MultiblockPreviewHighlightRegistry {

    public static final MultiblockPreviewHighlightRegistry INSTANCE = new MultiblockPreviewHighlightRegistry();

    public static final int INPUT_COLOR = 0x00ff00ff;
    public static final int OUTPUT_COLOR = 0xff8000ff;
    public static final int POWER_COLOR = 0xffff00ff;
    public static final int MAINTENANCE_COLOR = 0x00ffffff;
    public static final int MUFFLER_COLOR = 0x800080ff;
    public static final int PARALLEL_COLOR = 0xf0ffffff;
    public static final int MIXED_COLOR = 0x3b2525ff;

    private static final List<HighlightRule> RULES = new ArrayList<>();

    static {
        registerAbilityHighlight(INPUT_COLOR,
                PartAbility.IMPORT_ITEMS,
                PartAbility.IMPORT_FLUIDS,
                PartAbility.IMPORT_FLUIDS_1X,
                PartAbility.IMPORT_FLUIDS_4X,
                PartAbility.IMPORT_FLUIDS_9X,
                PartAbility.STEAM_IMPORT_ITEMS);
        registerAbilityHighlight(OUTPUT_COLOR,
                PartAbility.EXPORT_ITEMS,
                PartAbility.EXPORT_FLUIDS,
                PartAbility.EXPORT_FLUIDS_1X,
                PartAbility.EXPORT_FLUIDS_4X,
                PartAbility.EXPORT_FLUIDS_9X,
                PartAbility.STEAM_EXPORT_ITEMS);
        registerAbilityHighlight(POWER_COLOR,
                PartAbility.INPUT_ENERGY,
                PartAbility.OUTPUT_ENERGY,
                PartAbility.SUBSTATION_INPUT_ENERGY,
                PartAbility.SUBSTATION_OUTPUT_ENERGY,
                PartAbility.INPUT_LASER,
                PartAbility.OUTPUT_LASER,
                PartAbility.STEAM);
        registerAbilityHighlight(MAINTENANCE_COLOR, PartAbility.MAINTENANCE);
        registerAbilityHighlight(MUFFLER_COLOR, PartAbility.MUFFLER);
        registerAbilityHighlight(PARALLEL_COLOR, PartAbility.PARALLEL_HATCH);
    }

    private MultiblockPreviewHighlightRegistry() {}

    public static void registerAbilityHighlight(int color, PartAbility... abilities) {
        List<PartAbility> filteredAbilities = Arrays.stream(abilities)
                .filter(Objects::nonNull)
                .toList();
        if (filteredAbilities.isEmpty()) return;
        registerHighlight(color, block -> filteredAbilities.stream().anyMatch(ability -> ability.isApplicable(block)));
    }

    public static void registerHighlight(int color, Predicate<Block> matcher) {
        RULES.add(new HighlightRule(color, matcher));
    }

    public static int resolveColor(Collection<ItemStack> candidates) {
        Set<Integer> matchedColors = new LinkedHashSet<>();
        for (ItemStack candidate : candidates) {
            Block block = Block.byItem(candidate.getItem());
            if (block == Blocks.AIR) continue;
            for (HighlightRule rule : RULES) {
                if (rule.matches(block)) {
                    matchedColors.add(rule.color());
                    if (matchedColors.size() > 1) {
                        return MIXED_COLOR;
                    }
                }
            }
        }
        return matchedColors.isEmpty() ? 0 : matchedColors.iterator().next();
    }

    private record HighlightRule(int color, Predicate<Block> matcher) {

        private boolean matches(Block block) {
            return matcher.test(block);
        }
    }
}

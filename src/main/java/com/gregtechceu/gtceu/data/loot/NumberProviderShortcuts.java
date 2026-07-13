package com.gregtechceu.gtceu.data.loot;

import com.gregtechceu.gtceu.core.mixins.BinomialDistributionGeneratorAccessor;
import com.gregtechceu.gtceu.core.mixins.UniformGeneratorAccessor;
import lombok.experimental.UtilityClass;
import net.minecraft.world.level.storage.loot.providers.number.*;

/**
 * Utility class for working with loot tables and their number providers. To use this class efficiently, add the
 * following static imports to your class:
 * <pre>{@code
 * import static com.gregtechceu.gtceu.data.loot.NumberProviderShortcuts.*;
 * import static net.minecraft.world.level.storage.loot.LootTable.lootTable;
 * import static net.minecraft.world.level.storage.loot.LootPool.lootPool;
 * import static net.minecraft.world.level.storage.loot.entries.LootItem.lootTableItem;
 * import static net.minecraft.world.level.storage.loot.functions.SetItemCountFunction.*;
 * import static net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition.*;
 * import static net.minecraft.world.level.storage.loot.providers.number.ConstantValue.*;
 * import static net.minecraft.world.level.storage.loot.providers.number.UniformGenerator.*;
 * import static net.minecraft.world.level.storage.loot.providers.number.BinomialDistributionGenerator.*;
 * }</pre>
 */
@UtilityClass
public class NumberProviderShortcuts {

    // region CONSTANT VALUE

    public static ConstantValue constant(float value) {
        return ConstantValue.exactly(value);
    }

    // endregion //
    // region UNIFORM GENERATOR

    public static UniformGenerator between(NumberProvider min, NumberProvider max) {
        return UniformGeneratorAccessor.callInit(min, max);
    }

    public static UniformGenerator between(float min, NumberProvider max) {
        return between(constant(min), max);
    }

    public static UniformGenerator between(NumberProvider min, float max) {
        return between(min, constant(max));
    }

    // endregion //
    // region BINOMIAL DISTRIBUTION GENERATOR

    public static BinomialDistributionGenerator binomial(NumberProvider n, NumberProvider p) {
        return BinomialDistributionGeneratorAccessor.callInit(n, p);
    }

    public static BinomialDistributionGenerator binomial(int n, NumberProvider p) {
        return binomial(constant(n), p);
    }

    public static BinomialDistributionGenerator binomial(NumberProvider n, float p) {
        return binomial(n, constant(p));
    }

    // endregion //
}

package com.gregtechceu.gtceu.api.loot.modifier;

import com.gregtechceu.gtceu.common.data.loot.GTGlobalLootModifiers;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.ApiStatus;

/**
 * <p>
 * Loot modifier that rolls one loot table (the "subtable" and adds the results to the loot being modified (the "target
 * table").
 * Loot modifiers are not rolled for the subtable, as that could result in the subtables'
 * items being modified twice (by downstream loot modifiers modifying the target table).
 * </p>
 *
 * <p>
 * Json format:
 * 
 * <pre>{@code
 * {
 *   "type": "gtceu:add_table",
 *   "conditions": [], // conditions block to predicate target tables by
 *   "table": "namespace:loot_table_id" // subtable to roll loot for to add to the target table(s)
 * }
 * }</pre>
 * 
 * </p>
 */
// copy of NeoForge's AddTableLootModifier, tweaked slightly to work on 1.20
@Accessors(fluent = true)
public class AddTableLootModifier extends LootModifier {

    /**
     * @see GTGlobalLootModifiers#ADD_TABLE_LOOT_MODIFIER
     */
    // spotless:off
    @ApiStatus.Internal
    public static final Codec<AddTableLootModifier> CODEC = RecordCodecBuilder.create(instance -> LootModifier.codecStart(instance).and(
            ResourceLocation.CODEC.fieldOf("table").forGetter(AddTableLootModifier::table)
    ).apply(instance, AddTableLootModifier::new));
    // spotless:on

    @Getter
    private final ResourceLocation table;

    public AddTableLootModifier(LootItemCondition[] conditionsIn, ResourceLocation table) {
        super(conditionsIn);
        this.table = table;
    }

    @SuppressWarnings("deprecation")
    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        context.getResolver().getElementOptional(LootDataType.TABLE, this.table).ifPresent(extraTable -> {
            // Don't run loot modifiers for subtables;
            // the added loot will be modifiable by downstream loot modifiers modifying the target table,
            // so if we modify it here then it could get modified twice.
            extraTable.getRandomItemsRaw(context,
                    LootTable.createStackSplitter(context.getLevel(), generatedLoot::add));
        });
        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return GTGlobalLootModifiers.ADD_TABLE_LOOT_MODIFIER.get();
    }
}

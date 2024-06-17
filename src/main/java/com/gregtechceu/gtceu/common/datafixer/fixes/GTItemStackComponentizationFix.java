package com.gregtechceu.gtceu.common.datafixer.fixes;

import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.OptionalDynamic;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public class GTItemStackComponentizationFix extends DataFix {

    private static final Set<String> ARMOR_IDS = Set.of(
            "gtceu:nightvision_goggles",
            "gtceu:nanomuscle_chestplate",
            "gtceu:nanomuscle_leggings",
            "gtceu:nanomuscle_boots",
            "gtceu:nanomuscle_helmet",
            "gtceu:face_mask",
            "gtceu:rubber_gloves",
            "gtceu:hazmat_chestpiece",
            "gtceu:hazmat_leggings",
            "gtceu:hazmat_boots",
            "gtceu:hazmat_headpiece",
            "gtceu:quarktech_chestplate",
            "gtceu:quarktech_leggings",
            "gtceu:quarktech_boots",
            "gtceu:quarktech_helmet",
            "gtceu:liquid_fuel_jetpack",
            "gtceu:electric_jetpack",
            "gtceu:advanced_electric_jetpack",
            "gtceu:avanced_nanomuscle_chestplate",
            "gtceu:advanced_quarktech_chestplate");

    public GTItemStackComponentizationFix(Schema outputSchema) {
        super(outputSchema, true);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        return this.writeFixAndRead(
                "ItemStack componentization: GTCEuM edition",
                this.getInputSchema().getType(References.ITEM_STACK),
                this.getOutputSchema().getType(References.ITEM_STACK),
                dynamic -> {
                    Optional<? extends Dynamic<?>> optional = ItemStackData.read(dynamic).map(data -> {
                        fixItemStack(data, data.tag);
                        return data.write();
                    });
                    return DataFixUtils.orElse(optional, dynamic);
                });
    }

    private static void fixItemStack(ItemStackData data, Dynamic<?> tag) {
        // Fix tool info tag
        OptionalDynamic<?> gtTool = data.removeTag("GT.Tool");
        if (gtTool.result().isPresent()) {
            Dynamic<?> dynamic = tag.emptyMap()
                    .set("tool_speed", tag.createFloat(gtTool.get("ToolSpeed").asFloat(0.0f)))
                    .set("attack_damage", tag.createFloat(gtTool.get("AttackDamage").asFloat(0.0f)))
                    .set("enchantability", tag.createInt(gtTool.get("Enchantability").asInt(0)))
                    .set("harvest_level", tag.createInt(gtTool.get("HarvestLevel").asInt(0)))
                    .set("last_crafting_use", tag.createInt(gtTool.get("LastCraftingUse").asInt(0)));
            data.setComponent("gtceu:gt_tool", dynamic);

            data.setComponent("minecraft:max_damage", gtTool.get("MaxDamage"));
            data.setComponent("minecraft:damage", gtTool.get("Damage"));
            OptionalDynamic<?> dynamic1 = gtTool.get("TintColor");
            if (dynamic1.result().isPresent()) {
                Dynamic<?> colorDynamic = tag.emptyMap()
                        .set("rgb", tag.createInt(dynamic1.asInt(0)))
                        .set("showInTooltip", tag.createBoolean(false));
                data.setComponent("minecraft:dyed_color", colorDynamic);
            }
            data.setComponent("gtceu:disallow_container_item", gtTool.get("DisallowContainerItem"));
        }

        // fix tool behaviors tag
        fixGtToolBehaviors(data, tag);

        // Fix power info tags
        if (tag.get("Charge").result().isPresent()) {
            OptionalDynamic<?> charge = data.removeTag("Charge");
            OptionalDynamic<?> maxCharge = data.removeTag("MaxCharge");
            OptionalDynamic<?> infinite = data.removeTag("Infinite");
            OptionalDynamic<?> dischargeMode = data.removeTag("DischargeMode");
            Dynamic<?> dynamic = tag.emptyMap()
                    .set("max_charge", tag.createLong(maxCharge.asLong(-1)))
                    .set("charge", tag.createLong(charge.asLong(0)))
                    .set("infinite", tag.createBoolean(infinite.asBoolean(false)))
                    .set("discharge_mode", tag.createBoolean(dischargeMode.asBoolean(false)));
            data.setComponent("gtceu:energy_content", dynamic);
        }

        // Fix magnet tag
        if (tag.get("IsActive").result().isPresent()) {
            OptionalDynamic<?> isActive = data.removeTag("IsActive");
            data.setComponent("item_magnet", isActive);
        }

        // Fix armors
        if (data.is(ARMOR_IDS)) {
            OptionalDynamic<?> toggleTimer = data.removeTag("toggleTimer");
            OptionalDynamic<?> hover = data.removeTag("hover");
            OptionalDynamic<?> burnTimer = data.removeTag("burnTimer");
            OptionalDynamic<?> canShare = data.removeTag("canShare");
            OptionalDynamic<?> nightVision = data.removeTag("Nightvision");
            OptionalDynamic<?> consumerTicks = data.removeTag("consumerTicks");
            Dynamic<?> dynamic = tag.emptyMap()
                    .set("toggle_timer", tag.createByte(toggleTimer.asByte((byte) 0)))
                    .set("hover", tag.createBoolean(hover.asBoolean(false)))
                    .set("burn_timer", tag.createShort(burnTimer.asShort((short) 0)))
                    .set("can_share", tag.createBoolean(canShare.asBoolean(false)))
                    .set("nightvision", tag.createBoolean(nightVision.asBoolean(false)))
                    .set("consumer_ticks", tag.createByte(consumerTicks.asByte((byte) 0)));
            data.setComponent("gtceu:armor", dynamic);
        }
    }

    private static void fixGtToolBehaviors(ItemStackData data, Dynamic<?> tag) {
        OptionalDynamic<?> gtBehaviorsOpt = data.removeTag("GT.Behaviours");

        Optional<? extends Dynamic<?>> gtBehaviors = gtBehaviorsOpt.result();
        if (gtBehaviors.isPresent()) {
            Dynamic<?> dynamic = tag.emptyMap()
                    .set("max_column", tag.createInt(gtBehaviors.get().remove("MaxAoEColumn").asInt(0)))
                    .set("max_row", tag.createInt(gtBehaviors.get().remove("MaxAoERow").asInt(0)))
                    .set("max_layer", tag.createInt(gtBehaviors.get().remove("MaxAoELayer").asInt(0)))
                    .set("column", tag.createInt(gtBehaviors.get().remove("AoEColumn").asInt(0)))
                    .set("row", tag.createInt(gtBehaviors.get().remove("AoERow").asInt(0)))
                    .set("layer", tag.createInt(gtBehaviors.get().remove("AoELayer").asInt(0)));
            data.setComponent("gtceu:aoe", dynamic);

            Map<String, ? extends Dynamic<?>> map = gtBehaviors.get().asMap(val -> val.asString(""), Function.identity());
            if (!map.isEmpty()) {
                dynamic = tag.emptyMap();

                for (var entry : map.entrySet()) {
                    if (entry.getKey().equals("RelocateMinedBlocks")) {
                        data.setComponent("gtceu:relocate_mined_blocks", createEmpty(tag));
                        continue;
                    } else if (entry.getKey().contains("AoE")) {
                        continue;
                    }
                    dynamic = dynamic.set("gtceu:" + FormattingUtil.toLowerCaseUnderscore(entry.getKey()),
                            createEmpty(tag));
                }

                data.setComponent("gtceu:tool_behaviors", dynamic);
            }
        }
    }

    // I love generics <3
    private static <T> Dynamic<T> createEmpty(Dynamic<T> dynamic) {
        return new Dynamic<>(dynamic.getOps(), dynamic.getOps().empty());
    }

    static class ItemStackData {

        private final String item;
        private Dynamic<?> components;
        private final Dynamic<?> remainder;
        Dynamic<?> tag;

        private ItemStackData(String pItem, Dynamic<?> pNbt) {
            this.item = NamespacedSchema.ensureNamespaced(pItem);
            this.components = pNbt.emptyMap();
            this.tag = pNbt.get("tag").orElseEmptyMap();
            this.remainder = pNbt.remove("tag");
        }

        public static Optional<ItemStackData> read(final Dynamic<?> pTag) {
            return pTag.get("id")
                    .asString()
                    .map(itemId -> new ItemStackData(itemId, pTag))
                    .result();
        }

        public OptionalDynamic<?> removeTag(String pKey) {
            OptionalDynamic<?> optionaldynamic = this.tag.get(pKey);
            this.tag = this.tag.remove(pKey);
            return optionaldynamic;
        }

        public void setComponent(String pComponent, Dynamic<?> pValue) {
            this.components = this.components.set(pComponent, pValue);
        }

        public void setComponent(String pComponent, OptionalDynamic<?> pValue) {
            pValue.result().ifPresent(p_332105_ -> this.components = this.components.set(pComponent, p_332105_));
        }

        public Dynamic<?> moveTagInto(String pOldKey, Dynamic<?> pTag, String pNewKey) {
            Optional<? extends Dynamic<?>> optional = this.removeTag(pOldKey).result();
            return optional.isPresent() ? pTag.set(pNewKey, optional.get()) : pTag;
        }

        public void moveTagToComponent(String pKey, String pComponent, Dynamic<?> pTag) {
            Optional<? extends Dynamic<?>> optional = this.removeTag(pKey).result();
            if (optional.isPresent() && !optional.get().equals(pTag)) {
                this.setComponent(pComponent, optional.get());
            }
        }

        public void moveTagToComponent(String pKey, String pComponent) {
            this.removeTag(pKey).result().ifPresent(p_330514_ -> this.setComponent(pComponent, p_330514_));
        }

        public void fixSubTag(String pKey, boolean pSkipIfEmpty, UnaryOperator<Dynamic<?>> pFixer) {
            OptionalDynamic<?> optionaldynamic = this.tag.get(pKey);
            if (!pSkipIfEmpty || !optionaldynamic.result().isEmpty()) {
                Dynamic<?> dynamic = optionaldynamic.orElseEmptyMap();
                dynamic = pFixer.apply(dynamic);
                if (dynamic.equals(dynamic.emptyMap())) {
                    this.tag = this.tag.remove(pKey);
                } else {
                    this.tag = this.tag.set(pKey, dynamic);
                }
            }
        }

        public Dynamic<?> write() {
            Dynamic<?> dynamic = this.tag;

            if (!this.components.equals(this.tag.emptyMap())) {
                dynamic = dynamic.set("components", this.components);
            }

            return mergeRemainder(dynamic, this.remainder);
        }

        private static <T> Dynamic<T> mergeRemainder(Dynamic<T> tag, Dynamic<?> remainder) {
            DynamicOps<T> ops = tag.getOps();
            return ops.getMap(tag.getValue())
                    .flatMap(mapLike -> ops.mergeToMap(remainder.convert(ops).getValue(), mapLike))
                    .map(object -> new Dynamic<>(ops, object))
                    .result()
                    .orElse(tag);
        }

        public boolean is(String pItem) {
            return this.item.equals(pItem);
        }

        public boolean is(Set<String> pItems) {
            return pItems.contains(this.item);
        }

        public boolean hasComponent(String pComponent) {
            return this.components.get(pComponent).result().isPresent();
        }
    }
}

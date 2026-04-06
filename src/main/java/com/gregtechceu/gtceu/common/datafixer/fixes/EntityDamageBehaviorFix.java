package com.gregtechceu.gtceu.common.datafixer.fixes;

import com.gregtechceu.gtceu.api.datafixer.fixes.ToolBehaviorRemainderFix;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class EntityDamageBehaviorFix extends ToolBehaviorRemainderFix {

    public EntityDamageBehaviorFix(Schema outputSchema) {
        super(outputSchema, "EntityDamageBehaviorFix", "gtceu:tool_behaviors");
    }

    @Override
    protected <T> @NotNull Dynamic<T> fixBehavior(@NotNull Dynamic<T> tag) {
        Optional<Dynamic<T>> bonusList = tag.get("bonus_list").result();
        tag = tag.remove("bonus_list");

        if (bonusList.isPresent()) {
            final Dynamic<T> dynamic = tag;
            var map = bonusList.get().asMap(key -> key.asString(""),
                    value -> value.asFloat(0.0f));
            if (map.isEmpty()) return tag;

            tag = tag.set("bonus_list", tag.createList(
                    map.entrySet().stream()
                            .map(entry -> {
                                Dynamic<?> bonus = dynamic.emptyMap()
                                        .set("bonus", dynamic.createFloat(entry.getValue()));
                                if (entry.getKey().isEmpty()) {
                                    bonus = bonus.set("entities", bonus.emptyList());
                                } else {
                                    bonus = bonus.set("entities", bonus.createString("#" + entry.getKey()));
                                }
                                return bonus;
                            })));
        }
        return tag;
    }
}

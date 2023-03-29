package com.gregtechceu.gtceu.client;

import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.lowdragmc.lowdraglib.client.shader.Shaders;
import com.lowdragmc.lowdraglib.client.shader.management.ShaderManager;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.List;

/**
 * @author KilaBash
 * @date 2023/2/9
 * @implNote ClientCommands
 */
@Environment(EnvType.CLIENT)
public class ClientCommands {

    public static LiteralArgumentBuilder createLiteral(String command) {
        return com.lowdragmc.lowdraglib.client.ClientCommands.createLiteral(command);
    }

    @SuppressWarnings("unchecked")
    public static <S> List<LiteralArgumentBuilder<S>> createClientCommands() {
        return List.of(
                (LiteralArgumentBuilder<S>) createLiteral("gtceu_client").then(createLiteral("save_recipe_type_ui_template")
                        .executes(context -> {
                            for (GTRecipeType recipeType : GTRegistries.RECIPE_TYPES) {
                                recipeType.saveBuiltinUITemplate();
                            }
                            return 1;
                        }))
        );
    }
}

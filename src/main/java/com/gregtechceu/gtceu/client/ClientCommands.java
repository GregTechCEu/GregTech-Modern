package com.gregtechceu.gtceu.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import java.util.List;

/**
 * @author KilaBash
 * @date 2023/2/9
 * @implNote ClientCommands
 */
@OnlyIn(Dist.CLIENT)
public class ClientCommands {

    public static LiteralArgumentBuilder createLiteral(String command) {
        return com.lowdragmc.lowdraglib.client.ClientCommands.createLiteral(command);
    }

    @SuppressWarnings("unchecked")
    public static <S> List<LiteralArgumentBuilder<S>> createClientCommands() {
        return List.of(

        );
    }
}

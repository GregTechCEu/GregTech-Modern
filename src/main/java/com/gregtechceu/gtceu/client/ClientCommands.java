package com.gregtechceu.gtceu.client;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

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

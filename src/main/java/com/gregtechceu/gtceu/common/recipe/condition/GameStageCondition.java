package com.gregtechceu.gtceu.common.recipe.condition;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeConditionType;
import com.gregtechceu.gtceu.common.data.GTRecipeConditions;
import com.gregtechceu.gtceu.common.machine.owner.ArgonautsOwner;
import com.gregtechceu.gtceu.common.machine.owner.IMachineOwner;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import earth.terrarium.argonauts.api.guild.Guild;
import earth.terrarium.argonauts.common.handlers.guild.GuildHandler;
import lombok.NoArgsConstructor;
import net.darkhax.gamestages.GameStageHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;

@NoArgsConstructor
public class GameStageCondition extends RecipeCondition {
    public static final Codec<GameStageCondition> CODEC = RecordCodecBuilder
            .create(instance -> RecipeCondition.isReverse(instance)
                    .and(Codec.STRING.fieldOf("stageName").forGetter(val -> val.stageName))
                    .apply(instance, GameStageCondition::new));

    private String stageName;

    public final static GameStageCondition INSTANCE = new GameStageCondition();

    public GameStageCondition(String stageName, boolean isReverse) {
        super(isReverse);
        if(!GameStageHelper.isStageKnown(stageName)) {
            GTCEu.LOGGER.error("Game Stage id: {} is not known to GameStages!", stageName);
        }
        else {
            this.stageName = stageName;
        }
    }

    public GameStageCondition(String stageName) {
        this(stageName, false);
    }

    public GameStageCondition(boolean isReverse, String stageName) {
        this(stageName, isReverse);
    }

    @Override
    public RecipeConditionType<?> getType() {
        return GTRecipeConditions.GAMESTAGE;
    }

    @Override
    public Component getTooltips() {
        if(isReverse) return Component.translatable("recipe.condition.gamestage.unlocked_stage", stageName);
        return Component.translatable("recipe.condition.gamestage.locked_stage", stageName);
    }

    @Override
    public boolean test(@NotNull GTRecipe recipe, @NotNull RecipeLogic recipeLogic) {
        IMachineOwner owner = recipeLogic.machine.self().getHolder().getOwner();
        if(owner.type() == IMachineOwner.MachineOwnerType.PLAYER) {
            var uuid = owner.getUUID();
            Player player = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(uuid);
            return GameStageHelper.hasStage(player, stageName);
        }
        else if(owner.type() == IMachineOwner.MachineOwnerType.FTB) {
            boolean hasStage = false;
            for(var teamUUID : FTBTeamsAPI.api().getManager().getKnownPlayerTeams().entrySet()) {
                for(var player : teamUUID.getValue().getMembers()) {
                    Player p = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(player);
                    hasStage |= GameStageHelper.hasStage(p, stageName);
                }
            }
            return hasStage;
        }
        else if(owner.type() == IMachineOwner.MachineOwnerType.ARGONAUTS) {
            var argoOwner = (ArgonautsOwner)owner;
            boolean hasStage = false;
            Guild g = GuildHandler.read(argoOwner.getServer()).get(argoOwner.getServer(), argoOwner.getPlayerUUID());
            if(g != null) {
                for(var member : g.members()) {
                    Player p = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(member.profile().getId());
                    hasStage |= GameStageHelper.hasStage(p, stageName);
                }
            }
            return hasStage;
        }
        return false;
    }

    @Override
    public RecipeCondition createTemplate() {
        return new GameStageCondition();
    }
}

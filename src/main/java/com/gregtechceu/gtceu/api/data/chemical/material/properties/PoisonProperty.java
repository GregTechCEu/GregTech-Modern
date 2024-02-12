package com.gregtechceu.gtceu.api.data.chemical.material.properties;

import com.gregtechceu.gtceu.api.data.tag.TagPrefix;

import lombok.Getter;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PoisonProperty implements IMaterialProperty<PoisonProperty>{

    @Getter
    @Nullable
    private final ChemicalDamage damage;

    @Getter
    @Nullable
    private final ChemicalEffect effect;

    @Getter
    private final PoisonType poisonType;

    @Getter
    private final boolean applyToDerivatives;



    public PoisonProperty(PoisonType poisonType, @Nullable ChemicalEffect effect, @Nullable ChemicalDamage damage, boolean applyToDerivatives) {
        this.damage = damage;
        this.poisonType = poisonType;
        this.applyToDerivatives = applyToDerivatives;
        this.effect = effect;
    }
    public PoisonProperty(PoisonType poisonType, ChemicalDamage damage) {this(poisonType,null,damage,true);}


    /**
     * Default property constructor.
     */
    public PoisonProperty(){
        this(PoisonType.CONTACT,null,null,false);
    }

    @Override
    public void verifyProperty(MaterialProperties properties) {
    }

    public enum PoisonType{
        INHALATION(TagPrefix.dust,TagPrefix.dustImpure,TagPrefix.dustSmall,TagPrefix.dustPure,TagPrefix.dustTiny,TagPrefix.rawOre,TagPrefix.rawOreBlock,TagPrefix.crushed,TagPrefix.crushedRefined,TagPrefix.crushedPurified),
        CONTACT;

        private final List<TagPrefix> affectedTagPrefixes = new ArrayList<>();
        PoisonType(TagPrefix... tagPrefixes){
            affectedTagPrefixes.addAll(Arrays.asList(tagPrefixes));
        }

        public boolean isAffected(TagPrefix prefix){
            if(affectedTagPrefixes.isEmpty()) return true; //empty list means all prefixes are affected
            return affectedTagPrefixes.contains(prefix);
        }
    }

    /**
     * @param delay damage is applied every X seconds
     */
    public record ChemicalDamage( int damage, int delay){}
    public record ChemicalEffect( int duration, MobEffect... effects){
        public void apply(LivingEntity entity) {
            for(MobEffect effect: effects)
                entity.addEffect(new MobEffectInstance(effect,duration));

        }

    }

}

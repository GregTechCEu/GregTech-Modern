package com.gregtechceu.gtceu.api.data.chemical.material.properties;

import com.gregtechceu.gtceu.api.data.tag.TagPrefix;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PoisonProperty implements IMaterialProperty<PoisonProperty>{
    @Getter
    private final int damage;
    @Getter
    private final PoisonType poisonType;
    @Getter
    private final boolean applyToDerivatives;

    public PoisonProperty(int damage, PoisonType poisonType, boolean applyToDerivatives) {
        this.damage = damage;
        this.poisonType = poisonType;
        this.applyToDerivatives = applyToDerivatives;
    }

    public PoisonProperty(PoisonType poisonType, boolean applyToDerivatives) {
        this(2,poisonType,applyToDerivatives);
    }

    public PoisonProperty(int damage, PoisonType poisonType) {
        this(damage,poisonType,true);
    }

    public PoisonProperty(PoisonType poisonType) {
        this(2,poisonType,true);
    }
    /**
     * Default property constructor.
     */
    public PoisonProperty(){
        this(0,PoisonType.CONTACT,false);
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
}

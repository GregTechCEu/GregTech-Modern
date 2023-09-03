package com.gregtechceu.gtceu.forge.core.mixins.mekanism;

import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.chemical.BasicChemicalTank;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.attribute.ChemicalAttributeValidator;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.function.BiPredicate;
import java.util.function.Predicate;

@Mixin(BasicChemicalTank.class)
public interface BasicChemicalTankAccessor<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> {

    @Accessor
    public void setListener(IContentsListener listener);
    @Accessor
    public IContentsListener getListener();

    @Accessor
    public Predicate<@NotNull CHEMICAL> getValidator();
    @Accessor
    public BiPredicate<@NotNull CHEMICAL, @NotNull AutomationType> getCanExtract();
    @Accessor
    public BiPredicate<@NotNull CHEMICAL, @NotNull AutomationType> getCanInsert();
    @Accessor
    public long getCapacity();
    @Accessor
    public ChemicalAttributeValidator getAttributeValidator();
}

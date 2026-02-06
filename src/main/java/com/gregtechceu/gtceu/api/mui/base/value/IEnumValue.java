package com.gregtechceu.gtceu.api.mui.base.value;

public interface IEnumValue<T extends Enum<T>> extends IValue<T> {

    Class<T> getEnumClass();
}

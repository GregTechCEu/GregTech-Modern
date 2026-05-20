package com.gregtechceu.gtceu.api.pipenet.property;

import lombok.Getter;
import lombok.Setter;

public class BoolSegmentProperty extends PipeSegmentProperty<Boolean> {

    @Getter
    @Setter
    Boolean value;

    public BoolSegmentProperty(boolean value) {
        this.value = value;
    }

    @Override
    public PipeSegmentProperty<Boolean> copy() {
        return new BoolSegmentProperty(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}

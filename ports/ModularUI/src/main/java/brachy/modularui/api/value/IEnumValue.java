package brachy.modularui.api.value;

public interface IEnumValue<T extends Enum<T>> extends IValue<T> {

    Class<T> getEnumClass();
}

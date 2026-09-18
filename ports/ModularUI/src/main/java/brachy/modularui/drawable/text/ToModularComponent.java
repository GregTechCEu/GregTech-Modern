package brachy.modularui.drawable.text;

public interface ToModularComponent {

    default ModularComponent asModular() {
        throw new UnsupportedOperationException();
    }
}

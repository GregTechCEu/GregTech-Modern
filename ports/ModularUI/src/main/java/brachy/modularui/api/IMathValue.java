package brachy.modularui.api;

/**
 * Math value interface
 * <p>
 * This interface provides only one method which is used by all
 * mathematical related classes. The point of this interface is to
 * provide generalized abstract method for computing/fetching some value
 * from different mathematical classes.
 */
public interface IMathValue {

    /**
     * Get computed or stored value
     */
    IMathValue get();

    boolean isNumber();

    void set(double value);

    void set(String value);

    double doubleValue();

    boolean booleanValue();

    String stringValue();

    class EvaluateException extends RuntimeException {

        public EvaluateException(String message) {
            super(message);
        }
    }
}

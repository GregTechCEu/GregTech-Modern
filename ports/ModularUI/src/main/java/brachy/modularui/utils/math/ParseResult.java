package brachy.modularui.utils.math;

import com.ezylang.evalex.BaseException;
import com.ezylang.evalex.data.EvaluationValue;
import org.jetbrains.annotations.NotNull;

public class ParseResult {

    private final EvaluationValue result;
    private final BaseException error;

    public static ParseResult success(EvaluationValue result) {
        return new ParseResult(result, null);
    }

    public static ParseResult failure(@NotNull BaseException error) {
        return failure(null, error);
    }

    public static ParseResult failure(EvaluationValue value, @NotNull BaseException error) {
        return new ParseResult(value, error);
    }

    private ParseResult(EvaluationValue result, BaseException error) {
        this.result = result;
        this.error = error;
    }

    public boolean isSuccess() {
        return this.error == null;
    }

    public boolean isFailure() {
        return this.error != null;
    }

    public boolean hasValue() {
        return this.result != null;
    }

    public EvaluationValue getResult() {
        return result;
    }

    public BaseException getError() {
        return error;
    }

    public String getErrorMessage() {
        return isFailure() ?
                String.format("%s for Token %s at %d:%d",
                        this.error.getMessage(), this.error.getTokenString(),
                        this.error.getStartPosition(), this.error.getEndPosition()) : null;
    }
}

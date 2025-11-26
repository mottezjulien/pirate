package fr.plop.generic.enumerate;

public enum TrueOrFalse {
    TRUE, FALSE;

    public static TrueOrFalse fromBoolean(Boolean b) {
        if(b) {
            return TrueOrFalse.TRUE;
        }
        return TrueOrFalse.FALSE;
    }

    public TrueOrFalse inverse() {
        return switch (this) {
            case TRUE -> TrueOrFalse.FALSE;
            case FALSE -> TrueOrFalse.TRUE;
        };
    }

    public TrueOrFalse and(TrueOrFalse result) {
        return switch (this) {
            case TRUE -> result;
            case FALSE -> TrueOrFalse.FALSE;
        };
    }

    public TrueOrFalse or(TrueOrFalse result) {
        return switch (this) {
            case TRUE -> TrueOrFalse.TRUE;
            case FALSE -> result;
        };
    }

    public Boolean toBoolean() {
        return switch (this) {
            case TRUE -> true;
            case FALSE -> false;
        };
    }
}

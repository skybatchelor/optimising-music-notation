package uk.ac.cam.optimisingmusicnotation.representation.properties;

/**
 * An enum for different accidental types.
 */
public enum Accidental {
    SHARP,
    FLAT,
    DOUBLE_SHARP,
    DOUBLE_FLAT,
    NATURAL,
    NONE;

    /**
     * Get the number of semitones implied by the Accidental
     * @return a number of semitones
     */
    public int getSemitoneOffset() {
        return switch (this) {
            case SHARP -> 1;
            case FLAT -> -1;
            case DOUBLE_SHARP -> 2;
            case DOUBLE_FLAT -> -2;
            case NATURAL, NONE -> 0;
        };
    }
}

package uk.ac.cam.optimisingmusicnotation.representation.properties;

/**
 * Represents a pitch on the score.
 * @param rootStaveLine the number of the stave or space of the pitch, with the lowest line in a stave as 0, and the first space above that as 1
 * @param semitonesAbove the semitones above the base value of that line
 * @param semitonesAboveC0 the semitones above tC0
 */
public record Pitch (Integer rootStaveLine, Integer semitonesAbove, Integer semitonesAboveC0) {
}

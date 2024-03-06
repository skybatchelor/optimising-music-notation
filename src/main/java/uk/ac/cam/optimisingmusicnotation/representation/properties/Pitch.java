package uk.ac.cam.optimisingmusicnotation.representation.properties;

/**
 * Represents a pitch on the score.
 * @param rootStaveLine
 * @param semitonesAbove
 * @param semitonesAboveC0
 */
public record Pitch (Integer rootStaveLine, Integer semitonesAbove, Integer semitonesAboveC0) {
}

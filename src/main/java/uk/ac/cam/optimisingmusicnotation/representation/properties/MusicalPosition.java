package uk.ac.cam.optimisingmusicnotation.representation.properties;

import uk.ac.cam.optimisingmusicnotation.representation.Line;
import uk.ac.cam.optimisingmusicnotation.representation.Stave;

/**
 * A horizontal musical position, in terms of the time into a score.
 * @param line the line the position is on
 * @param stave the stave the position is on
 * @param crotchetsIntoLine the time into the line
 */
public record MusicalPosition(Line line, Stave stave, float crotchetsIntoLine) implements Comparable<MusicalPosition>{
    @Override
    public int compareTo(MusicalPosition o) {
        return (line.getLineNumber() == o.line.getLineNumber()) ? Float.compare(crotchetsIntoLine, o.crotchetsIntoLine) : Integer.compare(line.getLineNumber(),o.line.getLineNumber());
    }

    public int getIndex() {
        return line.getLineNumber() * line.getStaves().size() + stave.getStaveNumber();
    }

    public MusicalPosition getPositionWithStave(Stave stave) { return new MusicalPosition(line, stave, crotchetsIntoLine); }
}
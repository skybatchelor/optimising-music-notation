package uk.ac.cam.optimisingmusicnotation.representation.properties;

import uk.ac.cam.optimisingmusicnotation.representation.Line;

public record MusicalPosition(Line line, float crotchetsIntoLine) implements Comparable<MusicalPosition>{
    @Override
    public int compareTo(MusicalPosition o) {
        return (line.getLineNumber() == o.line.getLineNumber()) ? Float.compare(crotchetsIntoLine, o.crotchetsIntoLine) : Integer.compare(line.getLineNumber(),o.line.getLineNumber());
    }
}
package uk.ac.cam.optimisingmusicnotation.representation;

import uk.ac.cam.optimisingmusicnotation.rendering.MusicCanvas;
import uk.ac.cam.optimisingmusicnotation.representation.properties.Clef;
import uk.ac.cam.optimisingmusicnotation.representation.properties.KeySignature;
import uk.ac.cam.optimisingmusicnotation.representation.properties.RenderingConfiguration;

import java.util.ArrayList;
import java.util.List;

/**
 * A section of music, which appears as a collection with lines, with a key signature
 */
public class Section {
    private final List<Line> lines;
  
    private final List<Clef> clefs;
    private final KeySignature keySignature;
    public Section() {
        lines = new ArrayList<>();
        clefs = new ArrayList<>();
        keySignature = new KeySignature();

    }
    public Section(Line line, List<Clef> clefs, KeySignature keySignature) {
        lines = new ArrayList<>() {{ add(line); }};
        this.clefs = clefs;
        this.keySignature = keySignature;
    }

    public Section(List<Line> lines, List<Clef> clefs, KeySignature keySignature) {
        this.lines = lines;
        this.clefs = clefs;
        this.keySignature = keySignature;
    }

    public <Anchor> void draw(MusicCanvas<Anchor> canvas) {
        //canvas.addLine(lines.get(0).getOffsetInCrotchets());
        //drawClefAndKey(canvas);
        lines.get(0).drawWithClefAndKeySig(canvas, clefs, keySignature);
        for (Line l: lines.subList(1, lines.size())){
            //canvas.addLine(l.getOffsetInCrotchets());
            l.draw(canvas);
        }
        canvas.reserveHeight(RenderingConfiguration.postSectionHeight);
    }

    public float getMinOffset() {
        return lines.stream().map(Line::getOffsetInCrotchets).min(Float::compareTo).orElse(0f);
    }

    public float getMaxEnd() {
        return lines.stream().map(Line::getEndInCrotchets).max(Float::compareTo).orElse(0f);
    }

    public float getMaxCrotchetsPerLine() {
        return lines.stream().map(Line::getLengthInCrotchets).max(Float::compareTo).orElse(0f);
    }
}

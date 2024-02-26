package uk.ac.cam.optimisingmusicnotation.representation;

import uk.ac.cam.optimisingmusicnotation.rendering.MusicCanvas;
import uk.ac.cam.optimisingmusicnotation.representation.properties.Clef;
import uk.ac.cam.optimisingmusicnotation.representation.properties.ClefSign;
import uk.ac.cam.optimisingmusicnotation.representation.properties.KeySignature;

import java.util.ArrayList;
import java.util.List;

public class Section {
    private final List<Line> lines;
  
    private final Clef clef;
    private final KeySignature keySignature;
    public Section() {
        lines = new ArrayList<>();
        clef = new Clef(ClefSign.G);
        keySignature = new KeySignature();

    }
    public Section(Line line, Clef clef, KeySignature keySignature) {
        lines = new ArrayList<>() {{ add(line); }};
        this.clef = clef;
        this.keySignature = keySignature;
    }

    public Section(List<Line> lines, Clef clef, KeySignature keySignature) {
        this.lines = lines;
        this.clef = clef;
        this.keySignature = keySignature;
    }

    public <Anchor> void draw(MusicCanvas<Anchor> canvas) {
        canvas.addLine();
        drawClefAndKey(canvas);
        lines.get(0).draw(canvas);
        for (Line l: lines.subList(1, lines.size())){
            canvas.addLine();
            l.draw(canvas);
        }
    }

    private <Anchor> void drawClefAndKey(MusicCanvas<Anchor> canvas){
        Line firstLine = lines.get(0);
        for (Stave s: firstLine.getStaves()){
            s.drawPreStaveLines(canvas,firstLine,keySignature.getAlterations().size());
            clef.draw(canvas,firstLine,keySignature.getAlterations().size());
            keySignature.draw(canvas,firstLine,clef);
        }
    }
}

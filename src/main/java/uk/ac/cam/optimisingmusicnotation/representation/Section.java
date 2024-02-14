package uk.ac.cam.optimisingmusicnotation.representation;

import uk.ac.cam.optimisingmusicnotation.rendering.MusicCanvas;
import uk.ac.cam.optimisingmusicnotation.representation.properties.Clef;
import uk.ac.cam.optimisingmusicnotation.representation.properties.ClefSign;
import uk.ac.cam.optimisingmusicnotation.representation.properties.RenderingConfiguration;

import java.util.ArrayList;
import java.util.List;

public class Section {
    private final List<Line> lines;
  
    private final Clef clef;
    public Section() {
        lines = new ArrayList<>();
        clef = new Clef(ClefSign.G);
        lines.add(new Line(0,1));
        lines.add(new Line(1,0));
    }

    public Section(Line line) {
        lines = new ArrayList<>() {{ add(line); }};
    }

    public Section(List<Line> lines) {
        this.lines = lines;
    }

    public <Anchor> void draw(MusicCanvas<Anchor> canvas, RenderingConfiguration config) {
        drawClefKeyAndTimeSignature(canvas);
        for (Line l: lines){
            l.draw(canvas,config);
        }
    }

    private <Anchor> void drawClefKeyAndTimeSignature(MusicCanvas<Anchor> canvas){
        Line firstLine = lines.get(0);
        for (Stave s: firstLine.getStaves()){
            s.drawPreStaveLines(canvas,firstLine);
            clef.draw(canvas,firstLine);
        }
    }
}

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
    public Section(){
        lines = new ArrayList<>();
        clef = new Clef(ClefSign.G);
        lines.add(new Line(0));
    }
    public <Anchor> void draw(MusicCanvas<Anchor> canvas, RenderingConfiguration config) {
        for (Stave s: lines.get(0).getStaves()){
            clef.draw(canvas,lines.get(0));
        }
        for (Line l: lines){
            l.draw(canvas,config);
        }
    }
}

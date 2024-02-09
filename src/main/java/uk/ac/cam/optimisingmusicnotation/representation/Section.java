package uk.ac.cam.optimisingmusicnotation.representation;

import uk.ac.cam.optimisingmusicnotation.rendering.MusicCanvas;
import uk.ac.cam.optimisingmusicnotation.representation.properties.RenderingConfiguration;

import java.util.ArrayList;
import java.util.List;

public class Section {
    private final List<Line> lines;
    public Section(){
        lines = new ArrayList<>();
        lines.add(new Line(0));
        lines.add(new Line(1));
        lines.add(new Line(2));
    }
    public <Anchor> void draw(MusicCanvas<Anchor> canvas, RenderingConfiguration config) {
        for (Line l: lines){
            l.draw(canvas,config);
        }
    }
}

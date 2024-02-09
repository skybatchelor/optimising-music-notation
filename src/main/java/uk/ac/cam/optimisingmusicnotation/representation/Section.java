package uk.ac.cam.optimisingmusicnotation.representation;

import uk.ac.cam.optimisingmusicnotation.rendering.MusicCanvas;
import uk.ac.cam.optimisingmusicnotation.representation.properties.RenderingConfiguration;

import java.util.ArrayList;
import java.util.List;

public class Section {
    private final List<Line> lines;
    public Section() {
        lines = new ArrayList<>();
        lines.add(new Line());
    }

    public Section(Line line) {
        lines = new ArrayList<>() {{ add(line); }};
    }

    public Section(List<Line> lines) {
        this.lines = lines;
    }

    public <Anchor> void draw(MusicCanvas<Anchor> canvas, RenderingConfiguration config) {
        for (Line l: lines){
            l.draw(canvas,config);
        }
    }
}

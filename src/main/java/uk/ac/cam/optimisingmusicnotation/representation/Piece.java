package uk.ac.cam.optimisingmusicnotation.representation;

import uk.ac.cam.optimisingmusicnotation.rendering.MusicCanvas;
import uk.ac.cam.optimisingmusicnotation.representation.properties.RenderingConfiguration;

import java.util.ArrayList;
import java.util.List;

public class Piece {
    private final List<Section> sections;
    public Piece() {
        sections = new ArrayList<>();
        sections.add(new Section());
    }

    public Piece(List<Section> sections) {
        this.sections = sections;
    }

    public <Anchor> void draw(MusicCanvas<Anchor> canvas, RenderingConfiguration config) {
        for (Section s: sections) {
            s.draw(canvas, config);
        }
    }
}

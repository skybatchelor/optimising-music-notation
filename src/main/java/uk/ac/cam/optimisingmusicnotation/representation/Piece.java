package uk.ac.cam.optimisingmusicnotation.representation;

import uk.ac.cam.optimisingmusicnotation.rendering.MusicCanvas;
import uk.ac.cam.optimisingmusicnotation.representation.properties.RenderingConfiguration;

import java.util.List;

public class Piece {
    List<Section> sections;
    //TODO Alphonso converts MusicXML to this
    public <Anchor> void draw(MusicCanvas<Anchor> canvas, RenderingConfiguration config) {
        for (Section s: sections) {
            s.draw(canvas, config);
        }
    }
}

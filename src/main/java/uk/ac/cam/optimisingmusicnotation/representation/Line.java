package uk.ac.cam.optimisingmusicnotation.representation;

import uk.ac.cam.optimisingmusicnotation.rendering.MusicCanvas;
import uk.ac.cam.optimisingmusicnotation.representation.properties.RenderingConfiguration;

import java.util.List;

public class Line {
    Integer lineNumber;
    List<Stave> staves;
    public <Anchor> void draw(MusicCanvas<Anchor> canvas, RenderingConfiguration config) {
        // TODO draw pulse lines
        for (Stave s: staves){
            s.draw(canvas,config);
        }

    }
}

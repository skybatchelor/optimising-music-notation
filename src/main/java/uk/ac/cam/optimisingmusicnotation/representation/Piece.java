package uk.ac.cam.optimisingmusicnotation.representation;

import uk.ac.cam.optimisingmusicnotation.Canvas;
import uk.ac.cam.optimisingmusicnotation.representation.properties.RenderingConfiguration;

import java.util.List;

public class Piece implements Drawable {
    List<Section> sections;
    //TODO Alphonso converts MusicXML to this
    @Override
    public void draw(Canvas canvas, RenderingConfiguration config) {
    }
}

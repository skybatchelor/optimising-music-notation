package uk.ac.cam.optimisingmusicnotation.interfaces;

import java.util.List;

public class Piece implements Drawable {
    List<Section> sections;
    //TODO Alphonso converts MusicXML to this
    @Override
    public void Draw(Canvas canvas, RenderingConfiguration config) {
    }
}

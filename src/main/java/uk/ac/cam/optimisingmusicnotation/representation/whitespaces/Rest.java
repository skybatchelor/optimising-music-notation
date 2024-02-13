package uk.ac.cam.optimisingmusicnotation.representation.whitespaces;

import uk.ac.cam.optimisingmusicnotation.rendering.MusicCanvas;
import uk.ac.cam.optimisingmusicnotation.representation.Line;
import uk.ac.cam.optimisingmusicnotation.representation.properties.MusicalPosition;
import uk.ac.cam.optimisingmusicnotation.representation.properties.RenderingConfiguration;

public class Rest implements Whitespace {

    private final MusicalPosition startMusicalPosition;
    private final MusicalPosition endMusicalPosition;

    public Rest(MusicalPosition startMusicalPosition, MusicalPosition endMusicalPosition) {
        this.startMusicalPosition = startMusicalPosition;
        this.endMusicalPosition = endMusicalPosition;
    }

    @Override
    public <Anchor> void draw(MusicCanvas<Anchor> canvas, Line line, RenderingConfiguration config) {
        canvas.drawWhitespace(canvas.getAnchor(startMusicalPosition), 0, 0.5f,
                canvas.getAnchor(endMusicalPosition), 0, -4.5f);

        for (int i = (int) Math.ceil(startMusicalPosition.crotchetsIntoLine()); i <= endMusicalPosition.crotchetsIntoLine(); i++) {
            MusicalPosition startPosition = new MusicalPosition(line, i);
            Anchor startAnchor = canvas.getAnchor(startPosition);
            canvas.drawLine(startAnchor,0f,2f,0f,-4f,RenderingConfiguration.pulseLineWidth);}
    }
}

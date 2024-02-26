package uk.ac.cam.optimisingmusicnotation.representation.whitespaces;

import uk.ac.cam.optimisingmusicnotation.rendering.MusicCanvas;
import uk.ac.cam.optimisingmusicnotation.representation.Line;
import uk.ac.cam.optimisingmusicnotation.representation.beatlines.PulseLine;
import uk.ac.cam.optimisingmusicnotation.representation.properties.MusicalPosition;

public class Rest implements Whitespace {

    private final MusicalPosition startMusicalPosition;
    private final MusicalPosition endMusicalPosition;

    public Rest(MusicalPosition startMusicalPosition, MusicalPosition endMusicalPosition) {
        this.startMusicalPosition = startMusicalPosition;
        this.endMusicalPosition = endMusicalPosition;
    }

    @Override
    public <Anchor> void draw(MusicCanvas<Anchor> canvas, Line line) {
        //canvas.drawWhitespace(canvas.getAnchor(startMusicalPosition), 0, 0.5f,canvas.getAnchor(endMusicalPosition), 0, -4.5f);
        MusicalPosition pulsePosition;
        for (PulseLine p: line.getPulseLines()) {
            pulsePosition = p.getMusicalPosition();
            if(pulsePosition.compareTo(startMusicalPosition)>=0 && pulsePosition.compareTo(endMusicalPosition)<=0){
                p.drawFull(canvas);
            }
        }
    }

    @Override
    public MusicalPosition getStartMusicalPosition() {
        return startMusicalPosition;
    }

    @Override
    public MusicalPosition getEndMusicalPosition() {
        return endMusicalPosition;
    }
}

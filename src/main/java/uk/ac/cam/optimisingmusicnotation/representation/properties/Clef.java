package uk.ac.cam.optimisingmusicnotation.representation.properties;

import uk.ac.cam.optimisingmusicnotation.rendering.MusicCanvas;
import uk.ac.cam.optimisingmusicnotation.representation.Line;

public class Clef {
    ClefSign sign;
    int line;
    int octaveChange;

    public Clef(ClefSign sign){
        this.sign = sign;
        this.line = sign.defaultLinesFromBottomOfStave;
        this.octaveChange = 0;
    }

    public Clef(ClefSign sign, int line){
        this.sign = sign;
        this.line = line;
        this.octaveChange = 0;
    }

    public Clef(ClefSign sign, int line, int octaveChange){
        this.sign = sign;
        this.line = line;
        this.octaveChange = octaveChange;
    }

    public <Anchor> void draw(MusicCanvas<Anchor> canvas, Line line){
        Anchor anchor = canvas.getAnchor(new MusicalPosition(line, 0));
        String clefPath = "img/clefs/" + this.toString().toLowerCase() + ".svg";
        int topLeftY = this.line - 1 + ((sign.height - sign.lineDistanceFromBottomOfClef)-4);
        try{
            canvas.drawImage(clefPath,anchor,-2f,(float) topLeftY,0f,(float) this.sign.height);
        }catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return sign.toString();
    }
}

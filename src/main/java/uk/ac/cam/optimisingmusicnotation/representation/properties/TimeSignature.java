package uk.ac.cam.optimisingmusicnotation.representation.properties;

import uk.ac.cam.optimisingmusicnotation.rendering.MusicCanvas;
import uk.ac.cam.optimisingmusicnotation.rendering.TextAlignment;

import java.io.IOException;

public class TimeSignature {
    private int beatNum;
    private int beatType;

    public int getBeatNum() {
        return beatNum;
    }
    public int getBeatType() {
        return beatType;
    }

    public TimeSignature() {
        beatNum = 0;
        beatType = 0;
    }

    public TimeSignature(int beatNum, int beatType, MusicalPosition musicalPosition) {
        this.beatNum = beatNum;
        this.beatType = beatType;
    }

    public void setBeatNum(int beatNum) { this.beatNum = beatNum; }

    public void setBeatType(int beatType) { this.beatType = beatType; }

    private int maxDigits(){
        return Math.max(String.valueOf(beatNum).length(), String.valueOf(beatType).length());
    }

    public <Anchor> void draw(MusicCanvas<Anchor> canvas, MusicalPosition musicalPosition) {
        Anchor anchor = canvas.getAnchor(musicalPosition);
        float width = maxDigits() * 1.5f;
        try {
            canvas.drawWhitespace(anchor,-0.5f,7f,width,5f);
            canvas.drawText(RenderingConfiguration.fontFilePath,Integer.toString(beatNum),10f, TextAlignment.LEFT,anchor,-0.5f,8f,width,5f);
            canvas.drawText(RenderingConfiguration.fontFilePath,Integer.toString(beatType),10f, TextAlignment.LEFT,anchor,-0.5f,6f,width,5f);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean equals(TimeSignature timeSig) {
        return (timeSig != null && this.beatNum == timeSig.getBeatNum() && this.beatType == timeSig.getBeatType());
    }
}

package uk.ac.cam.optimisingmusicnotation.representation.properties;

import uk.ac.cam.optimisingmusicnotation.rendering.MusicCanvas;
import uk.ac.cam.optimisingmusicnotation.representation.Line;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Clef {
    public ClefSign getSign() {
        return sign;
    }
    ClefSign sign;

    public int getLine() {
        return line;
    }

    int line;

    public int getOctaveChange() {
        return octaveChange;
    }

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

    public <Anchor> void draw(MusicCanvas<Anchor> canvas, Line line, int numAlterations){
        Anchor anchor = canvas.getAnchor(new MusicalPosition(line, 0));
        String clefPath = "img/clefs/" + this.toString().toLowerCase() + ".svg";
        float topLeftY = (this.line/2f) + ((sign.height - sign.lineDistanceFromBottomOfClef)-4);
        try{
            canvas.drawImage(clefPath, anchor,-(5f+numAlterations*1.5f) ,topLeftY,0f,this.sign.height);
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return sign.toString();
    }

    public List<Pitch> pitchNameToPitches(PitchName name){
        List<Pitch> retList = new ArrayList<>();
        List<PitchName> pitches = Arrays.stream(PitchName.values()).toList();
        int bottomNameI = pitches.indexOf(getBottomLinePitch());
        int nameI = pitches.indexOf(name);
        int spacesAbove = Math.floorMod((nameI - bottomNameI) , pitches.size());
        retList.add(new Pitch(spacesAbove,0));
        if (spacesAbove < 3){
            retList.add(new Pitch(spacesAbove + 7,0));
        }
        return retList;
    }

    private PitchName getBottomLinePitch(){
        int diff = line - sign.defaultLinesFromBottomOfStave;
        List<PitchName> pitches = Arrays.stream(PitchName.values()).toList();
        int numPitches = pitches.size();
        switch (sign){
            case G -> {
                return pitches.get((pitches.indexOf(PitchName.E)+diff) % numPitches);
            }
            case F -> {
                return pitches.get((pitches.indexOf(PitchName.G)+diff) % numPitches);
            }
            case C -> {
                return pitches.get((pitches.indexOf(PitchName.F)+diff) % numPitches);
            }
            default -> {
                return null;
            }
        }
    }
}

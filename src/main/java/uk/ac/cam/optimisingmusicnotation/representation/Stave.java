package uk.ac.cam.optimisingmusicnotation.representation;

import uk.ac.cam.optimisingmusicnotation.rendering.MusicCanvas;
import uk.ac.cam.optimisingmusicnotation.representation.properties.ChordAnchors;
import uk.ac.cam.optimisingmusicnotation.representation.properties.MusicalPosition;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.Chord;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.StaveElement;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.musicgroups.MusicGroup;
import uk.ac.cam.optimisingmusicnotation.representation.whitespaces.Whitespace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Stave {
    private final List<StaveElement> staveElements;
    private final List<Whitespace> whitespaces;
    private final List<MusicGroup> musicGroups;
  
    public Stave() {
        staveElements = new ArrayList<>();
        whitespaces = new ArrayList<>();
        musicGroups = new ArrayList<>();
    }

    public Stave(List<StaveElement> staveElements, List<Whitespace> whitespaces, List<MusicGroup> musicGroups) {
        this.staveElements = staveElements;
        this.whitespaces = whitespaces;
        this.musicGroups = musicGroups;
    }
  
    public void addWhiteSpace(Whitespace whitespace) {
        whitespaces.add(whitespace);
    }
  
    public void addStaveElement(StaveElement staveElement) {
        staveElements.add(staveElement);
    }

    public <Anchor> void draw(MusicCanvas<Anchor> canvas, Line line) {
        Map<Chord, ChordAnchors<Anchor>> chordAnchorsMap = new HashMap<>();
        for (StaveElement s : staveElements) {
            s.draw(canvas, chordAnchorsMap);
        }
        for (MusicGroup m : musicGroups) {
            m.draw(canvas, chordAnchorsMap);
        }
        drawStaveLines(canvas, line);
        for (Whitespace w : whitespaces) {
            w.draw(canvas, line);
        }

    }

    private <Anchor> void drawStaveLines(MusicCanvas<Anchor> canvas, Line line){
        Anchor anchor1 = canvas.getAnchor(new MusicalPosition(line, 0));
        Anchor anchor2 = canvas.getAnchor(new MusicalPosition(line, line.getLengthInCrotchets()));

        for (int i = 0; i < 5; i++) {
            canvas.drawLine(anchor1, -1f, -i, anchor2, 2f, -i, 0.1f);
        }
    }

    public <Anchor> void drawPreStaveLines(MusicCanvas<Anchor> canvas, Line line){
        Anchor anchor1 = canvas.getAnchor(new MusicalPosition(line, 0));
        for (int i = 0; i < 5; i++) {
            canvas.drawLine(anchor1, -8f, -i, -2f, -i, 0.1f);
        }
    }
}

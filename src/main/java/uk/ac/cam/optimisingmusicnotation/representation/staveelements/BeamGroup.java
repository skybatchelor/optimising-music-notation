package uk.ac.cam.optimisingmusicnotation.representation.staveelements;

import uk.ac.cam.optimisingmusicnotation.rendering.MusicCanvas;
import uk.ac.cam.optimisingmusicnotation.representation.properties.RenderingConfiguration;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.musicgroups.MusicGroup;

import java.util.ArrayList;
import java.util.List;

public class BeamGroup extends MusicGroup {
    private static class Beam {
        int startIndex;
        int endIndex;
        // Zero indexed beams
        int number;

        public Beam(int startIndex, int endIndex, int number) {
            this.startIndex = startIndex;
            this.endIndex = endIndex;
            this.number = number;
        }
    }

    private final List<Chord> contents;
    // The list of secondary beams. Note that beam groups always have an implicit first level beam.
    private final List<Beam> beams;

    public BeamGroup() {
        contents = new ArrayList<>();
        beams = new ArrayList<>();
    }

    public BeamGroup(List<Chord> contents) {
        this.contents = contents;
        beams = new ArrayList<>();
    }

    public void addBeam(int startIndex, int endIndex, int number) {
        beams.add(new Beam(startIndex, endIndex, number));
    }

    @Override
    public <Anchor> void draw(MusicCanvas<Anchor> canvas, RenderingConfiguration config) {
        if (contents.size() == 1) {
            Anchor note = contents.get(0).drawRetAnchor(canvas, config);
            canvas.drawLine(note, -1, 3, 0, 3, 0.2f);
        } else {
            List<Anchor> anchors = new ArrayList<Anchor>();
            Anchor start = contents.get(0).drawRetAnchor(canvas, config);
            Anchor end = contents.get(contents.size() - 1).drawRetAnchor(canvas, config);
            anchors.add(start);
            canvas.drawLine(start, 0, 3, end, 0, 3, 0.2f);
            for (Chord chord : contents.subList(1, contents.size() - 1)) {
                anchors.add(chord.drawRetAnchor(canvas, config));
            }
            anchors.add(end);
            for (Beam beam : beams) {
                if (beam.startIndex == beam.endIndex) {
                    canvas.drawLine(anchors.get(beam.startIndex), -1, 3 - 0.5f * beam.number, anchors.get(beam.endIndex), 0, 3 - 0.5f * beam.number, 0.2f);
                } else {
                    canvas.drawLine(anchors.get(beam.startIndex), 0, 3 - 0.5f * beam.number, anchors.get(beam.endIndex), 0, 3 - 0.5f * beam.number, 0.2f);
                }
            }
        }
    }
}

package uk.ac.cam.optimisingmusicnotation.representation.properties;

import java.util.ArrayList;
import java.util.List;

public class KeySignature {

    public void addAlteration(int index, PitchName pitch, Accidental alteration) {
        alterations.add(index, new Alteration(pitch, alteration));
    }

    public void addAlteration(PitchName pitch, Accidental alteration) {
        alterations.add(new Alteration(pitch, alteration));
    }

    public List<Alteration> getAlterations() {
        return alterations;
    }

    private final List<Alteration> alterations;

    public KeySignature () {
        this.alterations = new ArrayList<>();
    }

    // Key signature has a list of alterations for each pitch type
    // They are added in the order they are expected to be drawn in
    // For example, D major would have the following alterations:
    // [(PitchName.F, Accidental.SHARP), (PitchName.C, Accidental.SHARP)]
    // Note that when C major/A minor is parsed, it does add all the relevant naturals so please do draw them
    public KeySignature (List<PitchName> pitches, List<Accidental> accidentals) {
        this.alterations = new ArrayList<>();
        for (int i = 0; i < pitches.size() && i < accidentals.size(); ++i) {
            this.alterations.add(new Alteration(pitches.get(i), accidentals.get(i)));
        }
    }

    public static class Alteration {
        public PitchName getAlteredPitch() {
            return alteredPitch;
        }

        PitchName alteredPitch;

        public Accidental getAccidental() {
            return accidental;
        }

        Accidental accidental;

        public Alteration(PitchName alteredPitch, Accidental accidental) {
            this.alteredPitch = alteredPitch;
            this.accidental = accidental;
        }
    }
}

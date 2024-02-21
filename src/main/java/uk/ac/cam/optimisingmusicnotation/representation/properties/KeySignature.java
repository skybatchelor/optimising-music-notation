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

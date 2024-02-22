package uk.ac.cam.optimisingmusicnotation.representation.properties;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClefTest {

    @Test
    void pitchNameToPitches() {
        Clef clef = new Clef(ClefSign.G);
        Pitch e = clef.pitchNameToPitches(PitchName.E).get(0);
        assertEquals(e.rootStaveLine(),0);
        Pitch f = clef.pitchNameToPitches(PitchName.F).get(0);
        assertEquals(f.rootStaveLine(),1);
        Pitch g = clef.pitchNameToPitches(PitchName.G).get(0);
        assertEquals(g.rootStaveLine(),2);
        Pitch a = clef.pitchNameToPitches(PitchName.A).get(0);
        assertEquals(a.rootStaveLine(),3);
        Pitch b = clef.pitchNameToPitches(PitchName.B).get(0);
        assertEquals(b.rootStaveLine(),4);
        Pitch c = clef.pitchNameToPitches(PitchName.C).get(0);
        assertEquals(c.rootStaveLine(),5);
        Pitch d = clef.pitchNameToPitches(PitchName.D).get(0);
        assertEquals(d.rootStaveLine(),6);
        Pitch e2 = clef.pitchNameToPitches(PitchName.E).get(1);
        assertEquals(e2.rootStaveLine(),7);
        Pitch f2 = clef.pitchNameToPitches(PitchName.F).get(1);
        assertEquals(f2.rootStaveLine(),8);
    }
}
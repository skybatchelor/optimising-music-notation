package uk.ac.cam.optimisingmusicnotation.mxlparser;

import org.audiveris.proxymusic.*;
import uk.ac.cam.optimisingmusicnotation.representation.properties.Accidental;
import uk.ac.cam.optimisingmusicnotation.representation.properties.KeySignature;
import uk.ac.cam.optimisingmusicnotation.representation.properties.Pitch;
import uk.ac.cam.optimisingmusicnotation.representation.properties.PitchName;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.chordmarkings.*;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.chordmarkings.StrongAccent;

import javax.xml.bind.JAXBElement;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

class ChordTuple {

    static int getDotNumber(Note note) {
        if (note.getDot() == null) {
            return 0;
        }
        return note.getDot().size();
    }

    static boolean isTiedFrom(Note note) {
        if (note.getTie() != null) {
            for (var tie : note.getTie()) {
                if(tie.getType() == StartStop.START) {
                    return true;
                }
            }
        }
        return false;
    }

    static boolean isTiedTo(Note note) {
        if (note.getTie() != null) {
            for (var tie : note.getTie()) {
                if(tie.getType() == StartStop.STOP) {
                    return true;
                }
            }
        }
        return false;
    }

    static void addMarkings(Note note, List<ChordMarking> target) {
        if (note.getNotations() != null) {
            for (Notations notations : note.getNotations()) {
                if (notations.getTiedOrSlurOrTuplet() != null) {
                    for (Object object : notations.getTiedOrSlurOrTuplet()) {
                        if (object instanceof Articulations articulations) {
                            for (JAXBElement element : articulations.getAccentOrStrongAccentOrStaccato()) {
                                switch (element.getName().getLocalPart()) {
                                    case "staccato" -> target.add(new Staccato());
                                    case "tenuto" -> target.add(new Tenuto());
                                    case "accent" -> target.add(new Accent());
                                    case "strong-accent" -> target.add(new StrongAccent());
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    List<Note> notes;
    float crotchets;
    float duration;
    int lowestLine;
    KeySignature keySig;
    boolean capital = false;

    public ChordTuple(float crochets, int lowestLine, KeySignature keySig) {
        notes = new ArrayList<>();
        this.crotchets = crochets;
        this.lowestLine = lowestLine;
        this.keySig = keySig;
    }

    InstantiatedChordTuple toInstantiatedChordTuple(float lineTime, TreeMap<Float, TempoChangeTuple> integratedTime) {
        List<Pitch> pitches = new ArrayList<>();
        List<Accidental> accidentals = new ArrayList<>();
        List<ChordMarking> markings = new ArrayList<>();
        List<Boolean> tiesFrom = new ArrayList<>();
        List<Boolean> tiesTo = new ArrayList<>();
        for (Note note : notes) {
            Accidental accidental;
            if (note.getAccidental() != null) {
                accidental = switch (note.getAccidental().getValue()) {
                    case FLAT -> Accidental.FLAT;
                    case SHARP -> Accidental.SHARP;
                    case FLAT_FLAT -> Accidental.DOUBLE_FLAT;
                    case DOUBLE_SHARP -> Accidental.DOUBLE_SHARP;
                    case NATURAL -> Accidental.NATURAL;
                    default -> Accidental.NONE;
                };
            } else {
                accidental = Accidental.NONE;
            }
            if (note.getPitch() != null) {
                Accidental keyAccidental = keySig.getAccidental(PitchName.valueOf(note.getPitch().getStep().name()));
                pitches.add(new Pitch(Parser.pitchToGrandStaveLine(note.getPitch()) - lowestLine,
                        accidental.getSemitoneOffset() - keyAccidental.getSemitoneOffset(), Parser.pitchToGrandStaveLine(note.getPitch())));
            } else if (note.getUnpitched() != null) {
                pitches.add(new Pitch(Parser.pitchToGrandStaveLine(note.getUnpitched()) - lowestLine,
                        0, Parser.pitchToGrandStaveLine(note.getUnpitched())));
            } else {
                pitches.add(new Pitch(0, 0, 0));
            }
            accidentals.add(accidental);
            tiesFrom.add(isTiedFrom(note));
            tiesTo.add(isTiedTo(note));
            addMarkings(note, markings);
        }
        return new InstantiatedChordTuple(pitches, accidentals, tiesFrom, tiesTo, capital,
                Parser.normaliseTime(crotchets, integratedTime) - lineTime, Parser.normaliseDuration(crotchets, duration, integratedTime),
                Parser.convertNoteType(notes.get(0).getType()), getDotNumber(notes.get(0)), markings);
    }
}

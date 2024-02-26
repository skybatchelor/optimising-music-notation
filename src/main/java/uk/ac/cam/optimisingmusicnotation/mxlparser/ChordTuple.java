package uk.ac.cam.optimisingmusicnotation.mxlparser;

import org.audiveris.proxymusic.Articulations;
import org.audiveris.proxymusic.Notations;
import org.audiveris.proxymusic.Note;
import org.audiveris.proxymusic.NoteType;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.chordmarkings.*;

import javax.xml.bind.JAXBElement;
import java.util.ArrayList;
import java.util.List;

class ChordTuple {

    static uk.ac.cam.optimisingmusicnotation.representation.staveelements.NoteType convertNoteType(NoteType noteType) {
        return switch (noteType.getValue()) {
            case "maxima" -> uk.ac.cam.optimisingmusicnotation.representation.staveelements.NoteType.MAXIMA;
            case "long" -> uk.ac.cam.optimisingmusicnotation.representation.staveelements.NoteType.BREVE;
            case "whole" -> uk.ac.cam.optimisingmusicnotation.representation.staveelements.NoteType.SEMIBREVE;
            case "half" -> uk.ac.cam.optimisingmusicnotation.representation.staveelements.NoteType.MINIM;
            case "quarter" -> uk.ac.cam.optimisingmusicnotation.representation.staveelements.NoteType.CROTCHET;
            case "eighth" -> uk.ac.cam.optimisingmusicnotation.representation.staveelements.NoteType.QUAVER;
            case "16th" -> uk.ac.cam.optimisingmusicnotation.representation.staveelements.NoteType.SQUAVER;
            case "32nd" -> uk.ac.cam.optimisingmusicnotation.representation.staveelements.NoteType.DSQUAVER;
            case "64th" -> uk.ac.cam.optimisingmusicnotation.representation.staveelements.NoteType.HDSQUAVER;
            default -> throw new IllegalArgumentException();
        };
    }

    static int getDotNumber(Note note) {
        if (note.getDot() == null) {
            return 0;
        }
        return note.getDot().size();
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

    public ChordTuple(float crochets, int lowestLine) {
        notes = new ArrayList<>();
        this.crotchets = crochets;
        this.lowestLine = lowestLine;
    }

    InstantiatedChordTuple toInstantiatedChordTuple(float lineTime, int lineNum) {
        List<uk.ac.cam.optimisingmusicnotation.representation.properties.Pitch> pitches = new ArrayList<>();
        List<uk.ac.cam.optimisingmusicnotation.representation.properties.Accidental> accidentals = new ArrayList<>();
        List<ChordMarking> markings = new ArrayList<>();
        for (Note note : notes) {
            if (note.getPitch() != null) {
                pitches.add(new uk.ac.cam.optimisingmusicnotation.representation.properties.Pitch(Parser.pitchToGrandStaveLine(note.getPitch()) - lowestLine, 0));
            } else {
                pitches.add(new uk.ac.cam.optimisingmusicnotation.representation.properties.Pitch(0, 0));
            }
            if (note.getAccidental() != null) {
                switch (note.getAccidental().getValue()) {
                    case FLAT -> accidentals.add(uk.ac.cam.optimisingmusicnotation.representation.properties.Accidental.FLAT);
                    case SHARP -> accidentals.add(uk.ac.cam.optimisingmusicnotation.representation.properties.Accidental.SHARP);
                    case FLAT_FLAT -> accidentals.add(uk.ac.cam.optimisingmusicnotation.representation.properties.Accidental.DOUBLE_FLAT);
                    case DOUBLE_SHARP -> accidentals.add(uk.ac.cam.optimisingmusicnotation.representation.properties.Accidental.DOUBLE_SHARP);
                    case NATURAL -> accidentals.add(uk.ac.cam.optimisingmusicnotation.representation.properties.Accidental.NATURAL);
                    default -> accidentals.add(uk.ac.cam.optimisingmusicnotation.representation.properties.Accidental.NONE);
                }
            } else {
                accidentals.add(uk.ac.cam.optimisingmusicnotation.representation.properties.Accidental.NONE);
            }
            addMarkings(note, markings);
        }
        return new InstantiatedChordTuple(pitches, accidentals, crotchets - lineTime, duration, convertNoteType(notes.get(0).getType()), getDotNumber(notes.get(0)), markings);
    }
}

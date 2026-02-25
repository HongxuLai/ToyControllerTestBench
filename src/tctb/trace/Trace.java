package tctb.trace;

import tctb.sim.Event;
import tctb.sim.EventType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Trace {
    private final List<Event> events = new ArrayList<>();

    // record the event, once the event happens, add that into the events list
    public void add(Event e) {
        if (e == null){
            throw new IllegalArgumentException("event cannot be null");
        }
        events.add(e);
    }

    public List<Event> events() {
        return Collections.unmodifiableList(events); // only be readable out of the Trace class
    }

    public int size(){
        return events.size();
    }

    // print each event in one line, use for-each to go through events list
    public String serialize() {
        StringBuilder sb = new StringBuilder();
        for (Event e : events) {
            sb.append(serializeLine(e)).append("\n");
        }
        return sb.toString();
    }

    // write the words in serialize() into file
    public void writeToFile(Path path) throws IOException {
        Files.writeString(path, serialize(), StandardCharsets.UTF_8);
    }

    // change files into list. the reverse of serialize
    public static Trace parse(Path path) throws IOException {
        // read all lines in files and get List<String> lines
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        Trace t = new Trace();
        for (int i = 0; i < lines.size(); i++) {
            // get files' ith line, and remove " " at first and last
            String raw = lines.get(i).trim();
            if (raw.isEmpty()) continue; // allow blank lines
            // change raw's file into event, and add this event into events list
            t.add(parseLine(raw, i + 1));
        }
        return t;
    }

    // a helper for changing an event into a line of file
    private static String serializeLine(Event e) {
        EventType type = e.getType();
        if(type == EventType.TICK) {
            return "TICK";
        }
        if(type == EventType.SET_DESIRED){
            Integer arg = e.getArg();
            if(arg == null){
                throw new IllegalArgumentException("argument cannot be null");
            }
            return "SET_DESIRED " + arg;
        }
        throw new IllegalArgumentException(" Unknown event: " + type);
    }

    // a helper for changing a line of file into an event
    private static Event parseLine(String line, int lineNo) {
        // Allow comments like: "TICK # comment"
        String noComment = line.split("#", 2)[0].trim();
        if (noComment.isEmpty()) {
            // line was comment-only
            return null; // caller should skip; but we already trimmed empty earlier
        }

        String[] parts = noComment.split("\\s+");
        String head = parts[0];

        if("TICK".equals(head)) {
            if(parts.length != 1) {
                throw new IllegalArgumentException("Line " + lineNo + ": TICK takes no arguments");
            }
            return Event.tick();
        }

        if ("SET_DESIRED".equals(head)) {
            if (parts.length != 2) {
                throw new IllegalArgumentException("Line " + lineNo + ": SET_DESIRED requires 1 integer arg");
            }
            int k;
            try {
                k = Integer.parseInt(parts[1]);
            } catch (NumberFormatException nfe) {
                throw new IllegalArgumentException("Line " + lineNo + ": invalid integer '" + parts[1] + "'");
            }
            return Event.setDesired(k);
        }
        throw new IllegalArgumentException("Line " + lineNo + " Unknown event: " + head);
    }
}

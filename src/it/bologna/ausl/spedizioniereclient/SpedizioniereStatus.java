package it.bologna.ausl.spedizioniereclient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/**
 *
 * @author andrea
 */
public class SpedizioniereStatus {
    public enum Status {
   /*    '0':"RECEIVED",
            '1':"SENT",
            '2':"TO_SEND",
            '3':"WAIT_FOR_RECEPIT",
            '4':"ERROR",
            '5':"CONFIRMED",
            '6':"ACCEPTED",
     */  RECEIVED("0"),
         SENT("1"),
         TO_SEND("2"),
         WAIT_FOR_RECEPIT("3"),
         ERROR("4"),
         CONFIRMED("5"),
         ACCEPTED("6"),
         RESENT("7"),
         UNKNOWN("unknown");
       
        private final String abbreviation;
        // Reverse-lookup map for getting a day from an abbreviation
        private static final Map<String, SpedizioniereStatus.Status> lookup = new HashMap();

        static {
            for (SpedizioniereStatus.Status d : SpedizioniereStatus.Status.values()) {
                lookup.put(d.getAbbreviation(), d);
            }
        }

        private Status(String abbreviation) {
            this.abbreviation = abbreviation;
        }

        public String getAbbreviation() {
            return abbreviation;
        }

        public static SpedizioniereStatus.Status get(String abbreviation) {
            SpedizioniereStatus.Status  get = lookup.get(abbreviation);
            if (get != null) {
                return get;
            }
            return Status.UNKNOWN;
        }
    }
    private final Status status;
    private ArrayList<SpedizioniereRecepit> recepits;
    public SpedizioniereStatus(Status status) {
        this.status=status;
    }
    public SpedizioniereStatus(Status status,ArrayList<SpedizioniereRecepit> recepits) {
        this.status=status;
        this.recepits=recepits;
    }

    public static SpedizioniereStatus statusFromJson(String json) {
        JSONObject jo=(JSONObject) JSONValue.parse(json);
        Status s=Status.get((String)jo.get("status"));
        if (s == Status.ERROR){
            ArrayList<SpedizioniereRecepit> recepitsFromJson = SpedizioniereRecepit.recepitsFromJson((JSONArray)jo.get("recepits"));
            return new SpedizioniereStatus(s,recepitsFromJson);
        }
        return new SpedizioniereStatus(s);
    }

    public Status getStatus() {
        return status;
    }
    
    public ArrayList<SpedizioniereRecepit> getErrorRecepits() {
        return recepits;
    }
    
    public SpedizioniereRecepit getErrorRecepit(int index) {
        ArrayList<SpedizioniereRecepit> errorRecepits = getErrorRecepits();
        return errorRecepits.get(index);
    }
    
    @Override
    public String toString() {
        if (recepits == null || recepits.isEmpty()){
        return "Status: " + status.toString();
        }
        return "Status: " + status.toString() + " recepits: " + Arrays.toString(recepits.toArray());
    }
}

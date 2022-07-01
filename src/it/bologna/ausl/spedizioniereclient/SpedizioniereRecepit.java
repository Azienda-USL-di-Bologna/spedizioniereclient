package it.bologna.ausl.spedizioniereclient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/**
 *
 * @author andrea
 */
public class SpedizioniereRecepit {

    private final String uuid;

    public enum TipoRicevuta {

        NON_ACCETTAZIONE("non_accettazione"),
        ACCETTAZIONE("accettazione"),
        ERRORE_CONSEGNA("errore-consegna"),
        PREAVVISO_ERRORE_CONSEGNA("preavviso-errore-consegna"),
        RILEVAZIONE_VIRUS("rilevazione-virus"),
        AVVENUTA_CONSEGNA("avvenuta-consegna"),
        UNKNOWN("unknown");
        private final String abbreviation;
        // Reverse-lookup map for getting a day from an abbreviation
        private static final Map<String, TipoRicevuta> lookup = new HashMap<String, TipoRicevuta>();

        static {
            for (TipoRicevuta d : TipoRicevuta.values()) {
                lookup.put(d.getAbbreviation(), d);
            }
        }

        private TipoRicevuta(String abbreviation) {
            this.abbreviation = abbreviation;
        }

        public String getAbbreviation() {
            return abbreviation;
        }

        public static TipoRicevuta get(String abbreviation) {
            TipoRicevuta get = lookup.get(abbreviation);
            if (get != null) {
                return get;
            }
            return TipoRicevuta.UNKNOWN;
        }
    }
    private final TipoRicevuta tipo;

    public SpedizioniereRecepit(String uuid, TipoRicevuta tipo) {
        this.uuid = uuid;
        this.tipo = tipo;
    }
    
    public TipoRicevuta getTipo() {
        return tipo;
    }
    
    public String getUuid() {
        return uuid;
    }

    public static ArrayList<SpedizioniereRecepit> recepitsFromJson(String json) {
        if (json==null)
            return new ArrayList();
        else {
            JSONArray ja = (JSONArray) JSONValue.parse(json);
            return recepitsFromJson(ja);
        }
    }
    
    public static ArrayList<SpedizioniereRecepit> recepitsFromJson(JSONArray ja) {
        ArrayList<SpedizioniereRecepit> res=new ArrayList();
        if (ja==null) return res;       
        for (Object o : ja) {
            JSONObject jrecepit = (JSONObject) o;
            res.add(new SpedizioniereRecepit((String) jrecepit.get("uuid"), TipoRicevuta.get((String) jrecepit.get("tipo"))));
            
        }
        return res;
    }
     
    @Override
    public String toString() {
        return "uuid: " + uuid + " - tipo: " + tipo;
    }
}

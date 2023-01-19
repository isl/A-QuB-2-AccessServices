package gr.forth.ics.isl.aqub.virtuoso;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TripleString {

    private String triple;

    public TripleString(String s, String p, String o, TripleType type) {
        try {
            switch (type) {
                case LITERAL:
                    triple = "<" + s + "> <" + p + "> \"" + o + "\"";
                    break;
                case LITERALENC:
                    triple = "<" + s + "> <" + p + "> \"" + URLEncoder.encode(o, "UTF-8") + "\"";
                    break;
                default:
                    triple = "<" + s + "> <" + p + "> <" + o + ">";
                    break;
            }
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(TripleString.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public String toString() {
        return triple;
    }

    public String getTripleString() {
        return triple;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 23 * hash + Objects.hashCode(this.triple);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TripleString other = (TripleString) obj;
        if (!Objects.equals(this.triple, other.triple)) {
            return false;
        }
        return true;
    }

}

package types.acl.implementation;

import types.acl.AbstractClientACLObject;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

// TODO: Add documentation
public class ClientACLObject<K extends Serializable> extends AbstractClientACLObject  {

    private Set<String> readers;
    private Set<String> writers;
    private Set<K> indirects;
    private String owner;

    public ClientACLObject(String o, ArrayList<String> r, ArrayList<String> w, ArrayList<K> i) {
        readers = r == null ? new HashSet<String>() : new HashSet<String>(r);
        writers = w == null ? new HashSet<String>() : new HashSet<String>(w);
        indirects = i == null ? new HashSet<K>() : new HashSet<K>(i);
        owner = o;
    }

    public ClientACLObject(String o) {
        this(o, null, null, null);
    }

    public String getOwner() {
        return this.owner;
    }

    public Set<String> getReaders() {
        return this.readers;
    }

    public Set<String> getWriters() {
        return this.writers;
    }

    public Set<K> getIndirects() {
        return this.indirects;
    }
}

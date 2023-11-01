package types.acl.implementation;

import types.acl.AbstractClientACLObject;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

// TODO: Add documentation
public class ClientACLObject<K extends Serializable> extends AbstractClientACLObject  {

    private Set<String> readers;
    private Set<String> writers;
    private Set<K> indirects;
    private String owner;

    public ClientACLObject(String o) {
        readers = new HashSet<String>();
        writers = new HashSet<String>();
        indirects = new HashSet<K>();
        owner = o;
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

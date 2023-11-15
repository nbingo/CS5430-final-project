package types.acl.implementation;

import types.acl.AbstractClientACLObject;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.*;

public class ClientACLObject<K extends Serializable> extends AbstractClientACLObject  {

    private Set<String> readers;
    private Set<String> writers;
    private Set<K> indirects;
    private String owner;


    /**
     * ClientACLObject constructor that takes an owner, readers list, writers list, and indirects list
     * Sets each list to null if none is provided
     */
    public ClientACLObject(String o, Collection<String> r, Collection<String> w, Collection<K> i) {
        readers = r == null ? new HashSet<String>() : new HashSet<String>(r);
        writers = w == null ? new HashSet<String>() : new HashSet<String>(w);
        indirects = i == null ? new HashSet<K>() : new HashSet<K>(i);
        owner = o;
    }


    /**
     * ClientACLObject constructor that takes only an owner string
     * Calls the other constructor with null values for each list
     */
    public ClientACLObject(String o) {
        this(o, null, null, null);
    }


    /**
     * Returns the userId for the owner of the associated key
     * 
     * @return The userId
     */
    public String getOwner() {
        return this.owner;
    }


    /**
     * Returns the set of users who can read the value of the associated key
     * 
     * @return The set of readers for the associated key
     */
    public Set<String> getReaders() {
        return this.readers;
    }


    /**
     * Returns the set of users who can write the value of the associated key
     * 
     * @return The set of writers for the associated key
     */
    public Set<String> getWriters() {
        return this.writers;
    }


    /**
     * Returns the set of keys that serve as indirects for the associated key
     * Any users with permission to read / write the value for a key in the indirects set may read / write the value for the associated key
     * 
     * @return The set of indirects for the associated key
     */
    public Set<K> getIndirects() {
        return this.indirects;
    }

}

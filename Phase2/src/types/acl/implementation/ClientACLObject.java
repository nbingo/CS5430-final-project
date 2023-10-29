package types.acl.implementation;

import types.acl.AbstractClientACLObject;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

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
  // you may add your own fields and methods here
}

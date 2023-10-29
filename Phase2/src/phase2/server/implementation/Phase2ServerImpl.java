package phase2.server.implementation;

import com.sun.security.ntlm.Server;
import phase0.implementation.Phase0Impl;
import phase1.server.implementation.Phase1ServerImpl;
import types.acl.AbstractServerACLObject;
import types.acl.implementation.ServerACLObject;
import types.requests.AbstractAuthenticatedDoRequest;
import types.requests.AbstractAuthenticatedRegisterRequest;
import types.responses.AbstractAuthenticatedDoResponse;
import types.responses.AbstractAuthenticatedRegisterResponse;
import types.responses.DoOperationOutcome;
import types.responses.implementation.AuthenticatedDoResponse;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

public class Phase2ServerImpl<K extends Serializable, V extends Serializable> extends Phase1ServerImpl<K, V, AbstractServerACLObject> {

  public final byte[] signingKey;
  public final byte[] verificationKey;

  public Phase2ServerImpl() throws IOException { 
    super(); 
    this.signingKey = super.getSigningKey();
    this.verificationKey = super.getVerificationKey();
  }

  private Set<String> getAccessSet(K k, String mode) {
    ServerACLObject acl = (ServerACLObject)this.store.readMetaVal(k);
    Set<String> usersWithAccess;
    if (mode == "readers") {
      usersWithAccess = acl.getReaders();
    } else {
      usersWithAccess = acl.getWriters();
    }

    Set<K> keysChecked = new HashSet<>(Arrays.asList(k));

    Set<K> indirects = new HashSet<>(acl.getIndirects());
    ArrayList<K> indirectsList = new ArrayList<>(indirects);
    Queue<K> keysToCheck = new LinkedList<>(indirectsList);

    while (!(keysToCheck.isEmpty())) {
      K keyToCheck = keysToCheck.poll();
      keysChecked.add(keyToCheck);

      ServerACLObject keyAcl = (ServerACLObject)this.store.readMetaVal(keyToCheck);
      if (mode == "readers") {
        usersWithAccess.addAll(keyAcl.getReaders());
      } else {
        usersWithAccess.addAll(keyAcl.getWriters());
      }

      Iterator<K> keyIterator = keyAcl.getIndirects().iterator();

      while (keyIterator.hasNext()) {
        K next = keyIterator.next();
        if (!keysChecked.contains(next)) {
          keysToCheck.add(next);
        }
      }
    }

    return usersWithAccess;
  }

  public AbstractAuthenticatedRegisterResponse authenticatedRegister(AbstractAuthenticatedRegisterRequest req) {
    return super.authenticatedRegister(req);
  }

  public AbstractAuthenticatedDoResponse<K, V, AbstractServerACLObject> authenticatedDo(AbstractAuthenticatedDoRequest<K, V, AbstractServerACLObject> req) {
    String userId = req.userId;
    K key = req.doOperation.key;
    ServerACLObject metaVal = (ServerACLObject)req.doOperation.metaVal;

    DoOperationOutcome.Outcome outcome = DoOperationOutcome.Outcome.AUTHORIZATION_FAILURE;
    AbstractAuthenticatedDoResponse response = null;

    switch (req.doOperation.operation) {
      case CREATE:
        if (super.store.containsKey(key)) {
          ServerACLObject kMetaVal = (ServerACLObject)super.store.readMetaVal(key);

          if (kMetaVal.getOwner() == userId) {
            response = super.authenticatedDo(req);
          }
        } else {
          response = super.authenticatedDo(req);
        }
        break;
      case DELETE:
        //TODO: ask what we should do in a delete of a non-existant key
        if (super.store.containsKey(key)) {
          ServerACLObject kMetaVal = (ServerACLObject)super.store.readMetaVal(key);

          if (kMetaVal.getOwner() == userId) {
            response = super.authenticatedDo(req);
          }
        } else {
          response = super.authenticatedDo(req);
        }
        break;
      case READVAL:
        Set<String> canRead = getAccessSet(key, "readers");

        if (canRead.contains(userId)) {
          response = super.authenticatedDo(req);
        }
        break;
      case READMETAVAL:
        if (super.store.containsKey(key)) {
          ServerACLObject kMetaVal = (ServerACLObject)super.store.readMetaVal(key);

          if (kMetaVal.getOwner() == userId) {
            response = super.authenticatedDo(req);
          }
        } else {
          response = super.authenticatedDo(req);
        }
        break;
      case WRITEVAL:
        Set<String> canWrite = getAccessSet(key, "writers");

        if (canWrite.contains(userId)) {
          response = super.authenticatedDo(req);
        }
        break;
      case WRITEMETAVAL:
        if (super.store.containsKey(key)) {
          ServerACLObject kMetaVal = (ServerACLObject)super.store.readMetaVal(key);

          if (kMetaVal.getOwner() == userId) {
            response = super.authenticatedDo(req);
          }
        } else {
          response = super.authenticatedDo(req);
        }
        break;
    }

    if (response != null) {
      // TODO: HI FILL ME OUT
      // Specifically, create a signed response if one wasn't returned from the switch statement
      // This will invovle extracting the signing function out from Phase1Impl
    }

    // return  AUTHORIZATION_FAILURE if not authorized
    // otherwise call super.authenticatedDo()
    return response;
  }
}

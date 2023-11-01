package phase2.server.implementation;

import phase0.implementation.Phase0Impl;
import phase1.server.implementation.Phase1ServerImpl;
import types.acl.AbstractClientACLObject;
import types.acl.implementation.ClientACLObject;
import types.acl.implementation.ClientACLObject;
import types.requests.AbstractAuthenticatedDoRequest;
import types.requests.AbstractAuthenticatedRegisterRequest;
import types.responses.AbstractAuthenticatedDoResponse;
import types.responses.AbstractAuthenticatedRegisterResponse;
import types.responses.DoOperationOutcome;
import types.responses.implementation.AuthenticatedDoResponse;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

public class Phase2ServerImpl<K extends Serializable, V extends Serializable>
    extends Phase1ServerImpl<K, V, AbstractClientACLObject> {

  public final byte[] signingKey;
  public final byte[] verificationKey;

  public Phase2ServerImpl() throws IOException {
    super();
    this.signingKey = super.getSigningKey();
    this.verificationKey = super.getVerificationKey();
  }

  // TODO: Change to using client ACT object throughout
  private Set<String> getAccessSet(K k, String mode) {
    ClientACLObject acl = (ClientACLObject) this.store.readMetaVal(k);
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

      ClientACLObject keyAcl = (ClientACLObject) this.store.readMetaVal(keyToCheck);
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

  // TODO: Change to using client ACT object throughout
  public AbstractAuthenticatedDoResponse<K, V, AbstractClientACLObject> authenticatedDo(
      AbstractAuthenticatedDoRequest<K, V, AbstractClientACLObject> req) {
    System.out.println("No we actually entered Phase2 authenticatedDo");
    String userId = req.userId;
    K key = req.doOperation.key;
    System.out.println(".... it's the cast isnt it");
    System.out.println(req.doOperation.metaVal);

    ClientACLObject metaVal = (ClientACLObject) req.doOperation.metaVal;
    System.out.println("Entered server authenticatedDo");
    AbstractAuthenticatedDoResponse response = null;

    switch (req.doOperation.operation) {
      case CREATE:
        if (super.store.containsKey(key)) {
          ClientACLObject kMetaVal = (ClientACLObject) super.store.readMetaVal(key);

          if (kMetaVal.getOwner() == userId) {
            response = super.authenticatedDo(req);
            System.out.println("Server overwrote key");
          }
        } else {
          response = super.authenticatedDo(req);
          System.out.println("Server created key");
        }
        System.out.println("Finished server CREATE");
        break;
      case DELETE:
        // TODO: ask what we should do in a delete of a non-existant key
        if (super.store.containsKey(key)) {
          ClientACLObject kMetaVal = (ClientACLObject) super.store.readMetaVal(key);

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
          ClientACLObject kMetaVal = (ClientACLObject) super.store.readMetaVal(key);

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
          ClientACLObject kMetaVal = (ClientACLObject) super.store.readMetaVal(key);

          if (kMetaVal.getOwner() == userId) {
            response = super.authenticatedDo(req);
          }
        } else {
          response = super.authenticatedDo(req);
        }
        break;
    }

    try {
      if (response != null) {
        response = super.getSignedAuthenticatedDoResponse(req, req.doOperation.key, req.doOperation.val,
            req.doOperation.metaVal, DoOperationOutcome.Outcome.AUTHORIZATION_FAILURE);
      }
    } catch (Exception e) {
      return null;
    }

    return response;
  }
}

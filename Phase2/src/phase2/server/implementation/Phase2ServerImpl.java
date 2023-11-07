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


  /**
  * Phase2ServerImpl constructor. Calls the constructor of Phase1ServerImpl. 
  */
  public Phase2ServerImpl() throws IOException {
    super();
    this.signingKey = super.getSigningKey();
    this.verificationKey = super.getVerificationKey();
  }


  /**
   * Finds the list of users permitted to write or read the value for a given key
   * 
   * @param k The key to check permissions for
   * @param mode Whether to check the readers or writers for key k
   * @return The list of ids for users allowed to read or write the value for key k
   */
  private Set<String> getAccessSet(K k, String mode) {
    // Get the metaVal / ACL for key k from the store
    ClientACLObject acl = (ClientACLObject) this.store.readMetaVal(k);

    // Initialize the set of users with access to contain the reader or writer set from the metaVal
    Set<String> usersWithAccess;
    if (mode.equals("readers")) {
      usersWithAccess = acl.getReaders();
    } else {
      usersWithAccess = acl.getWriters();
    }

    // Create an empty set to hold the keys in the indirect set for k whose reader or writer set has been added to the list of users with access 
    Set<K> keysChecked = new HashSet<>(Arrays.asList(k));


    // Create a queue of keys for indirect access; initialized to the indirects set from the metaVal
    Set<K> indirects = new HashSet<>(acl.getIndirects());
    ArrayList<K> indirectsList = new ArrayList<>(indirects);
    Queue<K> keysToCheck = new LinkedList<>(indirectsList);

    // While there are more keys to check, add the reader or writer sets for that key to the list of users with access
    while (!(keysToCheck.isEmpty())) {
      // Dequeue a key and add it to checked keys
      K keyToCheck = keysToCheck.poll();
      keysChecked.add(keyToCheck);

      // Get metaVal for the dequeued key
      ClientACLObject keyAcl = (ClientACLObject) this.store.readMetaVal(keyToCheck);

      // Add the relevant readers or writers set for the dequeued key to the list of users with access
      if (mode.equals("readers")) {
        usersWithAccess.addAll(keyAcl.getReaders());
      } else {
        usersWithAccess.addAll(keyAcl.getWriters());
      }

      // Loop through the keys in the indirect set of the dequeued key and add those to the keys to check for users
      Iterator<K> keyIterator = keyAcl.getIndirects().iterator();

      while (keyIterator.hasNext()) {
        K next = keyIterator.next();
        if (!keysChecked.contains(next)) {
          keysToCheck.add(next);
        }
      }
    }

    // Return the list of users with read or write access to key k's value
    return usersWithAccess;
  }


  /**
   * Returns the result of passing the registration request to the Phase 1 server
   * 
   * @param req The registration request from the stub
   * @return The AbstractAuthenticatedRegisterResponse containing the result of passing the registration request to the Phase 1 server
   */
  public AbstractAuthenticatedRegisterResponse authenticatedRegister(AbstractAuthenticatedRegisterRequest req) {
    return super.authenticatedRegister(req);
  }


  /**
   * Checks whether the user making a request to create or delete a key, read or write a meta-value, or read or write a value is authorized to do so.
   * If so, it passes the request on to the Phase 1 server. If not, it returns an authorization failure.
   * 
   * @param req The request from the user containing the action to perform and the associated <K,V,M> triple
   * @return The AbstractAuthenticatedDoResponse for the given request
   */
  public AbstractAuthenticatedDoResponse<K, V, AbstractClientACLObject> authenticatedDo(AbstractAuthenticatedDoRequest<K, V, AbstractClientACLObject> req) {
    // The userId, key, and meta-value for the given request
    String userId = super.extractId(req.userId);
    K key = req.doOperation.key;
    ClientACLObject metaVal = (ClientACLObject) req.doOperation.metaVal;

    // Create a null object to store the response
    AbstractAuthenticatedDoResponse response = null;

    // Handle each type of request
    switch (req.doOperation.operation) {
      case CREATE:
        // Allow the user to create a key if it does not exist in the store or if they are the owner of the previous version of the key
          // Passes the request to the Phase 1 server if the action is authorized
        if (super.store.containsKey(key)) {
          ClientACLObject kMetaVal = (ClientACLObject) super.store.readMetaVal(key);

          if (kMetaVal.getOwner().equals(userId)) {
            response = super.authenticatedDo(req);
          }
        } else if (metaVal.getOwner().equals(userId)){ // Prevents users from creating a key they don't own
          response = super.authenticatedDo(req);
        }
        break;
      case DELETE:
        // Allow the user to delete a key if they own the key or if the key does not exist
          // Passes the request to the Phase 1 server if the action is authorized
        if (super.store.containsKey(key)) {
          ClientACLObject kMetaVal = (ClientACLObject) super.store.readMetaVal(key);

          if (kMetaVal.getOwner().equals(userId)) {
            response = super.authenticatedDo(req);
          }
        } else {
          response = super.authenticatedDo(req);
        }
        break;
      case READVAL:
        // Allow a user to read the key's value if they are given read access to the key through the readers or indirects set
          // Passes the request to the Phase 1 server if the action is authorized
        Set<String> canRead = getAccessSet(key, "readers");

        if (canRead.contains(userId)) {
          response = super.authenticatedDo(req);
        }
        break;
      case READMETAVAL:
        // Allow a user to read the key's meta-value if they are the key's owner
          // Passes the request to the Phase 1 server if the action is authorized
        if (super.store.containsKey(key)) {
          ClientACLObject kMetaVal = (ClientACLObject) super.store.readMetaVal(key);

          if (kMetaVal.getOwner().equals(userId)) {
            response = super.authenticatedDo(req);
          }
        } else {
          response = super.authenticatedDo(req);
        }
        break;
      case WRITEVAL:
        // Allow a user to write the key's value if they are given read access to the key through the readers or indirects set
          // Passes the request to the Phase 1 server if the action is authorized
        Set<String> canWrite = getAccessSet(key, "writers");

        if (canWrite.contains(userId)) {
          response = super.authenticatedDo(req);
        }
        break;
      case WRITEMETAVAL:
        // Allow a user to write the key's meta-value if they are the key's owner
          // Passes the request to the Phase 1 server if the action is authorized
        if (super.store.containsKey(key)) {
          ClientACLObject kMetaVal = (ClientACLObject) super.store.readMetaVal(key);

          if (kMetaVal.getOwner().equals(userId)) {
            response = super.authenticatedDo(req);
          }
        } else {
          response = super.authenticatedDo(req);
        }
        break;
    }

    // If an authorization failure occured sometime during the switch above, create a signed AuthenticatedDoResponse noting the authorization failure
    try {
      if (response == null) {
        response = super.getSignedAuthenticatedDoResponse(req, req.doOperation.key, req.doOperation.val,
            req.doOperation.metaVal, DoOperationOutcome.Outcome.AUTHORIZATION_FAILURE);
      }
    } catch (Exception e) {
      return null;
    }

    // Return the AbstractAuthenticatedDoResponse corresponding to the given request
    return response;
  }
}

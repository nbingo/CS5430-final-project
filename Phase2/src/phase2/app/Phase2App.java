package phase2.app;

import network.NetworkBase;
import network.implementation.NetworkImpl;
import phase1.server.Phase1ServerBase;
import phase2.server.implementation.Phase2ServerImpl;
import phase1.stub.Phase1Stub;
import phase1.stub.Phase1StubBase;
import phase2.stub.implementation.Phase2StubImpl;
import types.acl.AbstractServerACLObject;
import types.acl.AbstractClientACLObject;
import types.acl.implementation.ClientACLObject;

import java.io.IOException;
import java.io.Serializable;
import java.lang.AssertionError;
import java.lang.ClassCastException;
import java.rmi.RemoteException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.NoSuchElementException;

public class Phase2App {

  // DO NOT CHANGE THIS CODE
  public static void registerPhase2() {
    try {
      Phase1ServerBase<String, Serializable, AbstractClientACLObject> phase2 = new Phase2ServerImpl<String, Serializable>();
      phase2.registerServer();
      System.out.println("Phase2ServerImpl bound");
    } catch (IOException /*| RemoteException*/ e) {
      System.err.println("Phase2ServerImpl exception:");
      e.printStackTrace();
      System.exit(1);
    } 
  }

  // DO NOT CHANGE THIS CODE
  public static void registerNetwork() {
    try {
      NetworkBase<String, Serializable, AbstractServerACLObject> network = new NetworkImpl<String, Serializable, AbstractServerACLObject>();
      network.registerNetwork();
      System.out.println("NetworkImpl bound");
    } catch (ClassCastException | NotBoundException | RemoteException e) {
      System.err.println("NetworkImpl exception:");
      e.printStackTrace();
      System.exit(2);
    }
  }

  // DO NOT CHANGE THIS CODE
  public static void registerStub() {
    try {
      Phase1StubBase<String, Serializable, AbstractClientACLObject> stub = new Phase2StubImpl<String, Serializable>();
      stub.registerStub();
      System.out.println("Phase2StubImpl bound");
    } catch (ClassCastException | IOException | NotBoundException /*| RemoteException*/ e) {
      System.err.println("Phase2StubImpl exception:");
      e.printStackTrace();
      System.exit(3);
    }
  }

  // DO NOT CHANGE THIS CODE
  // Note that for Java type systems we are using types
  // that refer to Phase1, but be assured that at runtime
  // the stub object you will be working with will be
  // an instance of Phase2Stub
  public static Phase1Stub<String, Serializable, AbstractClientACLObject> connectToStub() {
    try {
      String name = "Stub";
      Registry registry = LocateRegistry.getRegistry();
      Phase1Stub<String, Serializable, AbstractClientACLObject> stub = (Phase1Stub<String, Serializable, AbstractClientACLObject>) registry.lookup(name);
      return stub;
    } catch (ClassCastException | NotBoundException | RemoteException e) {
      System.err.println("Couldn't connect to Phase2StubImpl:");
      e.printStackTrace();
      System.exit(4);
    }
    return null;
  }

  public static void main(String[] args) {
    registerPhase2();
    registerNetwork();
    registerStub();

    // this naming due to the type is a bit of a misnomer
    // at runtime you will have an instance of Phase2StubImpl
    // but the static type is Phase1Stub
    Phase1Stub<String, Serializable, AbstractClientACLObject> stub = connectToStub();
    String userId = "fbs";
    
    // Get this test to pass first
    sampleTestRegister(stub, userId);

    System.out.println("=============Custom tests=============");
    try {
      // TESTS THAT SHOULD WORK
      System.out.println("=====Tests that should work=====");
      // Test registration
      sampleTestRegister(stub, "user1");
      // Test create
      System.out.println(stub.create("user1", "k1.1", "v1.1", new ClientACLObject("user1", new ArrayList<String>(Arrays.asList("user1")), new ArrayList<String>(Arrays.asList("user1")), null))); // Create key with owner in readers and writers
      System.out.println(stub.create("user1", "k1.2", "v1.2", new ClientACLObject("user1", null, null, new ArrayList<String>(Arrays.asList("k1.1"))))); // Create key with owner gaining access through indirects
      System.out.println(stub.create("user1", "k1.1", "v1.3", new ClientACLObject("user1", new ArrayList<String>(Arrays.asList("user1")), new ArrayList<String>(Arrays.asList("user1")), null))); // Create key that exists already as owner
      System.out.println(stub.create("fbs", "k2.1", "v2.1", new ClientACLObject("fbs", new ArrayList<String>(Arrays.asList("fbs", "user1")), new ArrayList<String>(Arrays.asList("fbs", "user1")), null))); // Create key with two users (including owner) in readers and writers
      System.out.println(stub.create("fbs", "k2.2", "v2.2", new ClientACLObject("fbs", null, null, new ArrayList<String>(Arrays.asList("k1.1"))))); // Create key with another user gaining access through indirects
      // Test read functions
      System.out.println(stub.readVal("user1", "k1.1")); // Read val direct access (owner)
      System.out.println(stub.readVal("user1", "k1.2")); // Read val with indirect access (owner)
      System.out.println(stub.readVal("user1", "k2.1")); // Read val with direct access (not owner)
      System.out.println(stub.readVal("user1", "k2.2")); // Read val with indirect access (not owner)
      System.out.println(stub.readMetaVal("user1", "k1.1")); // Read metaVal as owner
      // Test write functions and read resulting functions to ensure changes sucessful
      System.out.println(stub.writeVal("user1", "k1.1", "v1.1.2")); // Write val direct access (owner)
      System.out.println(stub.writeVal("user1", "k1.2", "v1.2.2")); // Write val with indirect access (owner)
      System.out.println(stub.writeVal("user1", "k2.1", "v2.1.2")); // Write val with direct access (not owner)
      System.out.println(stub.writeVal("user1", "k2.2", "v2.2.2")); // Write val with indirect access (not owner)
      System.out.println(stub.writeMetaVal("fbs", "k2.1", new ClientACLObject("fbs"))); // Write metaVal as owner
      // Test delete functions
      System.out.println(stub.delete("user1", "k1.1")); // Delete key user owns
      System.out.println(stub.delete("user1", "k1.1")); // Delete key that doesn't exist

      // TESTS THAT SHOULDN'T WORK
      System.out.println("=====Tests that should NOT work=====");
      // Ensure register function fails correctly
      System.out.println(stub.registerUser("user1"));
      // Ensure create functions fail correctly
      System.out.println(stub.create("user1", "k1.3", "v1.3", new ClientACLObject("fbs"))); // Cannot create key with another user as owner
      System.out.println(stub.create("fbs", "k1.2", "v2.2", new ClientACLObject("fbs"))); // Cannot create key that exists already if not owner
      System.out.println(stub.create("user.bla", "k.bla", "v.bla", new ClientACLObject("user.bla"))); // Nonexistant user cannot create key
      // Ensure read functions fail correctly
      System.out.println(stub.readVal("fbs", "k2.1")); // Cannot read val without direct or indirect access (owner)
      System.out.println(stub.readVal("fbs", "k1.1")); // Cannot read val without direct or indirect access (not owner)
      System.out.println(stub.readMetaVal("fbs", "k1.1")); // Cannot read metaVal if not owner
      System.out.println(stub.readVal("fbs", "k1.bla")); // Cannot read val for key that doesn't exist
      System.out.println(stub.readMetaVal("fbs", "k1.bla")); // Cannot read metaVal for key that doesn't exist
      System.out.println(stub.readVal("user.bla", "k1.1")); // Nonexistant user cannot read val
      System.out.println(stub.readMetaVal("user.bla", "k1.1")); // Nonexistant user cannot read metaVal
      // Ensure write functions fail correctly
      System.out.println(stub.writeVal("fbs", "k2.1", "v.bla")); // Cannot write val without direct or indirect access (owner)
      System.out.println(stub.writeVal("fbs", "k1.1", "v.bla")); // Cannot write val without direct or indirect access (not owner)
      System.out.println(stub.writeMetaVal("fbs", "k1.1", new ClientACLObject("fbs"))); // Cannot write metaVal if not owner
      System.out.println(stub.writeVal("fbs", "k1.bla", "v.bla")); // Cannot write val for key that doesn't exist
      System.out.println(stub.writeMetaVal("fbs", "k1.bla", new ClientACLObject("fbs"))); // Cannot write metaVal for key that doesn't exist
      System.out.println(stub.writeVal("user.bla", "k1.1", "v.bla")); // Nonexistant user cannot write val
      System.out.println(stub.writeMetaVal("user.bla", "k1.1", new ClientACLObject("user.bla"))); // Nonexistant user cannot write metaVal
      // Ensure delete functions fail correctly
      System.out.println(stub.delete("user1", "k2.1")); // Cannot delete key you don't own

    } catch (Exception e) {
      System.out.println(e);
    }

    System.exit(0);
  }

  public static void sampleTestRegister(Phase1Stub<String, Serializable, AbstractClientACLObject> stub, String userId) {
    // we should be able to register a userId if everything checks out
    // try to get this test to pass first before working on your own tests
    try {
      assert(stub.registerUser(userId));
    } catch (RemoteException e) {
      System.out.println("Test sampleTestRegister failed due to RemoteException.");
    } catch (AssertionError e) {
      System.out.println("Test sampleTestRegister failed.");
      System.exit(5);
    }
  }
}

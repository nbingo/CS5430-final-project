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
      // Tests that should work
      System.out.println("=====Tests that should work=====");
      // Test registration
      sampleTestRegister(stub, "user1");
      // Test create
      System.out.println(stub.create("user1", "k1.1", "v1.1", new ClientACLObject("user1")));
//      System.out.println(stub.create("user1", "k1.2", "v1.2", "m1.2"));
//      System.out.println(stub.create("fbs", "k1.2", "v2.2", "m2.2")); // Should just overwrite the previous value and meta-value of k1.2
//      // Test read functions
//      System.out.println(stub.readVal("user1", "k1.1"));
//      System.out.println(stub.readMetaVal("user1", "k1.1"));
//      System.out.println(stub.readVal("fbs", "k1.1"));
//      System.out.println(stub.readMetaVal("fbs", "k1.1"));
//      // Test write functions and read resulting functions to ensure changes sucessful
//      System.out.println(stub.writeMetaVal("user1", "k1.1", "v1.1-mod"));
//      System.out.println(stub.readMetaVal("user1", "k1.1"));
//      System.out.println(stub.writeVal("fbs", "k1.1", "v2.1"));
//      System.out.println(stub.readVal("user1", "k1.1"));
//      // Test delete functions
//      System.out.println(stub.delete("user1", "k1.1"));
//      System.out.println(stub.delete("user1", "k1.1")); // Should work because the key already doesn't exist
//
//      // Tests that should not work
//      System.out.println("=====Tests that should NOT work=====");
//      // Ensure register function fails correctly
//      System.out.println(stub.registerUser("user1"));
//      // Ensure create and delete functions fail correctly
//      System.out.println(stub.create("user.bla", "k.bla", "v.bla", "m.bla"));
//      System.out.println(stub.delete("user.bla", "k1.2"));
//      // Ensure read functions fail correctly
//      System.out.println(stub.readMetaVal("user1", "m.bla"));
//      System.out.println(stub.readMetaVal("user.bla", "k1.2"));
//      System.out.println(stub.readVal("user1", "v.bla"));
//      System.out.println(stub.readVal("user.bla", "k1.2"));
//      // Ensure write functions fail correctly
//      System.out.println(stub.writeVal("user1", "k.bla", "v.bla"));
//      System.out.println(stub.writeVal("user.bla", "k1.1", "v.bla"));
//      System.out.println(stub.writeMetaVal("user1", "k.bla", "m.bla"));
//      System.out.println(stub.writeMetaVal("user.bla", "k1.1", "m.bla"));
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

package phase1.app;

import network.NetworkBase;
import network.implementation.NetworkImpl;
import phase1.server.Phase1ServerBase;
import phase1.server.implementation.Phase1ServerImpl;
import phase1.stub.Phase1Stub;
import phase1.stub.Phase1StubBase;
import phase1.stub.implementation.Phase1StubImpl;

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

public class Phase1App {

  // DO NOT CHANGE THIS CODE
  public static void registerPhase1() {
    try {
      Phase1ServerBase<String, Serializable, Serializable> phase1 = new Phase1ServerImpl<String, Serializable, Serializable>();
      phase1.registerPhase1();
      System.out.println("Phase1ServerImpl bound");
    } catch (IOException /*| RemoteException*/ e) {
      System.err.println("Phase1ServerImpl exception:");
      e.printStackTrace();
      System.exit(1);
    } 
  }

  // DO NOT CHANGE THIS CODE
  public static void registerNetwork() {
    try {
      NetworkBase<String, Serializable, Serializable> network = new NetworkImpl<String, Serializable, Serializable>();
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
      Phase1StubBase<String, Serializable, Serializable> stub = new Phase1StubImpl<String, Serializable, Serializable>();
      stub.registerStub();
      System.out.println("Phase1StubImpl bound");
    } catch (ClassCastException | IOException | NotBoundException /*| RemoteException*/ e) {
      System.err.println("Phase1StubImpl exception:");
      e.printStackTrace();
      System.exit(3);
    }
  }

  // DO NOT CHANGE THIS CODE
  public static Phase1Stub<String, Serializable, Serializable> connectToStub() {
    try {
      String name = "Phase1Stub";
      Registry registry = LocateRegistry.getRegistry();
      Phase1Stub<String, Serializable, Serializable> stub = (Phase1Stub<String, Serializable, Serializable>) registry.lookup(name);
      return stub;
    } catch (ClassCastException | NotBoundException | RemoteException e) {
      System.err.println("Couldn't connect to Phase1StubImpl:");
      e.printStackTrace();
      System.exit(4);
    }
    return null;
  }

  public static void main(String[] args) {
    registerPhase1();
    registerNetwork();
    registerStub();

    Phase1Stub<String, Serializable, Serializable> stub = connectToStub();
    String userId = "fbs";
    
    // Get this test to pass first
    sampleTestRegister(stub, userId);

    System.out.println("Custom tests:");
    sampleTestRegister(stub, "Rebecca");
    try {
      stub.create("Rebecca", "banana", "cat", "dog");
      System.out.println(stub.readVal("Rebecca", "banana"));
      System.out.println(stub.readVal("fbs", "banana"));
      System.out.println(stub.writeMetaVal("Rebecca", "banana", "cheese"));
      System.out.println(stub.readMetaVal("Rebecca", "banana"));
      System.out.println(stub.writeVal("Rebecca", "banana", "cheese"));
      System.out.println(stub.readVal("Rebecca", "banana"));
      stub.create("Rebecca", "fruit", "cat", "dog");
      stub.create("Rebecca", "banana", "cat", "dog"); //TODO: Can we add the same key twice?
    } catch (Exception e) {
      System.out.println(e);
    }

    System.exit(0);
  }

  public static void sampleTestRegister(Phase1Stub<String, Serializable, Serializable> stub, String userId) {
    // we should be able to register a userId if everything checks out
    // try to get this test to pass first before working on your own tests
    try {
      assert(stub.registerUser(userId));
    } catch (RemoteException e) {
      System.out.println("Test sampleTestRegister failed due to RemoteException.");
    } catch (AssertionError e) {
      System.out.println("Test sampleTestRegister failed registering " + userId + ". With message: " + e);
      System.exit(5);
    }
  }
}

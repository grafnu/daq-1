package switchtest;

public class Main {

  public static void main(String[] args) throws Exception {

    // if (args.length != 1) {
    //    throw new IllegalArgumentException("Expected ipAddress && port as argument");
    //

    // String ipAddress = args[0];
    String ipAddress = "192.168.1.2";

    // int interfacePort = Integer.parseInt(args[1]);
    int interfacePort = 23;

    SwitchInterrogator switchInterrogator = new SwitchInterrogator(ipAddress, interfacePort);

    Thread switchInterrogatorThread = new Thread(switchInterrogator);
    switchInterrogatorThread.start();
  }
}
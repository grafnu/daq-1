package switchtest;

public class Main {

  public static void main(String[] args) throws Exception {

    if (args.length != 2) {
      throw new IllegalArgumentException("Expected ipAddress && port as arguments");
    }

    String ipAddress = args[0];

    int interfacePort = Integer.parseInt(args[1]);

    SwitchInterrogator switchInterrogator = new SwitchInterrogator(ipAddress, interfacePort);

    Thread switchInterrogatorThread = new Thread(switchInterrogator);
    switchInterrogatorThread.start();
  }
}
package switchtest;

public class Main {

  private static Thread switchInterrogatorThread;
	
  public static void main(String[] args) {

    String ipAddress = "192.168.1.2";
    int interfacePort = 10;

    try {
      if (args.length > 0) {
        if (args[0].length() > 0) {
          ipAddress = args[0];
        }

        if (args[1].length() > 0) {
          interfacePort = Integer.parseInt(args[1]);
        }
      }
    } catch (Exception e) {
      System.err.println("Exception main parse args:" + e.getMessage());
    }

    SwitchInterrogator switchInterrogator = new SwitchInterrogator(ipAddress, interfacePort);

    Thread switchInterrogatorThread = new Thread(switchInterrogator);
    switchInterrogatorThread.start();
  }
}
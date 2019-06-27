public class Main {

  public static void main(String[] args) {

    // telnet OR ssh AS A PROTOCOL

     if (args.length != 4) {
      throw new IllegalArgumentException(
      "Usage: target_ip protocol(ssh/telnet) target_port target_mac");
    }

	String host = args[0];

    String protocol = args[1];
    int connectionPort = Integer.parseInt(args[2]);
    String macAddress = args[3];

//      String host = "127.0.0.1";
//     String protocol = "telnet";
//     int connectionPort = 32768;
//   String macAddress = "02:42:AC:a3:r4:eo";

    System.out.println("Main Started...");
    SetupTest setupTest = new SetupTest(protocol, host, connectionPort, macAddress);
  }
}

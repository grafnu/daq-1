public class Main {

  public static void main(String[] args) throws Exception {

    if (args.length != 1) {
      throw new IllegalArgumentException("Expected ipAddress && port as argument");
    }

    String ipAddress = args[0];

    Certs certificate = new Certs("https://" + ipAddress);

    try {
      certificate.get_certificate();
      System.out.println("Certificate has been read successfully");
    } catch (Exception e) {
      System.err.println("Exception main certificate:" + e.getMessage());
    }
  }
}


public class Main {

  public static void main(String[] args) {
    String ipAddress = "127.0.0.1";

    try {
      if (args != null) {
        if (args[0] != null) {
          ipAddress = args[0];
        }
      }
    } catch (Exception e) {
      System.err.println("Exception main parse args:" + e.getMessage());
    }

    Certs certificate = new Certs("https://" + ipAddress);

    try {
      certificate.get_certificate();
      System.out.println("Certificate has been read successfully");
    } catch (Exception e) {
      System.err.println("Exception main certificate:" + e.getMessage());
    }
  }
}
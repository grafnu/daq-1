
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import javax.net.ssl.*;

public class Certs {

  public static String ipAddress;
  public static int port;

  public Certs(String ipAddress, int port) {
    this.ipAddress = ipAddress;
    this.port = port;
  }

  String certificateReport = "";
  Report report = new Report();

  public boolean testTLSVersions() throws Exception {
    try {
      boolean tlsV12 = testTLS12();
      boolean tlsV13 = testTLS13();
      boolean certValid = tlsV12 || tlsV13;
      passX509(certValid);
      return certValid;
    } finally {
      report.writeReport(certificateReport);
    }
  }

  private boolean testTLS12() throws Exception {
    return testTLS("1.2");
  }

  private boolean testTLS13() throws Exception {
    return testTLS("1.3");
  }

  private boolean testTLS(String tlsVersion) throws Exception {
    SSLSocket socket;
    try {
      // Attempt to open an SSL Socket at the TLS version specified
      socket = makeSSLSocket(ipAddress, port, "TLSv" + tlsVersion);
    } catch (IOException e) {
      certificateReport += "IOException unable to connect to server.\n";
      System.err.println("testTLS IOException:" + e.getMessage());
      skipTls(tlsVersion);
      return false;
    }

    // Validate Server Certificates While using specified TLS version
    try {
      Certificate[] certificates = getServerCertificates(socket);
      boolean certValid = validateCertificates(certificates);
      passTls(certValid, tlsVersion);
      return certValid;
    } catch (SSLHandshakeException e) {
      certificateReport += "SSLHandshakeException: Unable to complete handshake\n";
      System.err.println("SSLHandshakeException:" + e.getMessage());
      passTls(false, tlsVersion);
      return false;
    }
  }

  private boolean validateCertificates(Certificate[] certificates) {
    for (Certificate certificate : certificates) {

      if (certificate instanceof X509Certificate) {
        try {
          ((X509Certificate) certificate).checkValidity();
          certificateReport += "Certificate is active for current date.\n";
          certificateReport += "\n";
          certificateReport += "Certificate:\n" + certificate + "\n";
          return true;

        } catch (CertificateExpiredException cee) {
          certificateReport += "Certificate is expired.\n";
          return false;
        } catch (CertificateNotYetValidException e) {
          certificateReport += "Certificate not yet valid.\n";
          return false;
        }

      } else {
        certificateReport += "Unknown certificate type.\n";
        System.err.println("Unknown certificate type: " + certificate);
        return false;
      }
    }
    return false;
  }

  private void passX509(boolean status) {
    if (status) {
      certificateReport += "RESULT pass security.x509\n";
    } else {
      certificateReport += "RESULT fail security.x509\n";
    }
  }

  private void passTls(boolean status, String tlsVersion) {
    if (status) {
      certificateReport += "RESULT pass security.tls.v" + tlsVersion + "\n";
    } else {
      certificateReport += "RESULT fail security.tls.v" + tlsVersion + "\n";
    }
  }

  private void skipTls(String tlsVersion) {
    certificateReport += "RESULT skip security.tls.v" + tlsVersion + "\n";
  }

  private void skipTlsX509() {
    certificateReport += "RESULT skip security.x509\n";
  }

  /**
   * Creates a trust manager to accept all certificates. This is required since we need this test to
   * be able to connect to any device and can't know anything about the certificates before hand.
   *
   * @return A valid TrustManager which accepts all valid X509Certificates
   */
  private TrustManager[] trustAllManager() {
    // Create a trust manager that does not validate certificate chains
    return new TrustManager[] {
      new X509TrustManager() {
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
          return null;
        }

        public void checkClientTrusted(X509Certificate[] certs, String authType) {}

        public void checkServerTrusted(X509Certificate[] certs, String authType) {}
      }
    };
  }

  /**
   * Attemps to complete the SSL handshake and retrieve the Server Certificates. Server certificates
   * in this context refers to the device being tested.
   *
   * @param socket The SSLSocket which connects to the device for testing
   * @throws Exception
   */
  private Certificate[] getServerCertificates(SSLSocket socket) throws IOException {
    socket.startHandshake();
    return socket.getSession().getPeerCertificates();
  }

  /**
   * @param host This is the host IP address of the device being tested
   * @param port This is teh Port of the SSL connection for the device being tested.
   * @param protocol The SSL protocol to be tested.
   * @return SSLSocket which supports only the SSL protocol defined.
   * @throws Exception
   */
  private SSLSocket makeSSLSocket(String host, int port, String protocol)
      throws NoSuchAlgorithmException, KeyManagementException, IOException {
    SSLSocketFactory factory = makeSSLFactory(trustAllManager(), protocol);

    SSLSocket socket = (SSLSocket) factory.createSocket(host, port);
    socket.setEnabledProtocols(new String[] {protocol});
    return socket;
  }

  /**
   * Create an SSLSocketFactory with the defined trust manager and protocol
   *
   * @param trustManager TrustManager to be used in the SSLContext
   * @param protocol The SSL protocol to be used in the SSLContext
   * @return An initialized SSLSocketFactory with SSLContext defined by input parameters
   * @throws Exception
   */
  private SSLSocketFactory makeSSLFactory(TrustManager[] trustManager, String protocol)
      throws NoSuchAlgorithmException, KeyManagementException {
    // Install the all-trusting trust manager
    SSLContext sslContext = SSLContext.getInstance(protocol);
    sslContext.init(null, trustManager, new java.security.SecureRandom());
    return sslContext.getSocketFactory();
  }
}

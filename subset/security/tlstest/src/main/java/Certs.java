import java.io.IOException;
import java.net.*;
import java.security.*;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class Certs {

  String ipAddress = "127.0.0.1";
  String certificateReport = "";

  public Certs(String ipAddress) {
    this.ipAddress = ipAddress;
  }

  public void get_certificate() {
    try {
      Report report = new Report();

      disableSslVerification();

      URL url;

      url = new URL(ipAddress);
      HttpsURLConnection httpsURLConnection;
      httpsURLConnection = (HttpsURLConnection) url.openConnection();
      httpsURLConnection.connect();
      Principal peername = httpsURLConnection.getPeerPrincipal();
      String cipher = httpsURLConnection.getCipherSuite();
      java.security.cert.Certificate[] certificates = httpsURLConnection.getServerCertificates();
      certificateReport += "Cipher:\n" + cipher;
      for (java.security.cert.Certificate certificate : certificates) {

        if (certificate instanceof X509Certificate) {
          try {
            ((X509Certificate) certificate).checkValidity();
            certificateReport += "\nCertificate is active for current date";
          } catch (CertificateExpiredException cee) {
            certificateReport += "\nCertificate is expired";
          } catch (CertificateNotYetValidException e) {
        	  System.err.println("X509Certificate CertificateNotYetValidException:" + e.getMessage());
          }

          certificateReport += "\n";

          certificateReport += "Certificate:\n" + certificate;

        } else {
          System.err.println("Unknown certificate type: " + certificate);
        }
      }

      report.writeReport(certificateReport);
    } catch (MalformedURLException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static void disableSslVerification() {
    try {
      // Create a trust manager that does not validate certificate chains
      TrustManager[] trustAllCerts =
          new TrustManager[] {
            new X509TrustManager() {
              public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
              }

              public void checkClientTrusted(X509Certificate[] certs, String authType) {}

              public void checkServerTrusted(X509Certificate[] certs, String authType) {}
            }
          };

      // Install the all-trusting trust manager
      SSLContext sslContext = SSLContext.getInstance("SSL");
      sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
      HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());

      // Create all-trusting host name verifier
      HostnameVerifier allHostsValid =
          new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
              return true;
            }
          };

      // Install the all-trusting host verifier
      HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
    } catch (NoSuchAlgorithmException e) {
      System.err.println("disableSslVerification NoSuchAlgorithmException:" + e.getMessage());
    } catch (KeyManagementException e) {
      System.err.println("disableSslVerification KeyManagementException:" + e.getMessage());
    }
  }
}
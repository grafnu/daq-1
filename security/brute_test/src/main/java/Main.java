

public class Main {

public static void main(String[] args) {
		
		//WRITE EITHER telnet OR ssh AS A PROTOCOL
//		
//		if(args.length != 3) {
//			throw new IllegalArgumentException("Usage:  host protocol(ssh/telnet) port");
//		}
//		
//		String host = args[0];
//		String protocol = args[1];	
//		int connectionPort = Integer.parseInt(args[2]);
		
		String host = "192.168.1.2";
		String protocol = "telnet";
		int connectionPort = 23;

		System.out.println("Main Started...");
		PackageManager packageManager = new PackageManager(protocol, host, connectionPort);
		}

}

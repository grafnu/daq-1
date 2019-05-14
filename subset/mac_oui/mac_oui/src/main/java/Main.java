
public class Main {

	public static void main(String[] args) {
	
		  
//		if (args.length != 1) {
//	      throw new IllegalArgumentException(
//	          "Usage: target_mac");
//	    }
		
//	String macAddress = args[0];
	String macAddress = "01:1a:0d:we:09:28";
	System.out.println("Main Started...");
	RetrieveList setupTest = new RetrieveList(macAddress);
	}
}

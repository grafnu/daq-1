package alliedswitch;

public class Main {

	public static void main(String[] args) {
		
		//String ipAddress = "127.0.0.1";
		String ipAddress = "192.168.1.2";
		int interfacePort = 10;
		
		char[] command = new char[2];
		int count = 0;
		
		try {
			if(args!=null) {
				if(args[0]!=null) {
					ipAddress = args[0];
				}
				
				if(args[1]!=null) {
					interfacePort = Integer.parseInt(args[1]);
				}
			}
		}
		catch (Exception e){
			System.err.println("Exception main parse args:"+e.getMessage());
		}
		
		AlliedInterrogator alliedInterrogator = new AlliedInterrogator(ipAddress, interfacePort);
		
		Thread alliedInterrogatorThread = new Thread(alliedInterrogator);
		alliedInterrogatorThread.start();
	}
}

import java.util.Map;

public class MacLookup implements Runnable{
	String macAddress;
	Map macDevices;
	ReportHandler reportHandler = new ReportHandler();

	MacLookup(Map macDevices, String macAddress){
		this.macDevices = macDevices;
		this.macAddress = macAddress;
	}

	public void startTest(){
		System.out.println("Starting connection.mac_oui test...");
		String splicedMac = macAddress.replace(":", "");
		String formattedMac = splicedMac.substring(0, 6).toUpperCase();
		System.out.println(formattedMac);
		try{
			String manufacturer = macDevices.get(formattedMac).toString();
			if(manufacturer.equals(null)) {
				System.out.println("Could not find manufacturer");
				reportHandler.addText("RESULT connection.mac_oui FAILED *");
				reportHandler.addText("could not find device manufacturer *");
				reportHandler.writeReport();
			}
			else {
				reportHandler.addText("RESULT connection.mac_oui PASSED *");
				reportHandler.addText(formattedMac + " " + manufacturer + " *");
				reportHandler.writeReport();
				System.out.println(formattedMac + " " + manufacturer);
			}
		}
		catch(NullPointerException e) {
			System.out.println(e + " could not find the manufacturer");
			reportHandler.addText("RESULT connection.mac_oui FAILED *");
			reportHandler.addText("could not find device manufacturer *");
			reportHandler.writeReport();
		}
	}

	@Override
	public void run() {
		startTest();
	}
}

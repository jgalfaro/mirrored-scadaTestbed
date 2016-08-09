package net.wimpi.modbus;

import java.util.ArrayList;

import net.wimpi.modbus.procimg.IllegalAddressException;

/**
 * @author Ken LE PRADO
 *
 */
public class ModbusDeviceIdentification {
	private final static short MAX_OBJECT_IDENTIFICATION = 256;
	public final static int BASIC = 1;
	public final static int REGULAR = 2;
	public final static int EXTENDED = 3;
	public final static int SPECIFIC = 4;
	private String objects[];
	
	public ModbusDeviceIdentification() {
		objects = new String[MAX_OBJECT_IDENTIFICATION];
	}

	/**
	 * Add a new value in the device identification
	 * @param objectId Identifier of the value to set
	 * @param value Value to define
	 * @throws IllegalAddressException
	 */
	public void addIdentification(int objectId, String value) throws IllegalAddressException {
		if (objectId > MAX_OBJECT_IDENTIFICATION) {
			throw new IllegalAddressException();
		}
		objects[objectId] = value;
	}

	
	public void setIdentification(int objectId, String value) throws IllegalAddressException {
		if (objectId > MAX_OBJECT_IDENTIFICATION) {
			throw new IllegalAddressException();
		}
		objects[objectId] = value;
	}
	
	/**
	 * Returns the value of an identification
	 * 
	 * @param objectId
	 * @return
	 */
	public String getIdentification(int objectId) {

		return objects[objectId];
	}
	
	public boolean isDefined(int objectId) {
		if (objects[objectId] == null) {
			return false;
		}
		return true;
	}
	
	
	/*
	 * Extract an array of values from the identification
	 * param extractType Type of extraction (1/2/3/4)
	 * param objectId : start of extraction or 0. If Type=4, objectId = extracted Object
	 */
	public ArrayList<ModbusDeviceItem> getExtract(int extractType, int objectId) throws IllegalAddressException {
		ArrayList<ModbusDeviceItem> extract = new ArrayList<ModbusDeviceItem>();
		int i = 0;
		
		switch (extractType) {
		case BASIC:
			for (i = max (0, objectId); i< 3; i++) {
				if (!this.isDefined(i)) {
					continue;
				}
				extract.add(new ModbusDeviceItem(i, objects[i]));
			}			
			break;
		case REGULAR:
			for (i = max (3, objectId); i< 128; i++) {
				if (!this.isDefined(i)) {
					continue;
				}
				extract.add(new ModbusDeviceItem(i, objects[i]));
			}			
			break;
		case EXTENDED:
			for (i = max (128, objectId); i< 256; i++) {
				if (!this.isDefined(i)) {
					continue;
				}
				extract.add(new ModbusDeviceItem(i, objects[i]));
			}						
			break;
		case SPECIFIC:
			if (this.isDefined(objectId)) {
				extract.add(new ModbusDeviceItem(objectId, objects[objectId]));
			}
			break;
		default:
			throw new IllegalAddressException();
		}
		
		return extract;
	}
	
	public int getLength() {
		return objects.length;
	}
	
	private int max (int a, int b) {
		if (a<b) {
			return b;
		} else {
			return a;
		}
	}
	
	public void print() {
		int i ;
		for (i = 0; i < 30; i++) {
			System.out.println("Id : " + i + " Value : " + objects[i]);
		}
	}
}

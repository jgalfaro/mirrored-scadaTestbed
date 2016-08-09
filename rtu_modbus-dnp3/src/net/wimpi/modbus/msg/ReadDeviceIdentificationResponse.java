//License
/***
 * Java Modbus Library (jamod)
 * Copyright (c) 2002-2004, jamod development team
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of the author nor the names of its contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDER AND CONTRIBUTORS ``AS
 * IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 ***/
package net.wimpi.modbus.msg;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

import net.wimpi.modbus.ModbusDeviceIdentification;
import net.wimpi.modbus.ModbusDeviceItem;

/**
 * Class implementing a <tt>ReadMultipleRegistersResponse</tt>.
 * The implementation directly correlates with the class 0
 * function <i>read multiple registers (FC 3)</i>. It encapsulates
 * the corresponding response message.
 *
 * @author Dieter Wimberger
 * @version 1.2rc1 (09/11/2004)
 */
public final class ReadDeviceIdentificationResponse
    extends ModbusResponse {

  //instance attributes
  private ArrayList<ModbusDeviceItem> m_Extract;
  private int m_MEIType = 0;
  private int m_ReadDeviceCode = 0;
  private ModbusDeviceIdentification deviceIdent;

  /**
   * Constructs a new <tt>ReadMultipleRegistersResponse</tt>
   * instance.
   */
  public ReadDeviceIdentificationResponse() {
    super();
  }//constructor

  /**
   * Constructs a new <tt>ReadInputRegistersResponse</tt>
   * instance.
   *
   * @param registers the Register[] holding response registers.
   */
  public ReadDeviceIdentificationResponse(ArrayList<ModbusDeviceItem> extract) {
    super();
    m_Extract = extract;
	  setDataLength(getTotalLength());
    
  }//constructor

  public int getExtractCount() {
	  return m_Extract.size();
  }

  public int getTotalLength() {
	  int totalBytes = 6;
	  Enumeration<ModbusDeviceItem> e = Collections.enumeration(m_Extract);
	  ModbusDeviceItem item;
	  while (e.hasMoreElements()) {
	      item = e.nextElement();
	  	  
	      //Check limit of 255 o for a packet length
	      if ( totalBytes + 2 + item.getValueLength() > 255) {
	    	 break;
	      }

	      totalBytes += 2 + item.getValueLength();
	  }
 
	  return totalBytes;

  }
  
  public void setMEIType(int type) {
	  m_MEIType = type;
  }
  public int getMEIType() {
	  return m_MEIType;
  }
  
  public void setReadDeviceCode(int code) {
	  m_ReadDeviceCode = code;
  }
  public int getReadDeviceCode() {
	  return m_ReadDeviceCode;
  }
  public int getConformityLevel() {
	  return m_ReadDeviceCode + 128;
  }
  
  public ModbusDeviceIdentification getDeviceIdent() {
	  return deviceIdent;
  }
  
  public void writeData(DataOutput dout)
      throws IOException {
	  int totalBytes = 0;
	  Enumeration<ModbusDeviceItem> e = Collections.enumeration(m_Extract);
	  ModbusDeviceItem item;

	  int nextObjectId = 0;
	  boolean needFragmentation = false;
	  //Determine the fragmentation and the next objectID
	  e = Collections.enumeration(m_Extract);
	  
	  totalBytes = 6;
	  int nbElementsSent = 0;
	  while (e.hasMoreElements()) {
	      item = e.nextElement();
	  	  
	      //Check limit of 255 o for a packet length
	      if ( totalBytes + 2 + item.getValueLength() > 255) {
	    	  System.err.println("Data too long, truncated. Next : " + nextObjectId);
	    	  nextObjectId = item.getId();
	    	  needFragmentation = true;
	    	  break;
	      }
	      totalBytes += 2 + item.getValueLength();
	      nbElementsSent++;
	  }
	  
	  //Function code (already written)
	  //MEI Type
	  dout.writeByte(getMEIType());
	  //Read Device Code
	  dout.writeByte(getReadDeviceCode());
	  //Conformity level
	  dout.writeByte(getConformityLevel());
	  //More follows
	  if (needFragmentation) {
		  dout.writeByte(255);	  
	  } else {
		  dout.writeByte(0);
	  }
	  //Next Object ID
	  dout.writeByte(nextObjectId);
	  //Number of objects
	  dout.writeByte(nbElementsSent);

	  e = Collections.enumeration(m_Extract);
	  totalBytes = 6;
	  while (e.hasMoreElements()) {
	      item = e.nextElement();
	  	  
	      //Check limit of 255 o for a packet length
	      if ( totalBytes + 2 + item.getValueLength() > 255) {
	    	  System.err.println("Data too long, truncated");
	    	 break;
	      }
	      //Object Id
		  dout.writeByte((Integer) item.getId());
	      //Object Length
		  dout.writeByte(item.getValueLength());
	      //Object Value
		  dout.writeChars(item.getValue());

	      totalBytes += 2 + item.getValueLength();
	  }
	  	  
  }//writeData

  public void readData(DataInput din)
      throws IOException {
 	  
	  int nbObjects = 0;
	  int i, j;
	  int objectId;
	  int objectLength;
	  String objectValue = "";
	  deviceIdent = new ModbusDeviceIdentification();
	  
	  //MEI Type
	  m_MEIType = din.readUnsignedByte();
	  //Read Device Code
	  m_ReadDeviceCode = din.readUnsignedByte();
	  //Conformity Level
	  din.readUnsignedByte();
	  //More follows
	  din.readUnsignedByte();
	  //Next Object ID
	  din.readUnsignedByte();
	  //Nb objects
	  nbObjects = din.readUnsignedByte();

	  for (i = 0; i < nbObjects; i++) {
		  objectId = din.readUnsignedByte();
		  objectLength = din.readUnsignedByte();
		  objectValue = "";
		  for (j=0; j<objectLength / 2; j++) {
			  objectValue += din.readChar();
		  }
		  deviceIdent.setIdentification(objectId, objectValue);
	  }
	  
  }//readData


}//class ReadMultipleRegistersResponse

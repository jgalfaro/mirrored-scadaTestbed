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

import net.wimpi.modbus.Modbus;
import net.wimpi.modbus.ModbusCoupler;
import net.wimpi.modbus.ModbusDeviceIdentification;
import net.wimpi.modbus.ModbusDeviceItem;
import net.wimpi.modbus.procimg.IllegalAddressException;

/**
 * Class implementing a <tt>ReadMultipleRegistersRequest</tt>.
 * The implementation directly correlates with the class 0
 * function <i>read multiple registers (FC 3)</i>. ItCode
 * encapsulates the corresponding request message.
 *
 * @author Dieter Wimberger
 * @version 1.2rc1 (09/11/2004)
 */
public final class ReadDeviceIdentificationRequest
    extends ModbusRequest {

  //instance attributes
  private int m_MEIType;
  private int m_ReadDeviceCode;
  private int m_ObjectId = 0;
  
  /**
   * Constructs a new <tt>ReadMultipleRegistersRequest</tt>
   * instance.
   */
  public ReadDeviceIdentificationRequest() {
    super();
    setFunctionCode(Modbus.READ_DEVICE_IDENTIFICATION);
    //3 bytes (remember unit identifier and function
    //code are excluded)
    setDataLength(3);
  }//constructor

  public ReadDeviceIdentificationRequest(int type, int code, int id) {
	    super();
	    setFunctionCode(Modbus.READ_DEVICE_IDENTIFICATION);
	    setDataLength(3);

	    setMEIType(type);
	    setReadDeviceCode(code);
	    setObjectId(id);
	    
	  }//constructor

  public ReadDeviceIdentificationRequest(int code, int id) {
	    super();
	    setFunctionCode(Modbus.READ_DEVICE_IDENTIFICATION);
	    setDataLength(3);

	    setMEIType(Modbus.DEFAULT_MEI_TYPE);
	    setReadDeviceCode(code);
	    setObjectId(id);
	    
	  }//constructor

  public ReadDeviceIdentificationRequest(int id) {
	    super();
	    setFunctionCode(Modbus.READ_DEVICE_IDENTIFICATION);
	    setDataLength(3);

	    setMEIType(Modbus.DEFAULT_MEI_TYPE);
	    setReadDeviceCode(ModbusDeviceIdentification.SPECIFIC);
	    setObjectId(id);
	    
	  }//constructor

  
  public ModbusResponse createResponse() {
	    ReadDeviceIdentificationResponse response = null;
		ArrayList<ModbusDeviceItem> extract = new ArrayList<ModbusDeviceItem>();
	    
	    ModbusDeviceIdentification pIdent = ModbusCoupler.getReference().getIdentification();
	    
	    try {
	    	extract = pIdent.getExtract(m_ReadDeviceCode, m_ObjectId);
	      } catch (IllegalAddressException iaex) {
	        return createExceptionResponse(Modbus.ILLEGAL_ADDRESS_EXCEPTION);
	      }

	    response = new ReadDeviceIdentificationResponse(extract);
	    //transfer header data
	    if (!isHeadless()) {
	      response.setTransactionID(this.getTransactionID());
	      response.setProtocolID(this.getProtocolID());
	    } else {
	      response.setHeadless();
	    }

	    response.setUnitID(this.getUnitID());
	    response.setFunctionCode(this.getFunctionCode());
	    response.setMEIType(this.getMEIType());
	    response.setReadDeviceCode(this.getReadDeviceCode());

	    return response;
  }//createResponse

  
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
  
  public void setObjectId(int id) {
	  m_ObjectId = id;
  }
  public int getObjectId() {
	  return m_ObjectId;
  }


  public void writeData(DataOutput dout)
      throws IOException {
	    dout.writeByte(m_MEIType);
	    dout.writeByte(m_ReadDeviceCode);
	    dout.writeByte(m_ObjectId);
  }//writeData

  public void readData(DataInput din)
      throws IOException {
	  m_MEIType = din.readUnsignedByte();
	  m_ReadDeviceCode = din.readUnsignedByte();
	  m_ObjectId = din.readUnsignedByte();
  }//readData

}//class ReadMultipleRegistersRequest

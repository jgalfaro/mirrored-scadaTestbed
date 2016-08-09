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

import net.wimpi.modbus.Modbus;
import net.wimpi.modbus.ModbusCoupler;
import net.wimpi.modbus.io.NonWordDataHandler;
import net.wimpi.modbus.procimg.IllegalAddressException;
import net.wimpi.modbus.procimg.ProcessImage;
import net.wimpi.modbus.procimg.ProcessImageFactory;
import net.wimpi.modbus.procimg.Register;

/**
 * Class implementing a <tt>ReadWriteMultipleRegistersRequest</tt>.
 * The implementation directly correlates with the class 0
 * function <i>read multiple registers (FC 3)</i>. It
 * encapsulates the corresponding request message.
 *
 * @author Ken LE PRADO
 * @version 1.2rc1 (09/07/2014)
 */
public final class ReadWriteMultipleRegistersRequest
    extends ModbusRequest {

  //instance attributes
  private int m_Reference_R;
  private int m_WordCount_R;
  private int m_Reference_W;
  private Register[] m_Registers_W;
  private NonWordDataHandler m_NonWordDataHandler_W = null;

  
  /**
   * Constructs a new <tt>ReadWriteMultipleRegistersRequest</tt>
   * instance.
   */
  public ReadWriteMultipleRegistersRequest() {
    super();
    setFunctionCode(Modbus.READ_WRITE_MULTIPLE_REGISTERS);
    //4 bytes (remember unit identifier and function
    //code are excluded)
    setDataLength(4);
  }//constructor

  /**
   * Constructs a new <tt>ReadMultipleRegistersRequest</tt>
   * instance with a given reference and count of words
   * to be read.
   * <p>
   * @param ref the reference number of the register
   *        to read from.
   * @param count the number of words to be read.
   */
  public ReadWriteMultipleRegistersRequest(int ref_R, int count_R, int ref_W, Register[] registers_W) {
    super();
    setFunctionCode(Modbus.READ_WRITE_MULTIPLE_REGISTERS);
    setReferenceR(ref_R);
    setWordCountR(count_R);
    setReferenceW(ref_W);
    setRegistersW(registers_W);

    setDataLength(9 + getByteCountW());
    
  }//constructor

  public ModbusResponse createResponse() {
    ReadMultipleRegistersResponse response = null;
    Register[] regs = null;

    //1. get process image
    ProcessImage procimg = ModbusCoupler.getReference().getProcessImage();

    //2. Write values (before read)
    try {
        regs = procimg.getRegisterRange(this.getReferenceW(), this.getWordCountW());
        //3. set Register values
        for (int i = 0; i < regs.length; i++) {
          regs[i].setValue(this.getRegisterW(i).toBytes());
        }
      } catch (IllegalAddressException iaex) {
        return createExceptionResponse(Modbus.ILLEGAL_ADDRESS_EXCEPTION);
      }
    
    //3. get input registers range
    try {
      regs = procimg.getRegisterRange(this.getReferenceR(), this.getWordCountR());
    } catch (IllegalAddressException iaex) {
      return createExceptionResponse(Modbus.ILLEGAL_ADDRESS_EXCEPTION);
    }
    response = new ReadMultipleRegistersResponse(regs);
    //transfer header data
    if (!isHeadless()) {
      response.setTransactionID(this.getTransactionID());
      response.setProtocolID(this.getProtocolID());
    } else {
      response.setHeadless();
    }
    response.setUnitID(this.getUnitID());
    response.setFunctionCode(this.getFunctionCode());

    return response;
  }//createResponse

  /**
   * Sets the reference of the register to start reading
   * from with this <tt>ReadMultipleRegistersRequest</tt>.
   * <p>
   * @param ref the reference of the register
   *        to start reading from.
   */
  public void setReferenceR(int ref) {
    m_Reference_R = ref;
  }//setReference

  /**
   * Returns the reference of the register to to start
   * reading from with this
   * <tt>ReadMultipleRegistersRequest</tt>.
   * <p>
   * @return the reference of the register
   *        to start reading from as <tt>int</tt>.
   */
  public int getReferenceR() {
    return m_Reference_R;
  }//getReference

  /**
   * Sets the number of words to be read with this
   * <tt>ReadMultipleRegistersRequest</tt>.
   * <p>
   * @param count the number of words to be read.
   */
  public void setWordCountR(int count) {
    m_WordCount_R = count;
    //setChanged(true);
  }//setWordCount

  /**
   * Returns the number of words to be read with this
   * <tt>ReadMultipleRegistersRequest</tt>.
   * <p>
   * @return the number of words to be read as
   *        <tt>int</tt>.
   */
  public int getWordCountR() {
    return m_WordCount_R;
  }//getWordCount

  

  /**
   * Sets the reference of the register to start reading
   * from with this <tt>ReadMultipleRegistersRequest</tt>.
   * <p>
   * @param ref the reference of the register
   *        to start reading from.
   */
  public void setReferenceW(int ref) {
    m_Reference_W = ref;
  }//setReference

  /**
   * Returns the reference of the register to to start
   * reading from with this
   * <tt>ReadMultipleRegistersRequest</tt>.
   * <p>
   * @return the reference of the register
   *        to start reading from as <tt>int</tt>.
   */
  public int getReferenceW() {
    return m_Reference_W;
  }//getReference
  
  

  /**
   * Sets the registers to be written with this
   * <tt>WriteMultipleRegistersRequest</tt>.
   * <p>
   * @param registers the registers to be written
   *        as <tt>Register[]</tt>.
   */
  public void setRegistersW(Register[] registers) {
    m_Registers_W = registers;
  }//setRegisters


  /**
   * Returns the registers to be written with this
   * <tt>WriteMultipleRegistersRequest</tt>.
   * <p>
   * @return the registers to be written as <tt>Register[]</tt>.
   */
  public Register[] getRegistersW() {
    return m_Registers_W;
  }//getRegisters

  /**
   * Returns the <tt>Register</tt> at
   * the given position (relative to the reference
   * used in the request).
   * <p>
   * @param index the relative index of the <tt>Register</tt>.
   *
   * @return the register as <tt>Register</tt>.
   *
   * @throws IndexOutOfBoundsException if
   *         the index is out of bounds.
   */
  public Register getRegisterW(int index)
      throws IndexOutOfBoundsException {

    if (index >= getWordCountW()) {
      throw new IndexOutOfBoundsException();
    } else {
      return m_Registers_W[index];
    }
  }//getRegister
    
  /**
   * Returns the value of the register at
   * the given position (relative to the reference
   * used in the request) interpreted as unsigned short.
   * <p>
   * @param index the relative index of the register
   *        for which the value should be retrieved.
   *
   * @return the value as <tt>int</tt>.
   *
   * @throws IndexOutOfBoundsException if
   *         the index is out of bounds.
   */
  public int getRegisterValueW(int index)
      throws IndexOutOfBoundsException {
    return m_Registers_W[index].toUnsignedShort();
  }//getRegisterValue


  /**
   * Returns the number of bytes representing the
   * values to be written.
   * <p>
   * @return the number of bytes to be written
   *         as <tt>int</tt>.
   */
  public int getByteCountW() {
    return getWordCountW() * 2;
  }//getByteCount
  

  /**
   * Returns the number of words to be written.
   * <p>
   * @return the number of words to be written
   *         as <tt>int</tt>.
   */
  public int getWordCountW() {
    return m_Registers_W.length;
  }//getWordCount


  /**
   * Sets a non word data handler.
   *
   * @param dhandler a  <tt>NonWordDataHandler</tt> instance.
   */
  public void setNonWordDataHandler(NonWordDataHandler dhandler) {
    m_NonWordDataHandler_W = dhandler;
  }//setNonWordDataHandler

  /**
   * Returns the actual non word data handler.
   *
   * @return the actual <tt>NonWordDataHandler</tt>.
   */
  public NonWordDataHandler getNonWordDataHandler() {
    return m_NonWordDataHandler_W;
  }//getNonWordDataHandler

  
  public void writeData(DataOutput dout)
      throws IOException {
	  
	  //Read part
    dout.writeShort(m_Reference_R);
    dout.writeShort(m_WordCount_R);
    
      //Write part
    
    //1. the reference
    dout.writeShort(m_Reference_W);
    //2. the word count
    dout.writeShort(getWordCountW());
    //3. the byte count as byte
    dout.writeByte(getByteCountW());
    //4. write values
    if (m_NonWordDataHandler_W == null) {
      for (int n = 0; n < m_Registers_W.length; n++) {
        dout.write(m_Registers_W[n].toBytes());
      }
    } else {
      m_NonWordDataHandler_W.prepareData(getReferenceW(), getWordCountW());
      dout.write(m_NonWordDataHandler_W.getData());
    }  
        
  }//writeData

  public void readData(DataInput din)
      throws IOException {
    
	//Read part  
	m_Reference_R = din.readUnsignedShort();
    m_WordCount_R = din.readUnsignedShort();
    
    //Write part

    m_Reference_W = din.readShort();
    //read lengths
    int wc = din.readUnsignedShort();
    int bc = din.readUnsignedByte();

    //read values
    if (m_NonWordDataHandler_W == null) {
      m_Registers_W = new Register[wc];
      ProcessImageFactory pimf = ModbusCoupler.getReference().getProcessImageFactory();
      for (int i = 0; i < wc; i++) {
        m_Registers_W[i] = pimf.createRegister(din.readByte(), din.readByte());
      }
    } else {
      m_NonWordDataHandler_W.readData(din, m_Reference_W, wc);
    }  
    
  }//readData

}//class ReadMultipleRegistersRequest

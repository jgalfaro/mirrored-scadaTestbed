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

import net.wimpi.modbus.procimg.Register;
import net.wimpi.modbus.procimg.ProcessImageFactory;
import net.wimpi.modbus.ModbusCoupler;

/**
 * Class implementing a <tt>ReadWriteMultipleRegistersResponse</tt>.
 *
 * @author Dieter Wimberger
 * @version 1.2rc1 (09/07/2014)
 */
public final class ReadWriteMultipleRegistersResponse
    extends ModbusResponse {

  //instance attributes
  private int m_ByteCount_R;
  private Register[] m_Registers_R;

  private int m_WordCount_W;
  private int m_Reference_W;
  
  /**
   * Constructs a new <tt>ReadWriteMultipleRegistersResponse</tt>
   * instance.
   */
  public ReadWriteMultipleRegistersResponse() {
    super();
  }//constructor

  /**
   * Constructs a new <tt>ReadWriteInputRegistersResponse</tt>
   * instance.
   *
   * @param registers the Register[] holding response registers.
   */
  public ReadWriteMultipleRegistersResponse(Register[] registers_R, int reference_W, int wordcount_W) {
    super();
    m_Registers_R = registers_R;
    m_ByteCount_R = registers_R.length * 2;
    m_Reference_W = reference_W;
    m_WordCount_W = wordcount_W;

    //set correct data length excluding unit id and fc
    setDataLength(m_ByteCount_R + 5);
  }//constructor


  /**
   * Returns the number of bytes that have been read.
   * <p>
   * @return the number of bytes that have been read
   *         as <tt>int</tt>.
   */
  public int getByteCountR() {
    return m_ByteCount_R;
  }//getByteCount

  /**
   * Returns the number of words that have been read.
   * The returned value should be half of the
   * the byte count of this
   * <tt>ReadMultipleRegistersResponse</tt>.
   * <p>
   * @return the number of words that have been read
   *         as <tt>int</tt>.
   */
  public int getWordCountR() {
    return m_ByteCount_R / 2;
  }//getWordCount

  /**
   * Sets the number of bytes that have been returned.
   * <p>
   * @param count the number of bytes as <tt>int</tt>.
   */
  private void setByteCountR(int count) {
    m_ByteCount_R = count;
  }//setByteCount

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
  public int getRegisterValueR(int index)
      throws IndexOutOfBoundsException {
    return m_Registers_R[index].toUnsignedShort();
  }//getRegisterValue

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
  public Register getRegisterR(int index)
      throws IndexOutOfBoundsException {

    if (index >= getWordCountR()) {
      throw new IndexOutOfBoundsException();
    } else {
      return m_Registers_R[index];
    }
  }//getRegister

  /**
   * Returns a reference to the array of registers
   * read.
   *
   * @return a <tt>Register[]</tt> instance.
   */
  public Register[] getRegistersR() {
    return m_Registers_R;
  }//getRegisters

  
  

  /**
   * Sets the reference of the register to start writing to
   * with this <tt>WriteMultipleRegistersResponse</tt>.
   * <p>
   * @param ref the reference of the register
   *        to start writing to as <tt>int</tt>.
   */
  private void setReferenceW(int ref) {
    m_Reference_W = ref;
  }//setReference

  /**
   * Returns the reference of the register to start
   * writing to with this
   * <tt>WriteMultipleRegistersResponse</tt>.
   * <p>
   * @return the reference of the register
   *        to start writing to as <tt>int</tt>.
   */
  public int getReferenceW() {
    return m_Reference_W;
  }//getReference


  /**
   * Returns the number of bytes that have been written.
   * <p>
   * @return the number of bytes that have been read
   *         as <tt>int</tt>.
   */
  public int getByteCountW() {
    return m_WordCount_W * 2;
  }//getByteCount

  /**
   * Returns the number of words that have been read.
   * The returned value should be half of
   * the byte count of the response.
   * <p>
   * @return the number of words that have been read
   *         as <tt>int</tt>.
   */
  public int getWordCountW() {
    return m_WordCount_W;
  }//getWordCount

  /**
   * Sets the number of words that have been returned.
   * <p>
   * @param count the number of words as <tt>int</tt>.
   */
  private void setWordCountW(int count) {
    m_WordCount_W = count;
  }//setWordCount
  
    
  public void writeData(DataOutput dout)
      throws IOException {
    dout.writeByte(m_ByteCount_R);
    for (int k = 0; k < getWordCountR(); k++) {
      dout.write(m_Registers_R[k].toBytes());
    }
  }//writeData

  public void readData(DataInput din)
      throws IOException {
    setByteCountR(din.readUnsignedByte());

    m_Registers_R = new Register[getWordCountR()];
    ProcessImageFactory pimf = ModbusCoupler.getReference().getProcessImageFactory();

    for (int k = 0; k < getWordCountR(); k++) {
      m_Registers_R[k] = pimf.createRegister(din.readByte(), din.readByte());
    }

    //update data length
    setDataLength(getByteCountR() + 1);
  }//readData


}//class ReadMultipleRegistersResponse

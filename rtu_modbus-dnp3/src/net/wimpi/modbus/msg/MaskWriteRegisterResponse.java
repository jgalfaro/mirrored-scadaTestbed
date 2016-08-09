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

/**
 * Class implementing a <tt>MaskWriteRegisterResponse</tt>.
 * The implementation directly correlates with the class 0
 * function <i>write single register (FC 6)</i>. It
 * encapsulates the corresponding response message.
 *
 * @author Dieter Wimberger
 * @version 1.2rc1 (09/11/2004)
 */
public final class MaskWriteRegisterResponse
    extends ModbusResponse {

  //instance attributes
  private int m_Reference;
  private int m_AndMask;
  private int m_OrMask;

  /**
   * Constructs a new <tt>MaskWriteRegisterResponse</tt>
   * instance.
   */
  public MaskWriteRegisterResponse() {
    super();
  }//constructor

  /**
   * Constructs a new <tt>WriteSingleRegisterResponse</tt>
   * instance.
   *
   * @param reference the offset of the register written.
   * @param value the value of the register.
   */
  public MaskWriteRegisterResponse(int reference, int andMask, int orMask) {
    super();
    setReference(reference);
    setAndMask(andMask);
    setOrMask(orMask);
    setDataLength(6);
  }//constructor


  /**
   * Returns the reference of the register
   * that has been written to.
   * <p>
   * @return the reference of the written register.
   */
  public int getReference() {
    return m_Reference;
  }//getReference

  /**
   * Sets the reference of the register that has
   * been written to.
   * <p>
   * @param ref the reference of the written register.
   */
  private void setReference(int ref) {
    m_Reference = ref;
    //setChanged(true);
  }//setReference
  
  

  /**
   * Sets the reference of the register to be written
   * to with this <tt>WriteSingleRegisterRequest</tt>.
   * <p/>
   *
   * @param ref the reference of the register
   *            to be written to.
   */
  public void setAndMask(int andMask) {
    m_AndMask = andMask;
    //setChanged(true);
  }//setReference

  /**
   * Returns the reference of the register to be
   * written to with this
   * <tt>WriteSingleRegisterRequest</tt>.
   * <p/>
   *
   * @return the reference of the register
   *         to be written to.
   */
  public int getAndMask() {
    return m_AndMask;
  }//getReference
  

  /**
   * Sets the reference of the register to be written
   * to with this <tt>WriteSingleRegisterRequest</tt>.
   * <p/>
   *
   * @param ref the reference of the register
   *            to be written to.
   */
  public void setOrMask(int orMask) {
    m_OrMask = orMask;
    //setChanged(true);
  }//setReference

  /**
   * Returns the reference of the register to be
   * written to with this
   * <tt>WriteSingleRegisterRequest</tt>.
   * <p/>
   *
   * @return the reference of the register
   *         to be written to.
   */
  public int getOrMask() {
    return m_OrMask;
  }//getReference

  public void writeData(DataOutput dout)
      throws IOException {
    dout.writeShort(getReference());
    dout.writeShort(getAndMask());
    dout.writeShort(getOrMask());
    
  }//writeData

  public void readData(DataInput din)
      throws IOException {
    setReference(din.readUnsignedShort());
    setAndMask(din.readUnsignedShort());
    setOrMask(din.readUnsignedShort());

    //update data length
    setDataLength(6);
  }//readData


}//class WriteSingleRegisterResponse

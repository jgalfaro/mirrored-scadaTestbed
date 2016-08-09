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
package net.wimpi.modbus.cmd;

import net.wimpi.modbus.Modbus;
import net.wimpi.modbus.ModbusCoupler;
import net.wimpi.modbus.net.ModbusSerialListener;
import net.wimpi.modbus.procimg.*;
import net.wimpi.modbus.util.SerialParameters;


/**
 * Class implementing a simple Modbus slave.
 * A simple process image is available to test
 * functionality and behaviour of the implementation.
 *
 * @author Dieter Wimberger
 * @version 1.2rc1 (09/11/2004)
 */
public class SerialSlaveTest {

  public static void main(String[] args) {

    ModbusSerialListener listener = null;
    SimpleProcessImage spi = new SimpleProcessImage();
    String portname = args[0];

    if (Modbus.debug) System.out.println("jModbus ModbusSerial Slave");
    
    try {

      //1. Prepare a process image
      spi = new SimpleProcessImage();
      spi.addDigitalOut(new SimpleDigitalOut(true));
      spi.addDigitalOut(new SimpleDigitalOut(false));
      spi.addDigitalIn(new SimpleDigitalIn(false));
      spi.addDigitalIn(new SimpleDigitalIn(true));
      spi.addDigitalIn(new SimpleDigitalIn(false));
      spi.addDigitalIn(new SimpleDigitalIn(true));
      spi.addRegister(new SimpleRegister(251));
      spi.addInputRegister(new SimpleInputRegister(45));

      //2. Create the coupler and set the slave identity
      ModbusCoupler.getReference().setProcessImage(spi);
      ModbusCoupler.getReference().setMaster(false);
      ModbusCoupler.getReference().setUnitID(2);

      //3. Set up serial parameters
      SerialParameters params = new SerialParameters();
      params.setPortName(portname);
      params.setBaudRate(9600);
      params.setDatabits(8);
      params.setParity("None");
      params.setStopbits(1);
      params.setEncoding("ascii");
      params.setEcho(false);
      if (Modbus.debug) System.out.println("Encoding [" + params.getEncoding() + "]");

      //4. Set up serial listener
      listener = new ModbusSerialListener(params);
      listener.setListening(true);

    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }//main

}//class SerialSlaveTest


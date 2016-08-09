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
package net.wimpi.modbus.net;

import net.wimpi.modbus.Modbus;
import net.wimpi.modbus.io.*;
import net.wimpi.modbus.util.SerialParameters;

import javax.comm.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.TooManyListenersException;

/**
 * Class that implements a serial connection which
 * can be used for master and slave implementations.
 *
 * @author Dieter Wimberger
 * @author John Charlton
 * @version 1.2rc1 (09/11/2004)
 */
public class SerialConnection
    implements SerialPortEventListener {

  private SerialParameters m_Parameters;
  private ModbusSerialTransport m_Transport;
  private CommPortIdentifier m_PortIdentifyer;
  private SerialPort m_SerialPort;
  private boolean m_Open;
  private InputStream m_SerialIn;

  /**
   * Creates a SerialConnection object and initilizes variables passed in
   * as params.
   *
   * @param parameters A SerialParameters object.
   */
  public SerialConnection(SerialParameters parameters) {
    m_Parameters = parameters;
    m_Open = false;
  }//constructor

  /**
   * Returns the <tt>ModbusTransport</tt> instance to be
   * used for receiving and sending messages.
   *
   * @return a <tt>ModbusTransport</tt> instance.
   */
  public ModbusTransport getModbusTransport() {
    return m_Transport;
  }//getModbusTransport

  /**
   * Opens the communication port.
   *
   * @throws Exception if an error occurs.
   */
  public void open() throws Exception {


    //1. obtain a CommPortIdentifier instance
    try {
      m_PortIdentifyer =
          CommPortIdentifier.getPortIdentifier(m_Parameters.getPortName());
    } catch (NoSuchPortException e) {
      if(Modbus.debug) System.out.println(e.getMessage());
      throw new Exception(e.getMessage());
    }
    //System.out.println("Got Port Identifier");

    //2. open the port, wait for given timeout
    try {
      m_SerialPort = (SerialPort)
          m_PortIdentifyer.open("Modbus Serial Master", 30000);
    } catch (PortInUseException e) {
      if(Modbus.debug) System.out.println(e.getMessage());

      throw new Exception(e.getMessage());
    }
    //System.out.println("Got Serial Port");


    //3. set the parameters
    try {
      setConnectionParameters();
    } catch (Exception e) {
      //ensure it is closed
      m_SerialPort.close();
      if(Modbus.debug) System.out.println(e.getMessage());
      throw e;
    }

    if (Modbus.SERIAL_ENCODING_ASCII.equals(m_Parameters.getEncoding())) {
      m_Transport = new ModbusASCIITransport();
    } else if (Modbus.SERIAL_ENCODING_RTU.equals(m_Parameters.getEncoding())) {
      m_Transport = new ModbusRTUTransport();
    } else if (Modbus.SERIAL_ENCODING_BIN.equals(m_Parameters.getEncoding())) {
      m_Transport = new ModbusBINTransport();
    }
    m_Transport.setEcho(m_Parameters.isEcho());

    // Open the input and output streams for the connection. If they won't
    // open, close the port before throwing an exception.
    try {
      m_SerialIn = m_SerialPort.getInputStream();
      m_Transport.setCommPort(m_SerialPort);
//       m_Transport.prepareStreams(m_SerialIn,
//                                  m_SerialPort.getOutputStream());
    } catch (IOException e) {
      m_SerialPort.close();
      if(Modbus.debug) System.out.println(e.getMessage());

      throw new Exception("Error opening i/o streams");
    }
    //System.out.println("i/o Streams prepared");

    // Add this object as an event listener for the serial port.
    try {
      m_SerialPort.addEventListener(this);
    } catch (TooManyListenersException e) {
      m_SerialPort.close();
      if(Modbus.debug) System.out.println(e.getMessage());
      throw new Exception("too many listeners added");
    }

    // Set notifyOnBreakInterrup to allow event driven break handling.
    m_SerialPort.notifyOnBreakInterrupt(true);

    // Set receive timeout to allow breaking out of polling loop during
    // input handling.
    try {
      m_SerialPort.enableReceiveTimeout(200);
    } catch (UnsupportedCommOperationException e) {
      if(Modbus.debug) System.out.println(e.getMessage());

    }
    m_Open = true;
  }//open

  /**
   * Sets the connection parameters to the setting in the parameters object.
   * If set fails return the parameters object to origional settings and
   * throw exception.
   *
   * @throws Exception if the configured parameters cannot be set properly
   *         on the port.
   */
  public void setConnectionParameters() throws Exception {

    // Save state of parameters before trying a set.
    int oldBaudRate = m_SerialPort.getBaudRate();
    int oldDatabits = m_SerialPort.getDataBits();
    int oldStopbits = m_SerialPort.getStopBits();
    int oldParity = m_SerialPort.getParity();
    int oldFlowControl = m_SerialPort.getFlowControlMode();

    // Set connection parameters, if set fails return parameters object
    // to original state.
    try {
      m_SerialPort.setSerialPortParams(m_Parameters.getBaudRate(),
          m_Parameters.getDatabits(),
          m_Parameters.getStopbits(),
          m_Parameters.getParity());
    } catch (UnsupportedCommOperationException e) {
      m_Parameters.setBaudRate(oldBaudRate);
      m_Parameters.setDatabits(oldDatabits);
      m_Parameters.setStopbits(oldStopbits);
      m_Parameters.setParity(oldParity);
      if(Modbus.debug) System.out.println(e.getMessage());

      throw new Exception("Unsupported parameter");
    }

    // Set flow control.
    try {
      m_SerialPort.setFlowControlMode(m_Parameters.getFlowControlIn()
          | m_Parameters.getFlowControlOut());
    } catch (UnsupportedCommOperationException e) {
      if(Modbus.debug) System.out.println(e.getMessage());

      throw new Exception("Unsupported flow control");
    }
  }//setConnectionParameters

  /**
   * Close the port and clean up associated elements.
   */
  public void close() {
    // If port is alread closed just return.
    if (!m_Open) {
      return;
    }

    // Check to make sure sPort has reference to avoid a NPE.
    if (m_SerialPort != null) {
      try {
        m_Transport.close();
        m_SerialIn.close();
      } catch (IOException e) {
        System.err.println(e);
      }
      // Close the port.
      m_SerialPort.close();
    }

    m_Open = false;
  }//close

  /**
   * Reports the open status of the port.
   *
   * @return true if port is open, false if port is closed.
   */
  public boolean isOpen() {
    return m_Open;
  }//isOpen

  public void serialEvent(SerialPortEvent e) {
    // Determine type of event.
    switch (e.getEventType()) {
      // This event is ignored, the application reads directly from
      // the serial input stream
      case SerialPortEvent.DATA_AVAILABLE:
        /*        try {
          int amount = m_SerialIn.available();
          while (amount > 0) {
            try {
              byte[] buffer = new byte[amount];
              if ((amount = m_SerialIn.read(buffer, 0, amount)) > 0) {
                m_Pipe.write (buffer, 0, amount);
              }
              amount = m_SerialIn.available();
            } catch (IOException ex) {
              System.err.println("Error: Comm event read: " + ex);
              ex.printStackTrace();
              return;
            }
          }
        } catch (Exception ex) {
          //handle

        }
*/
        break;
      case SerialPortEvent.BI:
        if (Modbus.debug) System.out.println("Serial port break detected");
        break;
      default:
        if (Modbus.debug) System.out.println("Serial port event: " + e.getEventType());
    }
  }//serialEvent


}//class SerialConnection

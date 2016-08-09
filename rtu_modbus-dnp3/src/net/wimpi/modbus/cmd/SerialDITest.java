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

import net.wimpi.modbus.ModbusCoupler;
import net.wimpi.modbus.Modbus;
import net.wimpi.modbus.io.ModbusSerialTransaction;
import net.wimpi.modbus.msg.ReadInputDiscretesRequest;
import net.wimpi.modbus.msg.ReadInputDiscretesResponse;
import net.wimpi.modbus.net.SerialConnection;
import net.wimpi.modbus.util.SerialParameters;
import net.wimpi.modbus.util.BitVector;

/**
 * Class that implements a simple commandline
 * tool for reading an analog input.
 *
 * @author Dieter Wimberger
 * @version 1.2rc1 (09/11/2004)
 */
public class SerialDITest {

  public static void main(String[] args) {

    SerialConnection con = null;
    ModbusSerialTransaction trans = null;
    ReadInputDiscretesRequest req = null;
    ReadInputDiscretesResponse res = null;

    String portname = null;
    int unitid = 0;
    int ref = 0;
    int count = 0;
    int repeat = 1;

    try {

      //1. Setup the parameters
      if (args.length < 4) {
        printUsage();
        System.exit(1);
      } else {
        try {
          portname = args[0];
          unitid = Integer.parseInt(args[1]);
          ref = Integer.parseInt(args[2]);
          count = Integer.parseInt(args[3]);
          if (args.length == 5) {
            repeat = Integer.parseInt(args[4]);
          }
        } catch (Exception ex) {
          ex.printStackTrace();
          printUsage();
          System.exit(1);
        }
      }

      //2. Set slave identifier for master response parsing
      ModbusCoupler.getReference().setUnitID(unitid);

      System.out.println("net.wimpi.modbus.debug set to: " +
                         System.getProperty("net.wimpi.modbus.debug"));

      //3. Setup serial parameters
      SerialParameters params = new SerialParameters();
      params.setPortName(portname);
      params.setBaudRate(115200);
      params.setDatabits(7);
      params.setParity("None");
      params.setStopbits(2);
//       params.setEncoding("rtu");
//       params.setEcho(true);
      if (Modbus.debug) System.out.println("Encoding [" + params.getEncoding() + "]");

      //4. Open the connection
      con = new SerialConnection(params);
      con.open();


      //5. Prepare a request
      req = new ReadInputDiscretesRequest(ref, count);
      req.setUnitID(unitid);
      req.setHeadless();
      if (Modbus.debug) System.out.println("Request: " + req.getHexMessage());

      //6. Prepare the transaction
      trans = new ModbusSerialTransaction(con);
      trans.setRequest(req);

      //7. Execute the transaction repeat times
      int k = 0;
      do {
        trans.execute();

        res = (ReadInputDiscretesResponse) trans.getResponse();
        if (Modbus.debug) System.out.println("Response: " + res.getHexMessage());
        BitVector inputs = res.getDiscretes();
        byte ret[] = new byte[inputs.size()];
        for (int i = 0; i < count; i++) {
          System.out.println("Bit " + i + " = " + inputs.getBit(i));
        }

        k++;
      } while (k < repeat);

      //8. Close the connection
      con.close();

    } catch (Exception ex) {
      ex.printStackTrace();
      // Close the connection
      con.close();
    }
  }//main

  private static void printUsage() {
    System.out.println(
        "java net.wimpi.modbus.cmd.SerialAITest <portname [String]>  <Unit Address [int8]> <register [int16]> <wordcount [int16]> {<repeat [int]>}"
    );
  }//printUsage

}//class SerialAITest

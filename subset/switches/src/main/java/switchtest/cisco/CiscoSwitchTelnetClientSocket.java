package switchtest.cisco;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import switchtest.SwitchInterrogator;
import switchtest.SwitchTelnetClientSocket;

public class CiscoSwitchTelnetClientSocket extends SwitchTelnetClientSocket {
  public CiscoSwitchTelnetClientSocket(
      String remoteIpAddress, int remotePort, SwitchInterrogator interrogator, boolean debug) {
    super(remoteIpAddress, remotePort, interrogator, debug);
  }

  @Override
  public void run() {
    connectTelnetSocket();

    Runnable readDataRunnable =
        () -> {
          readData();
        };
    readerThread = new Thread(readDataRunnable);

    readerThread.start();

    Runnable gatherDataRunnable =
        () -> {
          gatherData();
        };
    gatherThread = new Thread(gatherDataRunnable);

    gatherThread.start();

    outputStream = telnetClient.getOutputStream();
  }

  private void gatherData() {
    StringBuilder rxData = new StringBuilder();
    String rxGathered = "";

    boolean parseFlag = false;

    int count = 0;
    int flush = 0;
    int rxQueueCount = 0;
    int rxTempCount = 0;
    int expectedLength = 1000;
    int requestFlag = 0;

    while (telnetClient.isConnected()) {
      try {

        if (rxQueue.isEmpty()) {
          Thread.sleep(100);
          rxQueueCount++;
          if (debug) {
            System.out.println("rxQueue.isEmpty:" + rxQueueCount);
            System.out.println("expectedLength:" + expectedLength);
            System.out.println("requestFlag:" + requestFlag);
          }
          if (rxQueueCount > 70) {
            rxQueueCount = 0;
            writeData("\n");
          }
          // No data has been received since last scan but the switch
          // is sitting at the command prompt and ready for the next command
          if (interrogator.promptReady) {
            // Send a blank string which will trigger next command
            interrogator.processConsoleMessage("");
          }

        } else {
          rxQueueCount = 0;
          String rxTemp = rxQueue.poll();
          if (rxTemp.equals("")) {
            Thread.sleep(100);
            rxTempCount++;
            if (debug) {
              System.out.println("rxTemp.equals:" + rxTempCount);
            }
          } else if (rxTemp.indexOf("--More--") > 0) {
            Thread.sleep(20);
            writeData("\n");

            if (debug) {
              System.out.println("more position:" + rxTemp.indexOf("--More--"));
              System.out.println("rxTemp.length" + rxTemp.length() + "rxTemp pre:" + rxTemp);
              // Useful for debugging
              // char[] tempChar = rxTemp.toCharArray();
              // for(char temp:tempChar) {
              //        System.out.println("tempChar:"+(byte)temp);
              // }
            }

            rxTemp = rxTemp.substring(0, rxTemp.length() - 9);

            if (debug) {
              System.out.println("rxTemp.length" + rxTemp.length() + "rxTemp post:" + rxTemp);
            }

            rxData.append(rxTemp);
          } else {
            rxQueueCount = 0;
            rxTempCount = 0;
            rxData.append(rxTemp);
            rxGathered = rxData.toString();
            System.out.println(
                java.time.LocalTime.now()
                    + "rxDataLen:"
                    + rxGathered.length()
                    + "rxData:"
                    + rxGathered);
            // Send the current data in the buffer to be processed
            boolean processed = interrogator.processConsoleMessage(rxGathered);
            // If we have processed the current buffers data we will clear the buffer
            if (processed) {
              rxData = new StringBuilder();
            }
          }
        }
      } catch (InterruptedException e) {
        System.err.println("InterruptedException gatherData:" + e.getMessage());
      }
    }
  }
}

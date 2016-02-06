/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hagc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Bruno
 */
public class LanToSerial implements Runnable {

    private static final int BUFFER_SIZE = DataFrame.ENCODED_VALUES_SIZE;

    private final InputStream in;
    private final OutputStream out;

    public LanToSerial(InputStream in, OutputStream out) {
        this.in = in;
        this.out = out;
    }

    private void sendToSerial(DataFrame frame) throws IOException {
        this.out.write(frame.getChannel());
        this.out.write(frame.getDataHigh());
        this.out.write(frame.getDataLow());
    }
    
    @Override
    public void run() {
        int[] buffer = new int[BUFFER_SIZE];
        int i = 0;

        int standby_light = 0;

        try {
            int data;
            while ((data = this.in.read()) > - 1) {

                // We are receiving a new paquet, start buffering new incomming data
                if ((0x0C0 & data) == 0) {
                    i = 0;
                }

                // Buffering new incomming data
                if (i < BUFFER_SIZE) {
                    buffer[i] = data;
                }
                i++;

                // We have received a hole paquet
                if (i == BUFFER_SIZE) {
                    DataFrame frame = new DataFrame(buffer);

                    // If channel is DSKY channel
                    if (frame.getChannel() == 0x08) {

                        // DSKY irrelevant data ?
                        if ((buffer[0] == 0x01)
                                && (buffer[1] == 0x40)
                                && (buffer[2] == 0x80)
                                && (buffer[3] == 0xC0)) {
                            //System.out.println("DSKY irrelevant data 1 40 80 c0");
                        } // DSKY relevant data
                        else {
                            this.sendToSerial(frame);
                            ChannelLogger.getInstance().writeLog(frame);
                        }
                    } else if (frame.getChannel() == 0x9) {
                        this.sendToSerial(frame);
                        //ChannelLogger.getInstance().writeLog(frame);
                    } else if ((frame.getChannel() == 0xB) && (standby_light != (frame.getDataHigh() & 0b00000100))) {
                        standby_light = (frame.getDataHigh() & 0b00000100);

                        this.sendToSerial(frame);
                        //ChannelLogger.getInstance().writeLog(frame);
                    } // Other channels
                    else {
                        //System.out.print("Channel ");
                        //System.out.println(Integer.toString(channel));
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(LanToSerial.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

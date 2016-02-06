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
public class SerialToLan implements Runnable {

    private static final int BUFFER_SIZE = DataFrame.DECODED_VALUES_SIZE;
    
    private final InputStream in;
    private final OutputStream out;
    
    private boolean close = false;

    public SerialToLan(InputStream in, OutputStream out) {
        this.in = in;
        this.out = out;
    }

    private void sendToLan(DataFrame frame) throws IOException {
        for (int value : frame.getEncodedValues()) {
            this.out.write(value);
        }
    }

    @Override
    public void run() {
        int[] buffer = new int[BUFFER_SIZE];
        int i = 0;

        try {
            int data;
            while (!this.close && (data = this.in.read()) > - 1) {

                if (i >= BUFFER_SIZE) {
                    i = 0;
                }

                if (i < BUFFER_SIZE) {
                    buffer[i] = data;
                }
                i++;

                // We have received a hole paquet
                if (i == BUFFER_SIZE) {
                    DataFrame frame = new DataFrame(buffer);

                    // If channel is KEYBOARD channel
                    if (frame.getChannel() == 0x0D) {
                        this.sendToLan(frame);
                        ChannelLogger.getInstance().writeLog(frame);
                    } // Other channels
                    else {
                        //System.out.print("Channel ");
                        //System.out.println(Integer.toString(channel));
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(SerialToLan.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}

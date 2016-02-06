/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hagc;

/**
 *
 * @author Bruno
 */
public class DataFrame {

    public static final int DECODED_VALUES_SIZE = 3;
    public static final int ENCODED_VALUES_SIZE = 4;

    private int decodedValues[];
    private int encodedValues[];

    DataFrame(int data[]) {
        // Decoded values
        if (data.length == DECODED_VALUES_SIZE) {
            this.decodedValues = data;
            this.encodedValues = encode(data);
        }
        // Encoded values
        else if (data.length == ENCODED_VALUES_SIZE) {
            this.decodedValues = decode(data);
            this.encodedValues = data;
        }
        // Wrong data
        else {
            throw new RuntimeException("Wring data");
        }

    }

    public int getChannel() {
        return this.decodedValues[0];
    }

    public int getDataLow() {
        return this.decodedValues[2];
    }

    public int getDataHigh() {
        return this.decodedValues[1];
    }

    public int[] getEncodedValues() {
        return this.encodedValues;
    }

    public int[] getDecodedValues() {
        return this.decodedValues;
    }

    public static int[] encode(int data[]) {
        if (data.length != DECODED_VALUES_SIZE) {
            throw new RuntimeException("Wrong data");
        }
        
        return encode(data[0], data[1], data[2]);
    }
    
    public static int[] encode(int channel, int dataHigh, int dataLow) {
        // ***************************************
        // * yaAGC protocol :                    *
        // * 0        1        2        3        *
        // * 00utCCCC 01CCCHHH 10HHHHLL 11LLLLLL *
        // ***************************************
        int data1, data2, data3, data4;

        // Compute byte #1
        data1 = ((channel >> 3) & 0b00001111);

        // Compute byte #2
        data2 = 0b01000000;
        data2 |= ((channel << 3) & 0b00111000);
        data2 |= ((dataHigh >> 4) & 0b00000111);

        // Compute byte #3
        data3 = 0b10000000;
        data3 |= ((dataHigh << 2) & 0b00111100);
        data3 |= ((dataLow >> 6) & 0b00000011);

        // Compute byte #4
        data4 = 0b11000000;
        data4 |= (dataLow & 0b00111111);

        return new int[]{data1, data2, data3, data4};
    }

    public static int[] decode(int[] data) {
        if ((data.length != ENCODED_VALUES_SIZE)
                || ((data[0] & 0xC0) != 0x00)
                || ((data[1] & 0xC0) != 0x40)
                || ((data[2] & 0xC0) != 0x80)
                || ((data[3] & 0xC0) != 0xC0)) {
            throw new RuntimeException("Wrong data");
        }

        // ***************************************
        // * yaAGC protocol :                    *
        // * 0        1        2        3        *
        // * 00utCCCC 01CCCHHH 10HHHHLL 11LLLLLL *
        // ***************************************/
        int channel, dataHigh, dataLow;

        // Compute channel number
        channel = (data[0] & 0b00001111) << 3;
        channel |= (data[1] & 0b00111000) >> 3;

        // Compute dataHigh
        dataHigh = (data[1] & 0b00000111) << 4;
        dataHigh |= (data[2] & 0b00111100) >> 2;

        // Compute dataLow
        dataLow = (data[2] & 0b00000011) << 6;
        dataLow |= (data[3] & 0b00111111);

        return new int[]{channel, dataHigh, dataLow};
    }

}

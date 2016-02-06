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
public class ChannelLogger {

    private int lineNumber = 0;

    private static ChannelLogger self;

    private ChannelLogger() {
    }

    public static ChannelLogger getInstance() {
        if (ChannelLogger.self == null) {
            ChannelLogger.self = new ChannelLogger();
        }
        return ChannelLogger.self;
    }

    private String generateLog(DataFrame frame) {
        StringBuilder sb = new StringBuilder();

        sb.append("Channel ");
        sb.append(Integer.toOctalString(frame.getChannel()));
        sb.append(" (");
        sb.append(this.lineNumber);
        sb.append("): ");

        sb.append(Integer.toHexString(frame.getChannel()));
        sb.append(" ");
        sb.append(Integer.toHexString(frame.getDataHigh()));
        sb.append(" ");
        sb.append(Integer.toHexString(frame.getDataLow()));

        return sb.toString();
    }

    public void writeLog(DataFrame frame) {
        System.out.println(this.generateLog(frame));
        this.lineNumber++;
    }

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hagc;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

/**
 * FXML Controller class
 *
 * @author Bruno
 */
public class FXMLMainPanelController implements Initializable {

    public static final String TITLE = "Apollo Guidance Computer";
    public static final String FXML_RESOURCE = "FXMLMainPanel.fxml";
    private static final int BITRATE = 9600;

    @FXML
    private Button serialConnectButton;
    @FXML
    private Button serialDisonnectButton;
    @FXML
    private TextField ipAddressTextField;
    @FXML
    private TextField portTextField;
    @FXML
    private Button yaAgcConnectButton;
    @FXML
    private Button yaAgcDisconnectButton;
    @FXML
    private Button refreshButton;
    @FXML
    private TextArea trololo;
    @FXML
    private ComboBox<String> serialComComboBox;
    
    private Socket socket;
    private SerialPort serialPort;
    private boolean serialPortIsConnected = false;
    private boolean socketIsConnected = false;
    
    private SerialToLan serialToLan; 
    private LanToSerial lanToSerial;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        listPorts();

        if (this.serialComComboBox.getItems().size() >= 1) {
            this.serialComComboBox.setValue(this.serialComComboBox.getItems().get(0));
        }
    }

    @FXML
    private void serialConnect(ActionEvent event) {
        try {
            this.serialPort = this.serialOpenConnection(this.serialComComboBox.getValue().split(" ")[0]); 
            this.serialPortIsConnected = true;
            this.updateUIConnectionState();
            
            if (this.socketIsConnected) makeBridge();
            
        } catch (Exception ex) {
            Logger.getLogger(FXMLMainPanelController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    private void serialDisconnect(ActionEvent event) throws IOException {
        this.serialPort.close();
        this.serialPortIsConnected = false;
        this.updateUIConnectionState();
    }

    @FXML
    private void yaAgcConnect(ActionEvent event) {
        try {
            String address = this.ipAddressTextField.getText();
            int port = Integer.parseInt(this.portTextField.getText());
            
            this.socket = new Socket(address, port);
            this.socketIsConnected = true;
            
            this.updateUIConnectionState();
            
            if (this.serialPortIsConnected) makeBridge();

        } catch (IOException ex) {
            Logger.getLogger(FXMLMainPanelController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    private void yaAgcDisconnect(ActionEvent event) {
        try {
            this.socket.close();
            this.socketIsConnected = false;
            this.updateUIConnectionState();
        } catch (IOException ex) {
            Logger.getLogger(FXMLMainPanelController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    void listPorts() {
        java.util.Enumeration<CommPortIdentifier> portEnum = CommPortIdentifier.getPortIdentifiers();

        ObservableList<String> list = FXCollections.observableArrayList();

        while (portEnum.hasMoreElements()) {
            CommPortIdentifier portIdentifier = portEnum.nextElement();

            StringBuilder sb = new StringBuilder();
            sb.append(portIdentifier.getName());
            sb.append(" (");
            sb.append(getPortTypeName(portIdentifier.getPortType()));

            try {
                CommPort commPort = portIdentifier.open("CommUtil", 50);
                commPort.close();

            } catch (PortInUseException e) {
                sb.append(" - busy");

            } catch (Exception e) {
                sb.append(" - error");
            }

            sb.append(")");

            list.add(sb.toString());
        }

        if (list.isEmpty()) {
            this.serialComComboBox.setValue("NONE");
        }
        else {
            this.serialComComboBox.setItems(list);
        } 
    }

    static String getPortTypeName(int portType) {
        switch (portType) {
            case CommPortIdentifier.PORT_I2C:
                return "I2C";
            case CommPortIdentifier.PORT_PARALLEL:
                return "Parallel";
            case CommPortIdentifier.PORT_RAW:
                return "Raw";
            case CommPortIdentifier.PORT_RS485:
                return "RS485";
            case CommPortIdentifier.PORT_SERIAL:
                return "Serial";
            default:
                return "unknown type";
        }
    }

    @FXML
    private void refreshSerialPort(ActionEvent event) {
        listPorts();
    }

    @FXML
    private void autoRefreshSerialPort(Event event) {
        listPorts();
    }
    
    private SerialPort serialOpenConnection(String portName) throws Exception {
        System.out.println("Opening port " + portName);

        CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
        if (portIdentifier.isCurrentlyOwned()) {
            throw new Exception("Error: Port is currently in use.");
        }

        CommPort commPort = portIdentifier.open(this.getClass().getName(), 2000);
        if (!(commPort instanceof SerialPort)) {
            throw new Exception("Error: Only serial ports are handled.");
        }

        SerialPort sp = (SerialPort) commPort;
        sp.setSerialPortParams(BITRATE, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

        sp.enableReceiveThreshold(1);
        if (!sp.isReceiveThresholdEnabled()) {
            throw new Exception("Error: Uncompatible driver. (Treshold)");
        }

        sp.disableReceiveTimeout();
        if (sp.isReceiveTimeoutEnabled()) {
            throw new Exception("Error: Uncompatible driver. (Timeout)");
        }
                
        return sp;
    }
    
    private void updateUIConnectionState() {
        this.serialConnectButton.setDisable(serialPortIsConnected);
        this.serialComComboBox.setDisable(serialPortIsConnected);
        this.serialDisonnectButton.setDisable(!serialPortIsConnected);
  
        this.ipAddressTextField.setDisable(this.socketIsConnected);
        this.portTextField.setDisable(this.socketIsConnected);
        this.yaAgcConnectButton.setDisable(this.socketIsConnected);
        this.yaAgcDisconnectButton.setDisable(!this.socketIsConnected);
    }
    
    private void makeBridge() {
        try {
            this.serialToLan = new SerialToLan(this.serialPort.getInputStream(), this.socket.getOutputStream());
            this.lanToSerial = new LanToSerial(this.socket.getInputStream(), this.serialPort.getOutputStream());
            
            (new Thread(this.serialToLan)).start();
            (new Thread(this.lanToSerial)).start();
            
        } catch (IOException ex) {
            Logger.getLogger(FXMLMainPanelController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}

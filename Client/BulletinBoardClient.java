import java.awt.*;
import java.io.*;
import java.net.Socket;
import javax.swing.*;

public class BulletinBoardClient extends JFrame {
    private JTextField hostField;
    private JTextField portField;
    private JButton connectBtn, disconnectBtn;
    private JTextArea outputArea;
    
    private JTextField postXField, postYField, postColorField, postMsgField;
    private JButton postBtn;
    
    private JTextField getColorField, getContainsXField, getContainsYField, getRefersToField;
    private JButton getAllBtn, getPinsBtn, getFilteredBtn;
    
    private JTextField pinXField, pinYField;
    private JButton pinBtn, unpinBtn;
    
    private JButton shakeBtn, clearBtn;
    
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private boolean connected = false;

    public BulletinBoardClient() {
        setTitle("Bulletin Board Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Connection Panel
        mainPanel.add(createConnectionPanel(), BorderLayout.NORTH);
        
        // Commands Panel
        mainPanel.add(createCommandsPanel(), BorderLayout.CENTER);
        
        // Output Panel
        mainPanel.add(createOutputPanel(), BorderLayout.SOUTH);
        
        add(mainPanel);
        setVisible(true);
    }

    private JPanel createConnectionPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Connection"));
        
        panel.add(new JLabel("Host:"));
        hostField = new JTextField("localhost", 15);
        panel.add(hostField);
        
        panel.add(new JLabel("Port:"));
        portField = new JTextField("4554", 8);
        panel.add(portField);
        
        connectBtn = new JButton("Connect");
        connectBtn.addActionListener(e -> connect());
        panel.add(connectBtn);
        
        disconnectBtn = new JButton("Disconnect");
        disconnectBtn.setEnabled(false);
        disconnectBtn.addActionListener(e -> disconnect());
        panel.add(disconnectBtn);
        
        return panel;
    }

    private JComponent createCommandsPanel() {
        JPanel mainPanel = new JPanel(new GridLayout(4, 1, 5, 5));
        
        mainPanel.add(createPostPanel());
        mainPanel.add(createGetPanel());
        mainPanel.add(createPinPanel());
        mainPanel.add(createControlPanel());
        
        JScrollPane scroll = new JScrollPane(mainPanel);
        scroll.setBorder(BorderFactory.createTitledBorder("Commands"));
        return scroll;
    }

    private JPanel createPostPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        panel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        
        panel.add(new JLabel("POST:"));
        panel.add(new JLabel("X:"));
        postXField = new JTextField(4);
        panel.add(postXField);
        
        panel.add(new JLabel("Y:"));
        postYField = new JTextField(4);
        panel.add(postYField);
        
        panel.add(new JLabel("Color:"));
        postColorField = new JTextField(8);
        panel.add(postColorField);
        
        panel.add(new JLabel("Message:"));
        postMsgField = new JTextField(20);
        panel.add(postMsgField);
        
        postBtn = new JButton("POST");
        postBtn.setEnabled(false);
        postBtn.addActionListener(e -> postNote());
        panel.add(postBtn);
        
        return panel;
    }

    private JPanel createGetPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        topPanel.add(new JLabel("GET Filters:"));
        topPanel.add(new JLabel("Color:"));
        getColorField = new JTextField(8);
        topPanel.add(getColorField);
        
        topPanel.add(new JLabel("Contains X:"));
        getContainsXField = new JTextField(4);
        topPanel.add(getContainsXField);
        
        topPanel.add(new JLabel("Y:"));
        getContainsYField = new JTextField(4);
        topPanel.add(getContainsYField);
        
        topPanel.add(new JLabel("RefersTo:"));
        getRefersToField = new JTextField(10);
        topPanel.add(getRefersToField);
        
        panel.add(topPanel, BorderLayout.NORTH);
        
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        
        getAllBtn = new JButton("Get All Notes");
        getAllBtn.setEnabled(false);
        getAllBtn.addActionListener(e -> getAllNotes());
        btnPanel.add(getAllBtn);
        
        getPinsBtn = new JButton("Get Pins");
        getPinsBtn.setEnabled(false);
        getPinsBtn.addActionListener(e -> getPins());
        btnPanel.add(getPinsBtn);
        
        getFilteredBtn = new JButton("Get Filtered");
        getFilteredBtn.setEnabled(false);
        getFilteredBtn.addActionListener(e -> getFiltered());
        btnPanel.add(getFilteredBtn);
        
        panel.add(btnPanel, BorderLayout.SOUTH);
        
        return panel;
    }

    private JPanel createPinPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        panel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        
        panel.add(new JLabel("PIN/UNPIN:"));
        panel.add(new JLabel("X:"));
        pinXField = new JTextField(4);
        panel.add(pinXField);
        
        panel.add(new JLabel("Y:"));
        pinYField = new JTextField(4);
        panel.add(pinYField);
        
        pinBtn = new JButton("PIN");
        pinBtn.setEnabled(false);
        pinBtn.addActionListener(e -> pinNote());
        panel.add(pinBtn);
        
        unpinBtn = new JButton("UNPIN");
        unpinBtn.setEnabled(false);
        unpinBtn.addActionListener(e -> unpinNote());
        panel.add(unpinBtn);
        
        return panel;
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        panel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        
        shakeBtn = new JButton("SHAKE");
        shakeBtn.setEnabled(false);
        shakeBtn.addActionListener(e -> shake());
        panel.add(shakeBtn);
        
        clearBtn = new JButton("CLEAR");
        clearBtn.setEnabled(false);
        clearBtn.addActionListener(e -> clear());
        panel.add(clearBtn);
        
        return panel;
    }

    private JPanel createOutputPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Output"));
        
        outputArea = new JTextArea(10, 80);
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        JScrollPane scroll = new JScrollPane(outputArea);
        panel.add(scroll, BorderLayout.CENTER);
        
        return panel;
    }

    private void connect() {
        try {
            String host = hostField.getText().trim();
            int port = Integer.parseInt(portField.getText().trim());
            
            if (host.isEmpty()) {
                showError("Host cannot be empty");
                return;
            }
            
            socket = new Socket(host, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            
            String handshake = in.readLine();
            if (handshake != null && handshake.startsWith("WELCOME")) {
                connected = true;
                updateUI();
                appendOutput("Connected: " + handshake);
            } else {
                showError("Invalid handshake from server");
                disconnect();
            }
        } catch (NumberFormatException e) {
            showError("Invalid port number");
        } catch (IOException e) {
            showError("Connection failed: " + e.getMessage());
        }
    }

    private void disconnect() {
        try {
            if (out != null) {
                out.println("DISCONNECT");
                out.flush();
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            connected = false;
            updateUI();
            appendOutput("Disconnected");
        } catch (IOException e) {
            showError("Disconnect error: " + e.getMessage());
        }
    }

    private void postNote() {
        try {
            String x = postXField.getText().trim();
            String y = postYField.getText().trim();
            String color = postColorField.getText().trim();
            String message = postMsgField.getText().trim();
            
            if (x.isEmpty() || y.isEmpty() || color.isEmpty() || message.isEmpty()) {
                showError("All POST fields are required");
                return;
            }
            
            try {
                Integer.parseInt(x);
                Integer.parseInt(y);
            } catch (NumberFormatException e) {
                showError("X and Y must be integers");
                return;
            }
            
            String cmd = "POST " + x + " " + y + " " + color + " " + message;
            sendCommand(cmd);
            
            postXField.setText("");
            postYField.setText("");
            postColorField.setText("");
            postMsgField.setText("");
        } catch (Exception e) {
            showError("POST error: " + e.getMessage());
        }
    }

    private void getAllNotes() {
        sendCommand("GET");
    }

    private void getPins() {
        sendCommand("GET PINS");
    }

    private void getFiltered() {
        try {
            String color = getColorField.getText().trim();
            String containsX = getContainsXField.getText().trim();
            String containsY = getContainsYField.getText().trim();
            String refersTo = getRefersToField.getText().trim();
            
            if (color.isEmpty() && containsX.isEmpty() && containsY.isEmpty() && refersTo.isEmpty()) {
                showError("Specify at least one filter");
                return;
            }
            
            StringBuilder cmd = new StringBuilder("GET");
            if (!color.isEmpty()) {
                cmd.append(" color=").append(color);
            }
            if (!containsX.isEmpty() && !containsY.isEmpty()) {
                try {
                    Integer.parseInt(containsX);
                    Integer.parseInt(containsY);
                    cmd.append(" contains=").append(containsX).append(" ").append(containsY);
                } catch (NumberFormatException e) {
                    showError("Contains coordinates must be integers");
                    return;
                }
            }
            if (!refersTo.isEmpty()) {
                cmd.append(" refersTo=").append(refersTo);
            }
            
            sendCommand(cmd.toString());
        } catch (Exception e) {
            showError("GET error: " + e.getMessage());
        }
    }

    private void pinNote() {
        try {
            String x = pinXField.getText().trim();
            String y = pinYField.getText().trim();
            
            if (x.isEmpty() || y.isEmpty()) {
                showError("X and Y are required");
                return;
            }
            
            try {
                Integer.parseInt(x);
                Integer.parseInt(y);
            } catch (NumberFormatException e) {
                showError("X and Y must be integers");
                return;
            }
            
            sendCommand("PIN " + x + " " + y);
        } catch (Exception e) {
            showError("PIN error: " + e.getMessage());
        }
    }

    private void unpinNote() {
        try {
            String x = pinXField.getText().trim();
            String y = pinYField.getText().trim();
            
            if (x.isEmpty() || y.isEmpty()) {
                showError("X and Y are required");
                return;
            }
            
            try {
                Integer.parseInt(x);
                Integer.parseInt(y);
            } catch (NumberFormatException e) {
                showError("X and Y must be integers");
                return;
            }
            
            sendCommand("UNPIN " + x + " " + y);
        } catch (Exception e) {
            showError("UNPIN error: " + e.getMessage());
        }
    }

    private void shake() {
        sendCommand("SHAKE");
    }

    private void clear() {
        int result = JOptionPane.showConfirmDialog(
            this, 
            "Are you sure you want to clear all notes and pins?", 
            "Confirm CLEAR", 
            JOptionPane.YES_NO_OPTION
        );
        if (result == JOptionPane.YES_OPTION) {
            sendCommand("CLEAR");
        }
    }

    private void sendCommand(String cmd) {
        try {
            if (!connected) {
                showError("Not connected to server");
                return;
            }
            
            appendOutput("\n> " + cmd);
            out.println(cmd);
            out.flush();
            
            String response = in.readLine();
            if (response != null) {
                appendOutput("< " + response);
                
                // Read additional lines for multi-line responses
                if (response.startsWith("OK") && response.contains("\n")) {
                    String line;
                    while ((line = in.readLine()) != null && !line.isEmpty()) {
                        appendOutput(line);
                    }
                }
            }
        } catch (IOException e) {
            showError("Command error: " + e.getMessage());
            connected = false;
            updateUI();
        }
    }

    private void updateUI() {
        hostField.setEnabled(!connected);
        portField.setEnabled(!connected);
        connectBtn.setEnabled(!connected);
        disconnectBtn.setEnabled(connected);
        
        postBtn.setEnabled(connected);
        getAllBtn.setEnabled(connected);
        getPinsBtn.setEnabled(connected);
        getFilteredBtn.setEnabled(connected);
        pinBtn.setEnabled(connected);
        unpinBtn.setEnabled(connected);
        shakeBtn.setEnabled(connected);
        clearBtn.setEnabled(connected);
    }

    private void appendOutput(String text) {
        outputArea.append(text + "\n");
        outputArea.setCaretPosition(outputArea.getDocument().getLength());
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new BulletinBoardClient());
    }
}

package burp;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.KeyEvent;

public class RequestDetailsDialog extends JDialog {
    private PostmanRequest request;
    private PostmanRequest originalRequest;
    
    private JTextField nameField;
    private JTextArea requestTextArea;
    private JTextArea notesArea;
    private JComboBox<String> vulnerabilityComboBox;
    
    private boolean result = false;
    
    public RequestDetailsDialog(Frame parent, PostmanRequest request) {
        super(parent, "✏️ Edit Request - " + request.getName(), true);
        this.request = request;
        this.originalRequest = copyRequest(request); 
        
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        loadRequestData();
        
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(700, 600);
        setLocationRelativeTo(parent);
    }
    
    private PostmanRequest copyRequest(PostmanRequest original) {
        PostmanRequest copy = new PostmanRequest();
        copy.setName(original.getName());
        copy.setMethod(original.getMethod());
        copy.setUrl(original.getUrl());
        copy.setBody(original.getBody());
        copy.setDescription(original.getDescription());
        copy.setNotes(original.getNotes());
        copy.setVulnerabilityStatus(original.getVulnerabilityStatus());
        copy.setFolderPath(original.getFolderPath());
        
        if (original.getHeaders() != null) {
            copy.setHeaders(new java.util.HashMap<>(original.getHeaders()));
        }
        
        if (original.getQueryParameters() != null) {
            copy.setQueryParameters(new java.util.HashMap<>(original.getQueryParameters()));
        }
        
        return copy;
    }
    
    private void initializeComponents() {
        nameField = new JTextField(40);
        nameField.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        
        requestTextArea = new JTextArea(20, 60);
        requestTextArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        requestTextArea.setLineWrap(false);
        requestTextArea.setTabSize(4);
        requestTextArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        
        notesArea = new JTextArea(6, 60);
        notesArea.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        notesArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        
        vulnerabilityComboBox = new JComboBox<>(new String[]{"", "🟢 Safe", "🔴 Vulnerable"});
        vulnerabilityComboBox.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBorder(new EmptyBorder(15, 15, 10, 15));
        
        JPanel namePanel = new JPanel(new BorderLayout(10, 5));
        namePanel.add(new JLabel("📝 Request Name:"), BorderLayout.NORTH);
        namePanel.add(nameField, BorderLayout.CENTER);
        
        JPanel statusPanel = new JPanel(new BorderLayout(10, 5));
        statusPanel.add(new JLabel("🔍 Vulnerability Status:"), BorderLayout.NORTH);
        statusPanel.add(vulnerabilityComboBox, BorderLayout.CENTER);
        
        topPanel.add(namePanel, BorderLayout.CENTER);
        topPanel.add(statusPanel, BorderLayout.EAST);
        
        add(topPanel, BorderLayout.NORTH);
        
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(new EmptyBorder(0, 15, 10, 15));
        
        JScrollPane requestScrollPane = new JScrollPane(requestTextArea);
        requestScrollPane.setBorder(new TitledBorder("🔧 Request Details (Editable)"));
        requestScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        requestScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        
        centerPanel.add(requestScrollPane, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);
        
        JPanel bottomPanel = new JPanel(new BorderLayout(0, 10));
        bottomPanel.setBorder(new EmptyBorder(0, 15, 15, 15));
        
        JScrollPane notesScrollPane = new JScrollPane(notesArea);
        notesScrollPane.setBorder(new TitledBorder("🧪 Testing Notes & Findings"));
        notesScrollPane.setPreferredSize(new Dimension(0, 120));
        
        bottomPanel.add(notesScrollPane, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        
        JButton saveButton = new JButton("💾 Save Changes");
        saveButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        saveButton.setBackground(new Color(40, 167, 69));
        saveButton.setForeground(Color.WHITE);
        saveButton.setFocusPainted(false);
        
        JButton cancelButton = new JButton("❌ Cancel");
        cancelButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        cancelButton.setBackground(new Color(108, 117, 125));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setFocusPainted(false);
        
        saveButton.addActionListener(e -> {
            parseAndSaveChanges();
            result = true;
            dispose();
        });
        
        cancelButton.addActionListener(e -> {
            restoreOriginalData();
            result = false;
            dispose();
        });
        
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);
        
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private void setupEventHandlers() {
        getRootPane().registerKeyboardAction(e -> {
            parseAndSaveChanges();
            result = true;
            dispose();
        }, KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK), JComponent.WHEN_IN_FOCUSED_WINDOW);
        
        getRootPane().registerKeyboardAction(e -> {
            restoreOriginalData();
            result = false;
            dispose();
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }
    
    private void loadRequestData() {
        nameField.setText(request.getName() != null ? request.getName() : "");
        notesArea.setText(request.getNotes() != null ? request.getNotes() : "");
        vulnerabilityComboBox.setSelectedItem(request.getVulnerabilityStatus());
        
        StringBuilder requestText = new StringBuilder();
        
        requestText.append(request.getMethod()).append(" ").append(request.getUrl()).append(" HTTP/1.1\n");
        
        if (request.getHeaders() != null && !request.getHeaders().isEmpty()) {
            for (java.util.Map.Entry<String, String> header : request.getHeaders().entrySet()) {
                requestText.append(header.getKey()).append(": ").append(header.getValue()).append("\n");
            }
        }
        
        requestText.append("\n");
        
        if (request.getBody() != null && !request.getBody().isEmpty()) {
            requestText.append(request.getBody());
        }
        
        requestTextArea.setText(requestText.toString());
        requestTextArea.setCaretPosition(0);
    }
    
    private void parseAndSaveChanges() {
        try {
            request.setName(nameField.getText().trim());
            request.setNotes(notesArea.getText());
            request.setVulnerabilityStatus((String) vulnerabilityComboBox.getSelectedItem());
            
            String requestText = requestTextArea.getText();
            String[] lines = requestText.split("\n");
            
            if (lines.length > 0) {
                String requestLine = lines[0].trim();
                String[] parts = requestLine.split("\\s+");
                
                if (parts.length >= 2) {
                    request.setMethod(parts[0].toUpperCase());
                    request.setUrl(parts[1]);
                }
                
                java.util.Map<String, String> headers = new java.util.HashMap<>();
                StringBuilder body = new StringBuilder();
                boolean inBody = false;
                
                for (int i = 1; i < lines.length; i++) {
                    String line = lines[i];
                    
                    if (line.trim().isEmpty() && !inBody) {
                        inBody = true; 
                        continue;
                    }
                    
                    if (!inBody && line.contains(":")) {
                        int colonIndex = line.indexOf(":");
                        String headerName = line.substring(0, colonIndex).trim();
                        String headerValue = line.substring(colonIndex + 1).trim();
                        headers.put(headerName, headerValue);
                    } else if (inBody) {
                        if (body.length() > 0) {
                            body.append("\n");
                        }
                        body.append(line);
                    }
                }
                
                request.setHeaders(headers);
                request.setBody(body.toString());
            }
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "Error parsing request: " + ex.getMessage() + 
                "\n\nPlease check the request format:\n" +
                "METHOD URL HTTP/1.1\n" +
                "Header-Name: Header-Value\n" +
                "\n" +
                "Request Body",
                "Parse Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void restoreOriginalData() {
        request.setName(originalRequest.getName());
        request.setUrl(originalRequest.getUrl());
        request.setMethod(originalRequest.getMethod());
        request.setBody(originalRequest.getBody());
        request.setNotes(originalRequest.getNotes());
        request.setDescription(originalRequest.getDescription());
        request.setVulnerabilityStatus(originalRequest.getVulnerabilityStatus());
        request.setHeaders(originalRequest.getHeaders() != null ? new java.util.HashMap<>(originalRequest.getHeaders()) : new java.util.HashMap<>());
        request.setQueryParameters(originalRequest.getQueryParameters() != null ? new java.util.HashMap<>(originalRequest.getQueryParameters()) : new java.util.HashMap<>());
    }
    
    public boolean showDialog() {
        setVisible(true);
        return result;
    }
}

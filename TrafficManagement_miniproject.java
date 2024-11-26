package traffic;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

// Route Class to define routes between points
class Route {
    String from;
    String to;
    double distance; // Representing distance (could represent congestion or time)
    double congestionLevel; // Congestion level for traffic

    public Route(String from, String to, double distance, double congestionLevel) {
        this.from = from;
        this.to = to;
        this.distance = distance;
        this.congestionLevel = congestionLevel;
    }
}

// Graph to represent the system of routes
class Graph {
    Map<String, ArrayList<Route>> adjList = new HashMap<>();

    // Add route to the graph
    public void addRoute(String from, String to, double distance, double congestionLevel) {
        adjList.putIfAbsent(from, new ArrayList<>());
        adjList.putIfAbsent(to, new ArrayList<>());
        adjList.get(from).add(new Route(from, to, distance, congestionLevel));
        adjList.get(to).add(new Route(to, from, distance, congestionLevel)); // Assuming bidirectional roads
    }

    // Dijkstra's Algorithm for finding the shortest path
    public String findShortestPath(String source, String destination) {
        Map<String, Double> distances = new HashMap<>();
        Map<String, String> previous = new HashMap<>();
        Set<String> visited = new HashSet<>();
        for (String node : adjList.keySet()) {
            distances.put(node, Double.MAX_VALUE);
            previous.put(node, null);
        }
        distances.put(source, 0.0);
        PriorityQueue<String> pq = new PriorityQueue<>(Comparator.comparingDouble(distances::get));
        pq.add(source);
        while (!pq.isEmpty()) {
            String current = pq.poll();
            visited.add(current);
            if (current.equals(destination)) {
                ArrayList<String> path = new ArrayList<>();
                for (String at = destination; at != null; at = previous.get(at)) {
                    path.add(at);
                }
                Collections.reverse(path);
                return "Shortest path from " + source + " to " + destination + ": " + path + ", Distance = " + distances.get(destination);
            }
            for (Route route : adjList.get(current)) {
                if (!visited.contains(route.to)) {
                    double newDist = distances.get(current) + route.distance;
                    if (newDist < distances.get(route.to)) {
                        distances.put(route.to, newDist);
                        previous.put(route.to, current);
                        pq.add(route.to);
                    }
                }
            }
        }
        return "No path available from " + source + " to " + destination;
    }
}

// Vehicle Class to represent vehicles with priority
class Vehicle {
    String id;
    String lane;
    String type; // e.g., "Ambulance", "Police", "Car"

    public Vehicle(String id, String lane, String type) {
        this.id = id;
        this.lane = lane;
        this.type = type;
    }
}

// Traffic Management System Class
public class TrafficManagementSystem {
    private Graph graph = new Graph();
    private List<Vehicle> vehicles = new ArrayList<>(); // Store vehicles with priority

    public void addRoute(String from, String to, double distance, double congestionLevel) {
        graph.addRoute(from, to, distance, congestionLevel);
    }

    public String findShortestPath(String from, String to) {
        return graph.findShortestPath(from, to);
    }

    public void addVehicle(String vehicleId, String lane, String type) {
        vehicles.add(new Vehicle(vehicleId, lane, type));
    }

    public String manageTraffic() {
        // Prioritize vehicles based on type: High priority first
        List<String> priorityTypes = Arrays.asList("Ambulance", "Police", "Car", "Truck");
        StringBuilder vehiclesLeaving = new StringBuilder("Vehicles leaving based on priority:\n");

        // Sort vehicles based on type priority
        vehicles.sort(Comparator.comparingInt(vehicle -> priorityTypes.indexOf(vehicle.type)));

        for (Vehicle vehicle : vehicles) {
            vehiclesLeaving.append("Vehicle ID: ").append(vehicle.id)
                    .append(" from ").append(vehicle.lane)
                    .append(" lane (Type: ").append(vehicle.type).append(")\n");
        }
        return vehiclesLeaving.toString();
    }

    public static void main(String[] args) {
        new MainMenu(new TrafficManagementSystem());
    }
}

// Main Menu Class
class MainMenu extends JFrame {
    private TrafficManagementSystem trafficSystem;

    public MainMenu(TrafficManagementSystem system) {
        this.trafficSystem = system;
        setTitle("Traffic Management System");
 setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(87, 143, 168));
        JButton adminButton = createButton("Admin");
        adminButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new AdminGUI(trafficSystem, MainMenu.this);
                setVisible(false);
            }
        });
        panel.add(adminButton);
        JButton userButton = createButton("User ");
        userButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new UserGUI(trafficSystem, MainMenu.this);
                setVisible(false);
            }
        });
        panel.add(userButton);
        add(panel, BorderLayout.CENTER);
        setVisible(true);
    }

    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(200, 50));
        button.setFont(new Font("Arial", Font.PLAIN, 18));
        button.setBackground(new Color(50, 150, 255));
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        return button;
    }
}

// Admin GUI Class
class AdminGUI extends JFrame {
    private TrafficManagementSystem trafficSystem;
    private JTextField vehicleIdField;
    private JComboBox<String> laneComboBox;
    private JComboBox<String> vehicleTypeComboBox; // New combo box for vehicle types
    private JTextField fromField, toField, distanceField, congestionField;
    private JFrame mainMenuFrame;

    public AdminGUI(TrafficManagementSystem system, JFrame mainMenu) {
        this.trafficSystem = system;
        this.mainMenuFrame = mainMenu;
        setTitle("Traffic Management System - Admin");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(8, 2, 10, 10));
        panel.setBackground(new Color(87, 143, 168));

        // Lane selection
        JLabel laneLabel = new JLabel("Select Lane:");
        laneComboBox = new JComboBox<>(new String[]{"North", "South", "East", "West"});
        addField(panel, laneLabel, laneComboBox);

        // Vehicle ID input
        JLabel vehicleIdLabel = new JLabel("Vehicle ID:");
        vehicleIdField = new JTextField();
        addField(panel, vehicleIdLabel, vehicleIdField);

        // Vehicle type selection
        JLabel vehicleTypeLabel = new JLabel("Select Vehicle Type:");
        vehicleTypeComboBox = new JComboBox<>(new String[]{"Car", "Truck", "Ambulance", "Police"});
        addField(panel, vehicleTypeLabel, vehicleTypeComboBox);

        // Other fields (from, to, distance, congestion) remain unchanged
        JLabel fromLabel = new JLabel("From Point:");
        fromField = new JTextField();
        addField(panel, fromLabel, fromField);
        JLabel toLabel = new JLabel("To Point:");
        toField = new JTextField();
        addField(panel, toLabel, toField);
        JLabel distanceLabel = new JLabel("Distance (km):");
        distanceField = new JTextField();
        addField(panel, distanceLabel, distanceField);
        JLabel congestionLabel = new JLabel("Congestion Level (0-10):");
        congestionField = new JTextField();
        addField(panel, congestionLabel, congestionField);

        // Add buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());
        buttonPanel.setBackground(new Color(87, 143, 168));
        JButton addRouteButton = new JButton("Add Route");
        addRouteButton.setBackground(new Color(50, 150, 255));
        addRouteButton.setForeground(Color.BLACK);
        addRouteButton.setFont(new Font("Arial", Font.PLAIN, 16));
        addRouteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addRoute();
            }
        });
        JButton addVehicleButton = new JButton("Add Vehicle");
        addVehicleButton.setBackground(new Color(50, 150, 255));
        addVehicleButton.setForeground(Color.BLACK);
        addVehicleButton.setFont(new Font("Arial", Font.PLAIN, 16));
        addVehicleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addVehicle();
            }
        });
        buttonPanel.add(addVehicleButton);
        button ```java
        buttonPanel.add(addRouteButton);
        JButton manageButton = new JButton("Manage Traffic");
        manageButton.setBackground(new Color(50, 150, 255));
        manageButton.setForeground(Color.BLACK);
        manageButton.setFont(new Font("Arial", Font.PLAIN, 16));
        manageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String result = trafficSystem.manageTraffic();
                JOptionPane.showMessageDialog(AdminGUI.this, result);
            }
        });
        buttonPanel.add(manageButton);
        JButton backButton = new JButton("Back");
        backButton.setBackground(new Color(200, 0, 0));
        backButton.setForeground(Color.BLACK);
        backButton.setFont(new Font("Arial", Font.PLAIN, 16));
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainMenuFrame.setVisible(true);
                setVisible(false);
            }
        });
        buttonPanel.add(backButton);
        add(panel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        setVisible(true);
    }

    private void addField(JPanel panel, JLabel label, JComponent field) {
        panel.add(label);
        panel.add(field);
    }

    private void addRoute() {
        try {
            String from = fromField.getText();
            String to = toField.getText();
            double distance = Double.parseDouble(distanceField.getText());
            double congestionLevel = Double.parseDouble(congestionField.getText());
            trafficSystem.addRoute(from, to, distance, congestionLevel);
            JOptionPane.showMessageDialog(this, "Route added successfully!");
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid input for distance or congestion level.");
        }
    }

    private void addVehicle() {
        String vehicleId = vehicleIdField.getText();
        String lane = (String) laneComboBox.getSelectedItem();
        String type = (String) vehicleTypeComboBox.getSelectedItem(); // Get the selected vehicle type
        trafficSystem.addVehicle(vehicleId, lane, type);
        JOptionPane.showMessageDialog(this, "Vehicle added successfully!");
    }
}

// User GUI Class
class UserGUI extends JFrame {
    private TrafficManagementSystem trafficSystem;
    private JTextField fromField, toField;
    private JFrame mainMenuFrame;

    public UserGUI(TrafficManagementSystem system, JFrame mainMenu) {
        this.trafficSystem = system;
        this.mainMenuFrame = mainMenu;
        setTitle("Traffic Management System - User");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(4, 2));
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(4, 2, 10, 10));
        panel.setBackground(new Color(87, 143, 168));
        JLabel fromLabel = new JLabel("From Point:");
        fromField = new JTextField();
        panel.add(fromLabel);
        panel.add(fromField);
        JLabel toLabel = new JLabel("To Point:");
        toField = new JTextField();
        panel.add(toLabel);
        panel.add(toField);
        JButton getPathButton = new JButton("Get Shortest Path");
        getPathButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showShortestPath();
            }
        });
        panel.add(getPathButton);
        JButton backButton = new JButton("Back");
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainMenuFrame.setVisible(true); // Show main menu
                dispose(); // Close the user GUI
            }
        });
        panel.add(backButton);
        add(panel);
        setVisible(true);
    }

    private void showShortestPath() {
        String from = fromField.getText().trim();
        String to = toField.getText().trim();
        if (from.isEmpty() || to.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both from and to points.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String result = trafficSystem.findShortestPath(from, to);
        JOptionPane.showMessageDialog(this, result);
    }
}


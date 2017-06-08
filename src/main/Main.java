package main;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import javax.swing.JOptionPane;

import rmi.RMIServer;

public class Main {

	public static RMIServer server;
	public static Registry registry;
	
	public static void main(String[] args) {
		
		try {
			System.setProperty("java.rmi.server.hostname", "81.108.95.40");
			server = new RMIServer();
			registry = LocateRegistry.createRegistry(1100);
			registry.bind("RMIServer", server);
			System.err.println("Server ready");
			
			JOptionPane.showMessageDialog(null, "Fine");
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Error");
			e.printStackTrace();
		}
		
	}

}

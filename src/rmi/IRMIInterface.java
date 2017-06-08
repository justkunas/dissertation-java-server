package rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IRMIInterface extends Remote {
	
	public String search(String query) throws RemoteException;
	
	public String nextPage() throws RemoteException;
	public String previousPage() throws RemoteException;
	public String loadPages(String query) throws RemoteException;
	
}

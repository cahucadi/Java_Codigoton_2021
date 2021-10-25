package org.cahucadi.reto.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.cahucadi.reto.domain.Client;
import org.cahucadi.reto.repository.AccountRepository;
import org.cahucadi.reto.repository.ClientRepository;
import org.cahucadi.reto.util.FileReaderUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EvalartService {

	@Autowired
	ClientRepository clientRepository;
	@Autowired
	AccountRepository accountRepository;

	List<Long> clientsAsignedToTable;

	public EvalartService(ClientRepository clientRepository, AccountRepository accountRepository) {
		super();
		this.clientRepository = clientRepository;
		this.accountRepository = accountRepository;
	}

	
	/** Main method
	 * @throws Exception
	 */
	public void execute() throws Exception {
		
		// Init the client asigned to any table
		clientsAsignedToTable = new ArrayList<>();

		// 1. FileReaderUtil returns a TreeMap with table names as key and a HashMap for filters
		TreeMap<String, HashMap<String, String>> entries = FileReaderUtil.loadFileContent("entrada.txt");

		String fileContent="";
		
		for (String table : entries.keySet()) {
			HashMap<String, String> filters = entries.get(table);
			
			// 2. Find Clients by filter
			Queue<Client> result = filterAccountTotalByClient(filters);

			if (result == null) {
				System.out.println(table + "\nCANCELADA");
				fileContent += table + "\nCANCELADA\n";
				continue;
			}

			// 3. Apply constrains of gender, companies on table and total balance
			List<Client> optimazedList = orderClientsOnTable(result);

			if (optimazedList.size() < 4) {
				System.out.println(table + "\nCANCELADA");
				fileContent += table + "\nCANCELADA\n";
				continue;
			}
			
			String outputList = optimazedList.stream().map(x -> x.getDecodedCode()).collect(Collectors.joining(","));
			System.out.println(table+"\n"+ outputList);
			fileContent += table+"\n"+ outputList+"\n";

			// For debuging porposes
			//debugCollection(result, "Table " + table + " results");
			//debugCollection(optimazedList, "Table " + table + " optimized");

			result.clear();
			filters = new HashMap<String, String>();

		}
		
		// Wrife salida.txt on project root
		FileReaderUtil.writeFile("salida.txt", fileContent);

	}

	
	/**
	 * @param params HashMap with filters (e.g. "RI":"10000" or "UG":"40")
	 * @return Ordered queue of Clients
	 */
	private Queue<Client> filterAccountTotalByClient(Map<String, String> params) {

		// Apply filters to clients
		HashMap<Long, Client> clients = filterClients(params);
		
		Object[] clientsArr = clients.keySet().toArray();

		Double min = null;
		Double max = null;

		// Check if there are account filters
		if (params.containsKey("RI"))
			min = Double.parseDouble(params.get("RI"));

		if (params.containsKey("RF"))
			max = Double.parseDouble(params.get("RF"));

		
		// Query only accounts for filtered clients
		List<Object[]> accounts = accountRepository.getTotalBalanceGroupByFilter(clientsArr, min, max);

		// With a PriorityQueue for ordering Clients objects
		Queue<Client> queue = new PriorityQueue<Client>();

		int maleCount = 0;

		for (Object[] account : accounts) {
			
			// Update client with calculated totalBalance
			Client c = clients.get(account[0]); 
			c.setTotalBalance(Double.parseDouble(account[1].toString()));

			if (c.getMale() == 1)
				maleCount++;

			queue.add(c);
		}

		// Return null if there is not enough clients or by gender capacity
		if (queue.size() < 4 || (queue.size() - maleCount) < 2 || maleCount < 2)
			queue = null;

		return queue;

	}

	
	/**
	 * @param params HashMap with filters (e.g. "RI":"10000" or "UG":"40")
	 * @return HashMap with ClientId on Key and Client on Value
	 */
	private HashMap<Long, Client> filterClients(Map<String, String> params) {

		Integer type = null;
		String location = null;

		// Check if there are client filters
		if (params.containsKey("TC"))
			type = Integer.parseInt(params.get("TC"));

		if (params.containsKey("UG"))
			location = params.get("UG").toString();

		// Query clients by filter
		List<Client> clients = clientRepository.findClientByFilters(type, location);

		// Transform Client List to Map<Long, Client>
		Map<Long, Client> clientMap = clients.stream().collect(Collectors.toMap(Client::getId, Function.identity()));

		return new HashMap<Long, Client>(clientMap);

	}

	
	/** This is the method for organize tables 
	 * 
	 * @param clientList List of all filtered Clients with calculated account values 
	 * @return ordered Client list with constrains applied
	 * 
	 */
	private List<Client> orderClientsOnTable(Queue<Client> clientList) {

		// List of companies with a client on this table
		List<String> companiesOnTable = new ArrayList<String>();

		List<Client> iterateClientList = new ArrayList<>(clientList);
		List<Client> currentTable = new LinkedList<>();

		int maleCount = 0;

		for (int i = 0; i < iterateClientList.size(); i++) {

			Client client = iterateClientList.get(i);

			// add to current table list ONLY clients if there is no other client from the same company on the table
			// add to current table list ONLY clients if there is not already asigned on a table before
			if (!companiesOnTable.contains(client.getCompany()) && !clientsAsignedToTable.contains(client.getId())) {
				maleCount += client.getMale();
				companiesOnTable.add(client.getCompany());
				clientsAsignedToTable.add(client.getId());
				currentTable.add(client);

				if (currentTable.size() == 8)
					break;
			}
		}

		int females = currentTable.size() - maleCount;

		// Revert current table, for removing only the last elements (with less total balance)
		Collections.reverse(currentTable);

		while (females != maleCount) {

			Predicate<Client> gender;
			Predicate<Client> oppositeGender;
			int genderValue = 0;

			if (females < maleCount) {
				gender = x -> x.getMale() == 1;
				genderValue = 1;
				oppositeGender = x -> x.getMale() == 0;
				maleCount--;
			} else {
				gender = x -> x.getMale() == 0;
				genderValue = 0;
				oppositeGender = x -> x.getMale() == 1;
				females--;
			}

			Client remove = currentTable.stream().filter(gender).findFirst().orElse(null);

			// Remove from current table if there is more male/female
			if (remove != null) {
				currentTable.remove(remove);
				companiesOnTable.remove(remove.getCompany());
				clientsAsignedToTable.remove(remove.getId());
			}
			
			// If table is balanced then break
            if (females == maleCount)
                break;

			Predicate<Client> notInCompaniesOnTableList = x -> !companiesOnTable.contains(x.getCompany());
			Predicate<Client> notInclientsAssignedToTableList = x -> !clientsAsignedToTable.contains(x.getId());

			Client add = iterateClientList.stream().filter(oppositeGender).filter(notInCompaniesOnTableList)
					.filter(notInclientsAssignedToTableList).findFirst().orElse(null);

			// Add to current table an opposite gender client using filter to select the client with less total balance
			if (add != null) {

				if (genderValue == 1) {
					females++;
				} else {
					maleCount++;
				}

				companiesOnTable.add(add.getCompany());
				clientsAsignedToTable.add(add.getId());
				currentTable.add(add);
			}

		}

		// Sort current table using Comparable
	    Collections.sort(currentTable);
		return currentTable;
		
	}

	
	/**
	 * Method for collection debugging
	 * @param collection Collection to test
	 * @param message Custom message
	 */
	@SuppressWarnings("unused")
	private void debugCollection(Collection<Client> collection, String message) {

		System.out.println(message);

		for (Iterator<Client> iterator = collection.iterator(); iterator.hasNext();) {
			Client c = (Client) iterator.next();
			System.out.println(c.getDecodedCode() + " - TC: " + c.getType() + " - " + "UG: " + c.getLocation() + " - "
					+ "EM: " + c.getCompany() + " - " + "Sexo: " + c.getMale() + " - " + "MONTO: "
					+ c.getTotalBalance());
		}

		System.out.println("########################\n");

	}


}

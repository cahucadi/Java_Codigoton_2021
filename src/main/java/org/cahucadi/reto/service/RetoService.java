package org.cahucadi.reto.service;

import java.util.ArrayDeque;
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
import java.util.Stack;
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
public class RetoService {

	@Autowired
	ClientRepository clientRepository;
	@Autowired
	AccountRepository accountRepository;

	List<Long> clientsAssignedToTable;

	public RetoService(ClientRepository clientRepository, AccountRepository accountRepository) {
		super();
		this.clientRepository = clientRepository;
		this.accountRepository = accountRepository;
	}

	public void execute() throws Exception {

		TreeMap<String, HashMap<String, String>> entries = FileReaderUtil.loadFileContent("entrada.txt");
		clientsAssignedToTable = new ArrayList<Long>();

		for (String table : entries.keySet()) {
			HashMap<String, String> filters = entries.get(table);
			
			Queue<Client> result = filterAccountTotalByClient(filters);

			if (result == null) {
				System.out.println(table + "\nCANCELADA");
				continue;
			}

			List<Client> optimazedList = orderClientsOnTable(result);

			if (optimazedList.size() < 4) {
				System.out.println(table + "\nCANCELADA");
				continue;
			}
			
			String outputList = optimazedList.stream().map(x -> x.getDecodedCode()).collect(Collectors.joining(","));
			System.out.println(table+"\n"+ outputList);


			//debugCollection(result, "Table " + table + " results");
			//debugCollection(optimazedList, "Table " + table + " optimized");

			result.clear();
			filters = new HashMap<String, String>();

		}

	}

	private Queue<Client> filterAccountTotalByClient(Map<String, String> params) {

		HashMap<Long, Client> clients = filterClients(params);
		Object[] arr = clients.keySet().toArray();

		Double min = null;
		Double max = null;

		if (params.containsKey("RI"))
			min = Double.parseDouble(params.get("RI"));

		if (params.containsKey("RF"))
			max = Double.parseDouble(params.get("RF"));

		List<Object[]> accounts = accountRepository.getTotalBalanceGroupByFilter(arr, min, max);

		Queue<Client> queue = new PriorityQueue<Client>();

		int maleCount = 0;

		for (Object[] account : accounts) {
			Client c = clients.get(account[0]);
			c.setTotalBalance(Double.parseDouble(account[1].toString()));

			if (c.getMale() == 1)
				maleCount++;

			queue.add(c);
		}

		if (queue.size() < 4 || (queue.size() - maleCount) < 2 || maleCount < 2)
			queue = null;

		return queue;

	}

	private HashMap<Long, Client> filterClients(Map<String, String> params) {

		Integer type = null;
		String location = null;

		if (params.containsKey("TC"))
			type = Integer.parseInt(params.get("TC"));

		if (params.containsKey("UG"))
			location = params.get("UG").toString();

		List<Client> clients = clientRepository.findClientByFilters(type, location);

		Map<Long, Client> clientMap = clients.stream().collect(Collectors.toMap(Client::getId, Function.identity()));

		return new HashMap<Long, Client>(clientMap);

	}

	
	private List<Client> orderClientsOnTable(Queue<Client> clientList) {

		List<String> companiesOnTable = new ArrayList<String>();

		List<Client> iterateClientList = new ArrayList<>(clientList);
		List<Client> currentTable = new LinkedList<>();

		int maleCount = 0;

		for (int i = 0; i < iterateClientList.size(); i++) {

			Client client = iterateClientList.get(i);

			if (!companiesOnTable.contains(client.getCompany()) && !clientsAssignedToTable.contains(client.getId())) {
				maleCount += client.getMale();
				companiesOnTable.add(client.getCompany());
				clientsAssignedToTable.add(client.getId());
				currentTable.add(client);

				if (currentTable.size() == 8)
					break;
			}
		}

		int females = currentTable.size() - maleCount;

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

			if (remove != null) {
				currentTable.remove(remove);
				companiesOnTable.remove(remove.getCompany());
				clientsAssignedToTable.remove(remove.getId());
			}
			
            if (females == maleCount)
                break;

			Predicate<Client> notInCompaniesOnTableList = x -> !companiesOnTable.contains(x.getCompany());
			Predicate<Client> notInclientsAssignedToTableList = x -> !clientsAssignedToTable.contains(x.getId());

			Client add = iterateClientList.stream().filter(oppositeGender).filter(notInCompaniesOnTableList)
					.filter(notInclientsAssignedToTableList).findFirst().orElse(null);

			if (add != null) {

				if (genderValue == 1) {
					females++;
				} else {
					maleCount++;
				}

				companiesOnTable.add(add.getCompany());
				clientsAssignedToTable.add(add.getId());
				currentTable.add(add);
			}

		}

	    Collections.sort(currentTable);
		return currentTable;
		
	}

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

	public void generateOutput() {

	}

}

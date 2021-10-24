package org.cahucadi.reto.service;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.cahucadi.reto.domain.Client;
import org.cahucadi.reto.repository.AccountRepository;
import org.cahucadi.reto.repository.ClientRepository;
import org.cahucadi.reto.util.FileReaderUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RetoService {

	@Autowired ClientRepository clientRepository;
	@Autowired AccountRepository accountRepository;

	public RetoService(ClientRepository clientRepository, AccountRepository accountRepository) {
		super();
		this.clientRepository = clientRepository;
		this.accountRepository = accountRepository;
	}

	public void execute() throws Exception {
		
		HashMap<String, HashMap<String, String>> entries = FileReaderUtil.loadFileContent("entrada.txt");
		
		
		for( Map.Entry<String, HashMap<String,String>> entry : entries.entrySet() ) {
			
			String table = entry.getKey();
			HashMap<String,String> filters = new HashMap<>(entry.getValue());
			
			Queue<Client> result = filterAccountTotalByClient(filters);
			
			if(result == null) {
				System.out.println(table+"\nCANCELADA");
				continue;
			}
				
			
			System.out.println(table);
			
			for (Iterator iterator = result.iterator(); iterator.hasNext();) {
				Client c = (Client) iterator.next();
				System.out.println(c.getCode()+" - TC: "+c.getType()+" - "
						+ "UG: "+c.getLocation()+" - "
								+ "EM: "+c.getCompany()+" - "
										+ "Sexo: "+c.getMale()+" - "
											+ "MONTO: "+c.getTotalBalance());
				
			}
			
			result.clear();
			filters = new HashMap<String, String>();
			System.out.println("##############");
			
		}
		
		
	}
	
	public Queue<Client> filterAccountTotalByClient(Map<String, String> params) {

		HashMap<Long, Client> clients = this.filterClients(params);
		Object[] arr = clients.keySet().toArray();
		
		Double min = null;
		Double max = null;
		
		if(params.containsKey("RI"))
			min = Double.parseDouble(params.get("RI"));

		if(params.containsKey("RF"))
			max = Double.parseDouble(params.get("RF"));
		
		List<Object[]> accounts = accountRepository.getTotalBalanceGroupByFilter(arr, min, max);

		Queue<Client> queue = new ArrayDeque<>();
		
		int maleCount = 0;
		
		for (Object[] account : accounts) {
			Client c = clients.get(account[0]);
			c.setTotalBalance(Double.parseDouble(account[1].toString()));
			
			if(c.getMale()==1)
				maleCount++;
			
			queue.add(c);
		}
		
		if(queue.size()<4 || (queue.size() - maleCount) < 2 || maleCount < 2)
			queue = null;

		return queue;
		
	}
	

	public HashMap<Long, Client> filterClients(Map<String, String> params) {

		Integer type = null;
		String location = null;
		
		if(params.containsKey("TC"))
			type = Integer.parseInt(params.get("TC")) ;
			
		if(params.containsKey("UG")) 
			location = params.get("UG").toString();			
		
		List<Client> clients = clientRepository.findClientByFilters(type, location);

		Map<Long, Client> clientMap = clients.stream()
				.collect(Collectors.toMap(Client::getId, Function.identity()));

		return new HashMap<Long, Client>(clientMap);
		
	}
	

	public void generateOutput() {

	}

}

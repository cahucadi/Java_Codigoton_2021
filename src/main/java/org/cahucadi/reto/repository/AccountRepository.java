package org.cahucadi.reto.repository;

import java.util.List;

import org.cahucadi.reto.domain.Account;
import org.cahucadi.reto.domain.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AccountRepository extends JpaRepository<Account,Long>{


	@Query("SELECT a.clientId, SUM(a.balance) as balance "
			+ "FROM Account a "
			+ "WHERE a.clientId IN (:list) "
			+ "AND (:min is null or a.balance >= :min) "
			+ "AND (:max is null or a.balance <= :max) "
			+ "GROUP BY a.clientId "
			+ "ORDER BY a.balance DESC, clientId ASC")
	List<Object[]> getTotalBalanceGroupByFilter(@Param("list") Object[] list, 
												@Param("min") Double min,
												@Param("max") Double max);
	
}


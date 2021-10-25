package org.cahucadi.reto.repository;

import java.util.List;

import org.cahucadi.reto.domain.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.query.QueryByExampleExecutor;

public interface ClientRepository extends JpaRepository<Client,Long>, QueryByExampleExecutor<Client> {

	/**
	 * @param type user type
	 * @param location user location
	 * @return list of filtered users
	 */
	@Query("SELECT c FROM Client c "
			+ "WHERE (:type is null or c.type = :type) "
			+ "AND (:location is null or c.location = :location)")
			List<Client> findClientByFilters(@Param("type") Integer type, @Param("location") String location);
	
}

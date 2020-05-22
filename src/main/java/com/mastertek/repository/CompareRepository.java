package com.mastertek.repository;

import com.mastertek.domain.Compare;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;


/**
 * Spring Data JPA repository for the Compare entity.
 */
@SuppressWarnings("unused")
@Repository
public interface CompareRepository extends JpaRepository<Compare, Long> {

}

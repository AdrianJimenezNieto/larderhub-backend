package com.larderhub.infrastructure.adapter.out.persistence.householdMember;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HouseholdMemberJpaRepository extends JpaRepository<HouseholdMemberEntity, Long> {
  
}

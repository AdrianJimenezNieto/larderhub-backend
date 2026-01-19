package com.larderhub.infrastructure.adapter.out.persistence.householdMember;

import org.springframework.stereotype.Repository;

import com.larderhub.domain.model.HouseholdMember;
import com.larderhub.domain.ports.out.householdMembers.HouseholdMembersPersistencePort;

@Repository
public class HouseholdPersistenceAdapter implements HouseholdMembersPersistencePort {

  private final HouseholdMemberJpaRepository householdMemberJpaRepository;
  private final HouseholdPersistenceMapper householdPersistenceMapper;

  public HouseholdPersistenceAdapter(HouseholdMemberJpaRepository householdMemberJpaRepository,
      HouseholdPersistenceMapper householdPersistenceMapper) {
    this.householdMemberJpaRepository = householdMemberJpaRepository;
    this.householdPersistenceMapper = householdPersistenceMapper;
  }

  @Override
  public HouseholdMember save(HouseholdMember householdMember) {
    HouseholdMemberEntity householdMemberEntity = householdPersistenceMapper.toEntity(householdMember);
    return householdPersistenceMapper.toDomain(householdMemberJpaRepository.save(householdMemberEntity));
  }
}

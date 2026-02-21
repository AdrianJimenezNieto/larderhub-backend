package com.larderhub.infrastructure.adapter.out.persistence.householdMember;

import org.springframework.stereotype.Repository;

import com.larderhub.domain.model.HouseholdMember;
import com.larderhub.domain.ports.out.householdMembers.HouseholdMembersPersistencePort;

import java.util.Optional;

@Repository
public class HouseholdMemberPersistenceAdapter implements HouseholdMembersPersistencePort {

  private final HouseholdMemberJpaRepository householdMemberJpaRepository;
  private final HouseholdMemberPersistenceMapper householdMemberPersistenceMapper;

  public HouseholdMemberPersistenceAdapter(HouseholdMemberJpaRepository householdMemberJpaRepository,
      HouseholdMemberPersistenceMapper householdMemberPersistenceMapper) {
    this.householdMemberJpaRepository = householdMemberJpaRepository;
    this.householdMemberPersistenceMapper = householdMemberPersistenceMapper;
  }

  @Override
  public HouseholdMember save(HouseholdMember householdMember) {
    HouseholdMemberEntity householdMemberEntity = householdMemberPersistenceMapper.toEntity(householdMember);
    return householdMemberPersistenceMapper.toDomain(householdMemberJpaRepository.save(householdMemberEntity));
  }

  @Override
  public Optional<HouseholdMember> findByUserId(Long userId) {
    return householdMemberJpaRepository.findByUser_Id(userId)
        .map(householdMemberPersistenceMapper::toDomain);
  }
}

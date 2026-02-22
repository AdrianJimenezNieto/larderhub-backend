package com.larderhub.infrastructure.adapter.out.persistence.householdMember;

import org.springframework.stereotype.Repository;

import com.larderhub.domain.model.HouseholdMember;
import com.larderhub.domain.ports.out.householdMembers.HouseholdMembersPersistencePort;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    HouseholdMemberEntity entity = householdMemberPersistenceMapper.toEntity(householdMember);
    return householdMemberPersistenceMapper.toDomain(householdMemberJpaRepository.save(entity));
  }

  @Override
  public Optional<HouseholdMember> findByUserId(Long userId) {
    // Returns the first membership found (used by InventoryService)
    return householdMemberJpaRepository.findByUser_Id(userId)
        .map(householdMemberPersistenceMapper::toDomain);
  }

  @Override
  public List<HouseholdMember> findAllByUserId(Long userId) {
    // Returns ALL memberships for a user (used by HouseholdService)
    return householdMemberJpaRepository.findAllByUser_Id(userId).stream()
        .map(householdMemberPersistenceMapper::toDomain)
        .collect(Collectors.toList());
  }

  @Override
  public List<HouseholdMember> findByHouseholdId(Long householdId) {
    return householdMemberJpaRepository.findByHousehold_Id(householdId).stream()
        .map(householdMemberPersistenceMapper::toDomain)
        .collect(Collectors.toList());
  }

  @Override
  public boolean existsByUserIdAndHouseholdId(Long userId, Long householdId) {
    return householdMemberJpaRepository.existsByUser_IdAndHousehold_Id(userId, householdId);
  }
}

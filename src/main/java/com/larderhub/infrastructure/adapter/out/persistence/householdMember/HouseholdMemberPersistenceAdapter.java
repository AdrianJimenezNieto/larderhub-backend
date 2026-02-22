package com.larderhub.infrastructure.adapter.out.persistence.householdMember;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.larderhub.domain.model.HouseholdMember;
import com.larderhub.domain.ports.out.householdMembers.HouseholdMembersPersistencePort;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
// @Transactional keeps the session open so MapStruct can resolve
// lazy-loaded user.id and household.id when mapping back to domain
@Transactional
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
  @Transactional(readOnly = true)
  public Optional<HouseholdMember> findByUserId(Long userId) {
    // Returns the first membership found (used by InventoryService)
    return householdMemberJpaRepository.findByUser_Id(userId)
        .map(householdMemberPersistenceMapper::toDomain);
  }

  @Override
  @Transactional(readOnly = true)
  public List<HouseholdMember> findAllByUserId(Long userId) {
    // Returns ALL memberships for a user (used by HouseholdService)
    return householdMemberJpaRepository.findAllByUser_Id(userId).stream()
        .map(householdMemberPersistenceMapper::toDomain)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public List<HouseholdMember> findByHouseholdId(Long householdId) {
    return householdMemberJpaRepository.findByHousehold_Id(householdId).stream()
        .map(householdMemberPersistenceMapper::toDomain)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public boolean existsByUserIdAndHouseholdId(Long userId, Long householdId) {
    return householdMemberJpaRepository.existsByUser_IdAndHousehold_Id(userId, householdId);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<HouseholdMember> findByUserIdAndHouseholdId(Long userId, Long householdId) {
    return householdMemberJpaRepository.findByUser_IdAndHousehold_Id(userId, householdId)
        .map(householdMemberPersistenceMapper::toDomain);
  }

  @Override
  public void deleteByUserIdAndHouseholdId(Long userId, Long householdId) {
    // Find the entity first so we can delete by its primary key
    householdMemberJpaRepository.findByUser_IdAndHousehold_Id(userId, householdId)
        .ifPresent(entity -> householdMemberJpaRepository.deleteById(entity.getId()));
  }
}

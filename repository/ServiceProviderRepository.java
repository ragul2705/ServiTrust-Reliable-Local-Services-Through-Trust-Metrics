package com.servitrust.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.servitrust.model.ServiceProvider;

public interface ServiceProviderRepository extends JpaRepository<ServiceProvider, Long> {
    List<ServiceProvider> findByServiceType(String serviceType);
    List<ServiceProvider> findByLocation(String location);
    List<ServiceProvider> findByServiceTypeIgnoreCaseOrderByTrustScoreDesc(String serviceType);
    Optional<ServiceProvider> findByNameAndServiceTypeAndLocation(String name, String serviceType, String location);
    Optional<ServiceProvider> findByAuth_Id(Long authid);

}
package com.agaoglu.integration.integrationtestexample.repository;

import com.agaoglu.integration.integrationtestexample.entity.Deneme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DenemeRepository extends JpaRepository<Deneme, String> {
}

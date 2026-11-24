package com.logicalparadigm.helpdesk.repository;

import com.logicalparadigm.helpdesk.domain.Category;
import com.logicalparadigm.helpdesk.domain.RoutingRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoutingRuleRepository extends JpaRepository<RoutingRule, Long> {

    Optional<RoutingRule> findByCategory(Category category);
}

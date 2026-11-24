package com.logicalparadigm.helpdesk.repository;

import com.logicalparadigm.helpdesk.domain.AppUser;
import com.logicalparadigm.helpdesk.domain.Category;
import com.logicalparadigm.helpdesk.domain.Ticket;
import com.logicalparadigm.helpdesk.domain.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findByRequesterOrderByCreatedAtDesc(AppUser requester);

    long countByStatus(TicketStatus status);

    long countByCategoryAndStatusIn(Category category, List<TicketStatus> statuses);

    /** Admin search. Any filter left null is ignored. */
    @Query("""
            select t from Ticket t
            where (:category is null or t.category = :category)
              and (:status is null or t.status = :status)
              and (:q is null
                   or lower(t.subject) like lower(concat('%', :q, '%'))
                   or lower(t.requester.fullName) like lower(concat('%', :q, '%'))
                   or lower(t.requester.email) like lower(concat('%', :q, '%')))
            order by t.createdAt desc
            """)
    List<Ticket> search(@Param("category") Category category,
                        @Param("status") TicketStatus status,
                        @Param("q") String q);
}

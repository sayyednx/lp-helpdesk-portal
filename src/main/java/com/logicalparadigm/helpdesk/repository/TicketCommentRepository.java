package com.logicalparadigm.helpdesk.repository;

import com.logicalparadigm.helpdesk.domain.TicketComment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketCommentRepository extends JpaRepository<TicketComment, Long> {
}

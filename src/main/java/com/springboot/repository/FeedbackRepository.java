package com.springboot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.springboot.entity.Feedback;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
}
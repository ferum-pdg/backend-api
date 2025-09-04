package org.heigvd.training_generator.interfaces;

import org.heigvd.dto.training_plan_dto.TrainingPlanRequestDto;
import org.heigvd.entity.Account;
import org.heigvd.entity.training_plan.TrainingPlan;

/**
 * Interface for training plan generation implementations.
 * This interface defines the contract for generating complete training plans
 * based on user requirements and account information.
 *
 * Implementations should provide comprehensive training plan generation including:
 * - Goal-based planning
 * - Schedule optimization
 * - Phase-based progression
 * - Constraint handling (available days, fitness level, etc.)
 *
 * @version 1.0
 */
public interface TrainingPlanGenerator {

    /**
     * Generates a complete training plan based on user requirements.
     * This method should create a comprehensive training plan that considers
     * all user constraints, goals, and preferences while applying sound
     * training principles.
     *
     * @param request the training plan request containing user requirements
     * @param account the user account with personal information and fitness data
     * @return a complete training plan with weekly schedules and progression
     * @throws IllegalArgumentException if the request contains invalid parameters
     *                                 or constraints that cannot be satisfied
     */
    TrainingPlan generate(TrainingPlanRequestDto request, Account account);

    /**
     * Returns the version identifier for this implementation.
     * This allows the system to track which implementation was used
     * for generating specific training plans.
     *
     * @return the version identifier (e.g., "V1", "V2")
     */
    String getVersion();
}

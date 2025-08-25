package org.heigvd;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.heigvd.dto.TrainingPlanRequestDto;
import org.heigvd.entity.Account;
import org.heigvd.entity.FitnessLevel;
import org.heigvd.entity.Goal;
import org.heigvd.entity.Sport;
import org.heigvd.entity.TrainingPlan.TrainingPlan;
import org.heigvd.service.GoalService;
import org.heigvd.service.TrainingPlanService;
import org.heigvd.training_generator.TrainingGeneratorV1;
import org.heigvd.training_generator.generatorV1.TrainingPlanGeneratorV1;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@QuarkusTest
public class TrainingPlanGeneratorV1Test {

    @Inject
    TrainingPlanGeneratorV1 tpGen;

    @Inject
    TrainingGeneratorV1 tgV1;

    @Inject
    TrainingPlanService tpService;

    @Inject
    GoalService goalService;

    @Test
    void testDateCalculations() {

    }
}

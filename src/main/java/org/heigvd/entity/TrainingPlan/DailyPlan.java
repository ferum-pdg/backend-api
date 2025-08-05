package org.heigvd.entity.TrainingPlan;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import org.heigvd.entity.Sport;

import java.time.DayOfWeek;
import java.util.UUID;

@Entity
public class DailyPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private DayOfWeek dayOfWeek;

    private Sport sport;

    // CONSTRUCTORS ---------------------------------------------

    public DailyPlan() { }

    public DailyPlan(DayOfWeek dayOfWeek, Sport sport) {
        this.dayOfWeek = dayOfWeek;
        this.sport = sport;
    }

    // METHODS ---------------------------------------------

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public DayOfWeek getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(DayOfWeek dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public Sport getSport() { return sport; }
    public void setSport(Sport sport) { this.sport = sport; }

    @Override
    public String toString() {
        return "\n { \n" +
                "  dayOfWeek=" + dayOfWeek + "\n" +
                "  sport=" + sport + "\n" +
                " }";
    }
}

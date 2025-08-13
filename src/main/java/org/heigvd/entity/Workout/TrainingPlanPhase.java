package org.heigvd.entity.Workout;

public enum TrainingPlanPhase {
    BASE("Phase de base", "Développer l'endurance fondamentale et la force aérobie."),
    SPECIFIC("Phase spécifique", "Améliorer la capacité à maintenir un effort soutenu et développer la puissance aérobie."),
    SHARPENING("Phase d'affûtage", "Optimiser la performance en intensifiant les efforts et en réduisant le volume.");

    private final String label;
    private final String description;
    private TrainingPlanPhase(String label, String description) {
        this.label = label;
        this.description = description;
    }

    public String getLabel() { return label; }
    public String getDescription() { return description; }
}

package org.heigvd.entity.training_plan;

public enum TrainingPlanPhase {
    BASE("Phase de base",
            "Développer l'endurance fondamentale et la force aérobie.",
            0.5),

    SPECIFIC("Phase spécifique",
            "Améliorer la capacité à maintenir un effort soutenu et développer la puissance aérobie.",
            0.3),

    SHARPENING("Phase d'affûtage",
            "Optimiser la performance en intensifiant les efforts et en réduisant le volume.",
            0.2);

    private final String label;
    private final String description;
    private final double percentage;

    private TrainingPlanPhase(String label, String description, double percentage) {
        this.label = label;
        this.description = description;
        this.percentage = percentage;
    }

    public String getLabel() { return label; }
    public String getDescription() { return description; }
    public double getPercentage() { return percentage; }

    /**
     * Calcule le nombre de semaines pour cette phase,
     * en fonction du total disponible.
     */
    public int computeWeeks(int totalWeeks) {
        return (int) Math.round(totalWeeks * percentage);
    }
}

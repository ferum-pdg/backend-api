package org.heigvd.entity.workout;

public enum IntensityZone {
    RECOVERY(1, "Récupération", 0.5, 0.59),
    ENDURANCE(2, "Endurance", 0.6, 0.69),
    TEMPO(2, "Tempo", 0.7, 0.79),
    THRESHOLD(4, "Seuil", 0.8, 0.89),
    VO2_MAX(5, "VO2 Max", 0.9, 0.94),
    ANAEROBIC(6,"Anaérobie", 0.95, 1.0);

    private final int zone;
    private final String label;
    private final double minHr;
    private final double maxHr;

    IntensityZone(int zone, String label, double minHr, double maxHr) {
        this.zone = zone;
        this.label = label;
        this.minHr = minHr;
        this.maxHr = maxHr;
    }

    public String getLabel() {
        return label;
    }

    public int getZone() {
        return zone;
    }

    public double getMinHr() {
        return minHr;
    }

    public double getMaxHr() {
        return maxHr;
    }
}

package org.heigvd.entity.Workout;

public enum IntensityZone {
    RECOVERY(1, "Récupération", 50, 59),
    ENDURANCE(2, "Endurance", 60, 69),
    TEMPO(2, "Tempo", 70, 79),
    THRESHOLD(4, "Seuil", 80, 89),
    VO2_MAX(5, "VO2 Max", 90, 94),
    ANAEROBIC(6,"Anaérobie", 95, 100);

    private final int zone;
    private final String label;
    private final int minHr;
    private final int maxHr;

    IntensityZone(int zone, String label, int minHr, int maxHr) {
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

    public int getMinHr() {
        return minHr;
    }

    public int getMaxHr() {
        return maxHr;
    }
}

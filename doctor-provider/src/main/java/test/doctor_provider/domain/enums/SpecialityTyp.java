package test.doctor_provider.domain.enums;

import lombok.Getter;

@Getter
public enum SpecialityTyp {
    Allgemeinmedizin ("allgemeinmedizin"), // Hausarzt
    InnereMedizin ("inneremedizin"), // Internist
    Kardiologie ("kardiologe"), // Herzspezialist
    Dermatologie ("dermatologe"), // Hautarzt
    Orthopädie ("orthopäde"), // Knochenspezialist
    Neurologie ("neurologe"), // Nervenarzt
    Psychiatrie ("psychiater"),// Psychiater
    Gynäkologie ("gynäkologe"), // Frauenarzt
    Pädiatrie ("pädiater"),// Kinderarzt
    Urologie ("urologe"),// Harnwegsspezialist
    Augenheilkunde ("augenarzt"), // Augenarzt
    HNO ("hno"),// Hals-Nasen-Ohrenarzt
    Radiologie ("radiologe"),// Bildgebender Diagnostiker
    Anästhesiologie ("anästhesist"),// Narkosearzt
    Zahnmedizin ("zahnarzt");// Zahnarzt

    private final String value;

    SpecialityTyp(String value) {
        this.value = value;
    }

    public static SpecialityTyp fromValue(String value) {
        for (SpecialityTyp typ : SpecialityTyp.values()) {
            if (typ.value.equals(value)) {
                return typ;
            }
        }
        throw new IllegalArgumentException(
                "Invalid Speciality value: " + value
        );
    }
}

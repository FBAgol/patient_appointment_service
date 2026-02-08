package test.doctor_provider.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Domain-Modell für paginierte Antworten.
 * Unabhängig von API-DTOs (Hexagonale Architektur).
 *
 * @param <T> Der Typ der Elemente in der Page
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Page<T> {
    /**
     * Liste der Elemente auf dieser Seite
     */
    private List<T> items;

    /**
     * Aktuelle Seitennummer (0-basiert)
     */
    private int page;

    /**
     * Anzahl der Elemente pro Seite
     */
    private int size;

    /**
     * Gesamtanzahl aller Elemente (über alle Seiten)
     */
    private long totalElements;

    /**
     * Gesamtanzahl der Seiten
     */
    private int totalPages;
}


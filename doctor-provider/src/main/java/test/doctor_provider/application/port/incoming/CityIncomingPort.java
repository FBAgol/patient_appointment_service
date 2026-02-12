package test.doctor_provider.application.port.incoming;

import java.util.Optional;

import test.doctor_provider.domain.model.City;
import test.doctor_provider.domain.model.Page;

public interface CityIncomingPort {

	/**
	 * Alle Städte abrufen mit optionalen Filtern und Paginierung.
	 *
	 * @param name
	 *            Optional: Filter nach Stadt-Name
	 * @param postalCode
	 *            Optional: Filter nach Postleitzahl
	 * @param page
	 *            Seitennummer (0-basiert, Pflicht)
	 * @param size
	 *            Anzahl Elemente pro Seite (Pflicht)
	 * @return Paginierte Liste von Cities
	 */
	Page<City> getAllCities(Optional<String> name, Optional<String> postalCode, int page, int size);
}

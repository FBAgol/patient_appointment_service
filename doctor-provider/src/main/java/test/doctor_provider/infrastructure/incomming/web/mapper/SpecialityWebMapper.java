package test.doctor_provider.infrastructure.incomming.web.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.ValueMapping;

import test.doctor_provider.api.model.SpecialityDto;
import test.doctor_provider.api.model.SpecialityType;
import test.doctor_provider.domain.enums.SpecialityTyp;
import test.doctor_provider.domain.model.Speciality;

@Mapper(componentModel = "spring")
public interface SpecialityWebMapper {

	/**
	 * Konvertiert Speciality Domain zu SpecialityDto (Einzelnes Objekt)
	 *
	 * Datenfluss: Server → Client (Response)
	 *
	 * WICHTIG: - Das Feld "name" hat unterschiedliche Enum-Typen: Domain:
	 * SpecialityTyp (z.B. Allgemeinmedizin, Kardiologie) API: SpecialityType (z.B.
	 * ALLGEMEINMEDIZIN, KARDIOLOGE) - Die @ValueMapping Annotationen mappen
	 * zwischen den verschiedenen Konstanten-Namen
	 */
	SpecialityDto toDto(Speciality speciality);

	/**
	 * Konvertiert eine Liste von Speciality Domain zu List<SpecialityDto>
	 *
	 * Datenfluss: Server → Client (Response für GET /api/v1/internal/specialities + /api/v1/external/specialities)
	 *
	 * WICHTIG: - MapStruct ruft automatisch toDto(Speciality) für jedes Element auf
	 * - Laut API: Response ist ein Array (keine Paginierung!)
	 */
	List<SpecialityDto> toDto(List<Speciality> specialities);

	// ─── Enum-Mapping: Domain SpecialityTyp → API SpecialityType ─────────
	@ValueMapping(source = "Allgemeinmedizin", target = "ALLGEMEINMEDIZIN")
	@ValueMapping(source = "InnereMedizin", target = "INNEREMEDIZIN")
	@ValueMapping(source = "Kardiologie", target = "KARDIOLOGE")
	@ValueMapping(source = "Dermatologie", target = "DERMATOLOGE")
	@ValueMapping(source = "Orthopädie", target = "ORTHOP_DE")
	@ValueMapping(source = "Neurologie", target = "NEUROLOGE")
	@ValueMapping(source = "Psychiatrie", target = "PSYCHIATER")
	@ValueMapping(source = "Gynäkologie", target = "GYN_KOLOGE")
	@ValueMapping(source = "Pädiatrie", target = "P_DIATER")
	@ValueMapping(source = "Urologie", target = "UROLOGE")
	@ValueMapping(source = "Augenheilkunde", target = "AUGENARZT")
	@ValueMapping(source = "HNO", target = "HNO")
	@ValueMapping(source = "Radiologie", target = "RADIOLOGE")
	@ValueMapping(source = "Anästhesiologie", target = "AN_STHESIST")
	@ValueMapping(source = "Zahnmedizin", target = "ZAHNARZT")
	SpecialityType toApiEnum(SpecialityTyp typ);

	// ─── Enum-Mapping: API SpecialityType → Domain SpecialityTyp ─────────
	@ValueMapping(source = "ALLGEMEINMEDIZIN", target = "Allgemeinmedizin")
	@ValueMapping(source = "INNEREMEDIZIN", target = "InnereMedizin")
	@ValueMapping(source = "KARDIOLOGE", target = "Kardiologie")
	@ValueMapping(source = "DERMATOLOGE", target = "Dermatologie")
	@ValueMapping(source = "ORTHOP_DE", target = "Orthopädie")
	@ValueMapping(source = "NEUROLOGE", target = "Neurologie")
	@ValueMapping(source = "PSYCHIATER", target = "Psychiatrie")
	@ValueMapping(source = "GYN_KOLOGE", target = "Gynäkologie")
	@ValueMapping(source = "P_DIATER", target = "Pädiatrie")
	@ValueMapping(source = "UROLOGE", target = "Urologie")
	@ValueMapping(source = "AUGENARZT", target = "Augenheilkunde")
	@ValueMapping(source = "HNO", target = "HNO")
	@ValueMapping(source = "RADIOLOGE", target = "Radiologie")
	@ValueMapping(source = "AN_STHESIST", target = "Anästhesiologie")
	@ValueMapping(source = "ZAHNARZT", target = "Zahnmedizin")
	SpecialityTyp toDomainEnum(SpecialityType type);
}

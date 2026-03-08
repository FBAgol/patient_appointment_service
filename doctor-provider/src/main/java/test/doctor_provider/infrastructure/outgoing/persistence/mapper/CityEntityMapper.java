package test.doctor_provider.infrastructure.outgoing.persistence.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import test.doctor_provider.domain.model.City;
import test.doctor_provider.infrastructure.outgoing.persistence.entity.CityEntity;

@Mapper(componentModel = "spring")
public interface CityEntityMapper {

	CityEntity toEntity(City city);

	List<CityEntity> toEntity(List<City> cities);

	City toDomain(CityEntity cityEntity);

	List<City> toDomain(List<CityEntity> cities);
}

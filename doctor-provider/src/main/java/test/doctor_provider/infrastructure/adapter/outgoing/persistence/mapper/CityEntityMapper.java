package test.doctor_provider.infrastructure.adapter.outgoing.persistence.mapper;

import org.mapstruct.Mapper;

import test.doctor_provider.domain.model.City;
import test.doctor_provider.infrastructure.adapter.outgoing.persistence.entity.CityEntity;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CityEntityMapper {

  CityEntity toEntity(City city);

  List<CityEntity> toEntity(List<City> cities);

  City toDomain(CityEntity cityEntity);

  List<City> toDomain(List<CityEntity> cities);
}

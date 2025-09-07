package com.thefreelancer.microservices.gig.mapper;

import com.thefreelancer.microservices.gig.dto.GigPackageCreateDto;
import com.thefreelancer.microservices.gig.dto.GigPackageResponseDto;
import com.thefreelancer.microservices.gig.dto.GigPackageUpdateDto;
import com.thefreelancer.microservices.gig.model.GigPackage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.math.BigDecimal;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface GigPackageMapper {
    
    @Mapping(target = "tier", source = "tier", qualifiedByName = "tierToString")
    @Mapping(target = "priceCents", source = "priceCents", qualifiedByName = "longToDouble")
    GigPackageResponseDto toResponseDto(GigPackage gigPackage);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "gigId", ignore = true)
    @Mapping(target = "priceCents", source = "priceCents", qualifiedByName = "bigDecimalToLong")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    GigPackage toEntity(GigPackageCreateDto createDto);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "gigId", ignore = true)
    @Mapping(target = "tier", ignore = true)
    @Mapping(target = "priceCents", source = "priceCents", qualifiedByName = "bigDecimalToLong")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(GigPackageUpdateDto updateDto, @MappingTarget GigPackage gigPackage);
    
    @Named("tierToString")
    default String tierToString(GigPackage.Tier tier) {
        return tier != null ? tier.toString() : null;
    }
    
    @Named("longToDouble")
    default Double longToDouble(Long value) {
        return value != null ? value.doubleValue() : null;
    }
    
    @Named("bigDecimalToLong")
    default Long bigDecimalToLong(BigDecimal bigDecimal) {
        return bigDecimal != null ? bigDecimal.longValue() : null;
    }
}

package com.thefreelancer.microservices.gig.mapper;

import com.thefreelancer.microservices.gig.dto.BadgeCreateDto;
import com.thefreelancer.microservices.gig.dto.BadgeResponseDto;
import com.thefreelancer.microservices.gig.model.ProfileBadge;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.math.BigDecimal;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface BadgeMapper {
    
    @Mapping(target = "score", source = "score", qualifiedByName = "bigDecimalToDouble")
    BadgeResponseDto toResponseDto(ProfileBadge badge);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "score", source = "score", qualifiedByName = "doubleToBigDecimal")
    @Mapping(target = "issuedAt", ignore = true)
    @Mapping(target = "expiresAt", ignore = true)
    ProfileBadge toEntity(BadgeCreateDto createDto);
    
    @org.mapstruct.Named("doubleToBigDecimal")
    default BigDecimal doubleToBigDecimal(Double value) {
        return value != null ? BigDecimal.valueOf(value) : null;
    }
    
    @org.mapstruct.Named("bigDecimalToDouble")
    default Double bigDecimalToDouble(BigDecimal value) {
        return value != null ? value.doubleValue() : null;
    }
}

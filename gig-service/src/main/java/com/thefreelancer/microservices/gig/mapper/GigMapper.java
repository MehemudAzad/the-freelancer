package com.thefreelancer.microservices.gig.mapper;

import com.thefreelancer.microservices.gig.dto.GigCreateDto;
import com.thefreelancer.microservices.gig.dto.GigResponseDto;
import com.thefreelancer.microservices.gig.model.Gig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.math.BigDecimal;
import java.util.List;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface GigMapper {
    
    @Mapping(target = "status", source = "status", qualifiedByName = "statusToString")
    @Mapping(target = "tags", source = "tags", qualifiedByName = "arrayToList")
    @Mapping(target = "reviewAvg", source = "reviewAvg", qualifiedByName = "bigDecimalToDouble")
    GigResponseDto toResponseDto(Gig gig);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "profileId", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "tags", source = "tags", qualifiedByName = "listToArray")
    @Mapping(target = "reviewAvg", ignore = true)
    @Mapping(target = "reviewsCount", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Gig toEntity(GigCreateDto createDto);
    
    @Named("statusToString")
    default String statusToString(Gig.Status status) {
        return status != null ? status.toString() : null;
    }
    
    @Named("arrayToList")
    default List<String> arrayToList(String[] array) {
        return array != null ? List.of(array) : null;
    }
    
    @Named("listToArray")
    default String[] listToArray(List<String> list) {
        return list != null ? list.toArray(new String[0]) : null;
    }
    
    @Named("bigDecimalToDouble")
    default Double bigDecimalToDouble(BigDecimal bigDecimal) {
        return bigDecimal != null ? bigDecimal.doubleValue() : null;
    }
}

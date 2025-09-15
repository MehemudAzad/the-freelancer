package com.thefreelancer.microservices.gig.mapper;

import com.thefreelancer.microservices.gig.dto.ProfileResponseDto;
import com.thefreelancer.microservices.gig.dto.ProfileUpdateDto;
import com.thefreelancer.microservices.gig.model.Profile;
import org.mapstruct.*;

import java.math.BigDecimal;
import java.util.List;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface ProfileMapper {
    
    @Mapping(target = "availability", source = "availability", qualifiedByName = "availabilityToString")
    @Mapping(target = "languages", source = "languages", qualifiedByName = "arrayToList")
    @Mapping(target = "skills", source = "skills", qualifiedByName = "arrayToList")
    @Mapping(target = "deliveryScore", source = "deliveryScore", qualifiedByName = "bigDecimalToDouble")
    @Mapping(target = "reviewAvg", source = "reviewAvg", qualifiedByName = "bigDecimalToDouble")
    @Mapping(target = "profilePictureUrl", source = "profilePictureUrl")
    ProfileResponseDto toResponseDto(Profile profile);
    
    @Mapping(target = "availability", source = "availability", qualifiedByName = "stringToAvailability")
    @Mapping(target = "languages", source = "languages", qualifiedByName = "listToArray")
    @Mapping(target = "skills", source = "skills", qualifiedByName = "listToArray")
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "deliveryScore", ignore = true)
    @Mapping(target = "reviewAvg", ignore = true)
    @Mapping(target = "reviewsCount", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateProfileFromDto(ProfileUpdateDto updateDto, @MappingTarget Profile profile);
    
    @Named("availabilityToString")
    default String availabilityToString(Profile.Availability availability) {
        return availability != null ? availability.toString() : null;
    }
    
    @Named("stringToAvailability")
    default Profile.Availability stringToAvailability(String availability) {
        if (availability == null || availability.trim().isEmpty()) {
            return null;
        }
        try {
            return Profile.Availability.valueOf(availability.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null; // Invalid value, ignore
        }
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

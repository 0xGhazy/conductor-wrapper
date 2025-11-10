package com.vodafone.vcs.conductorwrapper.action.database.adapter;

import com.vodafone.vcs.conductorwrapper.action.database.dto.QueryStoreDto;
import com.vodafone.vcs.conductorwrapper.action.database.entity.QueryStore;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QueryStoreAdapter {

    private final ModelMapper modelMapper;

    public QueryStore toEntity(QueryStoreDto dto) {
        return modelMapper.map(dto, QueryStore.class);
    }

    public QueryStoreDto toDto(QueryStore entity) {
        return modelMapper.map(entity, QueryStoreDto.class);
    }
}

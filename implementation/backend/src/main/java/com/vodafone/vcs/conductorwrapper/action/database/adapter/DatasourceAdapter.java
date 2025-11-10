package com.vodafone.vcs.conductorwrapper.action.database.adapter;

import com.vodafone.vcs.conductorwrapper.action.database.dto.DatasourceDto;
import com.vodafone.vcs.conductorwrapper.action.database.entity.DataSourceDef;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DatasourceAdapter {

    private final ModelMapper modelMapper;

    public DataSourceDef toEntity(DatasourceDto dto) {
        return modelMapper.map(dto, DataSourceDef.class);
    }

    public DatasourceDto toDto(DataSourceDef entity) {
        return modelMapper.map(entity, DatasourceDto.class);
    }

}

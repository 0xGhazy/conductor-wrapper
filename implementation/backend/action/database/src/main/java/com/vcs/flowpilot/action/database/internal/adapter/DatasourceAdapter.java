package com.vcs.flowpilot.action.database.internal.adapter;

import com.vcs.flowpilot.action.database.internal.dto.DatasourceDto;
import com.vcs.flowpilot.action.database.internal.dto.ResponseDatasourceDto;
import com.vcs.flowpilot.action.database.internal.entity.DataSourceDef;
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

    public ResponseDatasourceDto toResponseDto(DataSourceDef entity) {
        return modelMapper.map(entity, ResponseDatasourceDto.class);
    }

}

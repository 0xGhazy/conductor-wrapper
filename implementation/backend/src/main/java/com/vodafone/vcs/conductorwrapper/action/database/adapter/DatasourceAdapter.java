package com.vodafone.vcs.conductorwrapper.action.database.adapter;

import com.vodafone.vcs.conductorwrapper.action.database.dto.DatasourceDto;
import com.vodafone.vcs.conductorwrapper.action.database.dto.DatasourceDtoResponse;
import com.vodafone.vcs.conductorwrapper.action.database.entity.DataSourceDef;
import com.vodafone.vcs.conductorwrapper.action.database.enums.DatasourceStatus;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import java.util.concurrent.ConcurrentMap;

@Component
@RequiredArgsConstructor
public class DatasourceAdapter {

    private final ModelMapper modelMapper;

    public DataSourceDef toEntity(DatasourceDto dto) {
        return modelMapper.map(dto, DataSourceDef.class);
    }

    public DatasourceDtoResponse toDtoResponse(DataSourceDef entity, ConcurrentMap<String, NamedParameterJdbcTemplate> templates) {
        return new DatasourceDtoResponse(
                entity.getName(),
                templates.get(entity.getName()) != null ? DatasourceStatus.ACTIVE: DatasourceStatus.INACTIVE,
                entity.getType(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getSchema()
        );
    }

}

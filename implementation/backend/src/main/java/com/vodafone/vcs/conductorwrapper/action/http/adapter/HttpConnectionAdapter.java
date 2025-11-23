package com.vodafone.vcs.conductorwrapper.action.http.adapter;

import com.vodafone.vcs.conductorwrapper.action.http.dto.HTTPConnectionDTO;
import com.vodafone.vcs.conductorwrapper.action.http.entity.HttpConnection;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HttpConnectionAdapter {

    private final ModelMapper mapper;

    public HttpConnection toEntity(HTTPConnectionDTO dto) {
        return mapper.map(dto, HttpConnection.class);
    }

    public HTTPConnectionDTO toDTO(HttpConnection entity) {
        return mapper.map(entity, HTTPConnectionDTO.class);
    }

}

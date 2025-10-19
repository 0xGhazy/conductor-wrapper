package com.vcs.flowpilot.action.http.entity.converter;


import com.vcs.flowpilot.action.http.service.EncryptionService;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Converter(autoApply = false)
public class EncryptionConverter implements AttributeConverter<String, String> {
    private static EncryptionService crypto;
    @Autowired
    public void setCrypto(EncryptionService c) { crypto = c; }
    @Override public String convertToDatabaseColumn(String attr) { return crypto.enc(attr); }
    @Override public String convertToEntityAttribute(String db) { return crypto.dec(db); }
}

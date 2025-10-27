package com.vodafone.vcs.conductorwrapper.common.converters;

import com.vodafone.vcs.conductorwrapper.common.Encryption;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Converter(autoApply = false)
public class EncryptionConverter implements AttributeConverter<String, String> {
    private static Encryption crypto;
    @Autowired
    public void setCrypto(Encryption c) { crypto = c; }
    @Override public String convertToDatabaseColumn(String attr) { return crypto.enc(attr); }
    @Override public String convertToEntityAttribute(String db) { return crypto.dec(db); }
}

package cc.kertaskerja.pengajuan_kta.helper;

import cc.kertaskerja.pengajuan_kta.dto.TertandaDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class TertandaConverter implements AttributeConverter<TertandaDTO, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(TertandaDTO tertanda) {
        if (tertanda == null) return null;
        try {
            return objectMapper.writeValueAsString(tertanda);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert PegawaiInfo to JSON", e);
        }
    }

    @Override
    public TertandaDTO convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) return null;
        try {
            return objectMapper.readValue(dbData, TertandaDTO.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert JSON to PegawaiInfo", e);
        }
    }
}

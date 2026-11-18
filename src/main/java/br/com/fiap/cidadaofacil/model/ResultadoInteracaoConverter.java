package br.com.fiap.cidadaofacil.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

// Converte o enum da API para o código usado no banco (COM_RESULTADO, PEDIU_ESCLARECIMENTO, SEM_RESULTADO)
@Converter
public class ResultadoInteracaoConverter implements AttributeConverter<ResultadoInteracao, String> {
    @Override
    public String convertToDatabaseColumn(ResultadoInteracao resultado) {
        return resultado == null ? null : resultado.getCodigoBanco();
    }

    @Override
    public ResultadoInteracao convertToEntityAttribute(String codigo) {
        return codigo == null ? null : ResultadoInteracao.doCodigo(codigo);
    }
}

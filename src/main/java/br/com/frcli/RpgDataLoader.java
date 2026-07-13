package br.com.frcli;

import br.com.frcli.model.Classe;
import br.com.frcli.model.Magia;
import br.com.frcli.model.Raca;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class RpgDataLoader {
    private final ObjectMapper mapper;

    public RpgDataLoader() {
        this.mapper = new ObjectMapper();
        // Evita falhar ao encontrar propriedades extras nos arquivos existentes
        this.mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public List<Raca> carregarRacas(File file) throws IOException {
        return mapper.readValue(file, new TypeReference<List<Raca>>() {});
    }

    public List<Classe> carregarClasses(File file) throws IOException {
        return mapper.readValue(file, new TypeReference<List<Classe>>() {});
    }

    public List<Magia> carregarMagias(File file) throws IOException {
        return mapper.readValue(file, new TypeReference<List<Magia>>() {});
    }
}

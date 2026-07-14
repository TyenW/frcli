package br.com.frcli.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

public class RedeLoja {
    private String id;
    private String nomeRede;
    private List<String> sedeIds = new ArrayList<>();   // ids das SedeRede pertencentes a essa rede

    public RedeLoja() {}

    @JsonCreator
    public RedeLoja(
            @JsonProperty("id") String id,
            @JsonProperty("nomeRede") String nomeRede,
            @JsonProperty("sedeIds") List<String> sedeIds) {
        this.id = id;
        this.nomeRede = nomeRede;
        this.sedeIds = sedeIds != null ? sedeIds : new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNomeRede() {
        return nomeRede;
    }

    public void setNomeRede(String nomeRede) {
        this.nomeRede = nomeRede;
    }

    public List<String> getSedeIds() {
        if (sedeIds == null) {
            sedeIds = new ArrayList<>();
        }
        return sedeIds;
    }

    public void setSedeIds(List<String> sedeIds) {
        this.sedeIds = sedeIds;
    }
}

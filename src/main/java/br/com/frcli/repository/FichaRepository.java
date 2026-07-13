package br.com.frcli.repository;

import br.com.frcli.model.Personagem;
import java.util.List;

public interface FichaRepository {
    void salvar(Personagem p);
    Personagem buscarPorId(String id);
    List<Personagem> listarTodos();
    void deletar(String id);
}

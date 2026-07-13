package br.com.frcli.manager;

import br.com.frcli.event.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AuditManager {
    private static final File LOG_FILE = new File("dados/historico_campanha.log");
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static void registrarListeners() {
        // Assegura que o diretório pai existe
        File parent = LOG_FILE.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        EventBus bus = EventBus.getInstance();

        bus.subscribe(CombateIniciadoEvent.class, e -> {
            log("[COMBATE INICIADO] " + e.getAtacante().getNome() + " vs " + e.getDefensor().getNome());
        });

        bus.subscribe(ItemEquipadoEvent.class, e -> {
            log("[ITEM EQUIPADO] Personagem '" + e.getPersonagem().getNome() + 
                "' equipou '" + e.getEquipamento().getNome() + "' no slot " + e.getSlot());
        });

        bus.subscribe(ItemDesequipadoEvent.class, e -> {
            log("[ITEM DESEQUIPADO] Personagem '" + e.getPersonagem().getNome() + 
                "' desequipou '" + e.getEquipamento().getNome() + "' do slot " + e.getSlot());
        });

        bus.subscribe(TurnoFinalizadoEvent.class, e -> {
            log("[TURNO FINALIZADO] Turno " + e.getNumeroTurno() + " concluído para " + e.getPersonagem().getNome());
        });

        bus.subscribe(EntidadeDerrotadaEvent.class, e -> {
            log("[ENTIDADE DERROTADA] '" + e.getEntidade().getNome() + "' foi derrotada no combate.");
        });
    }

    private static synchronized void log(String message) {
        String timestamp = DATE_FORMAT.format(new Date());
        try (PrintWriter out = new PrintWriter(new FileWriter(LOG_FILE, true))) {
            out.println("[" + timestamp + "] " + message);
        } catch (IOException e) {
            System.err.println("[AuditManager] Falha ao escrever log: " + e.getMessage());
        }
    }
}

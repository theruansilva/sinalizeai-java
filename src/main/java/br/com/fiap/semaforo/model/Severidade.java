package br.com.fiap.semaforo.model;

public enum Severidade {
    BAIXA, MODERADA, ALTA, MUITO_ALTA;

    public static Severidade deCemaden(String nivel) {
        if (nivel == null) return MODERADA;
        String n = nivel.trim().toUpperCase();
        if (n.startsWith("MUITO")) return MUITO_ALTA;
        if (n.startsWith("ALTO")) return ALTA;
        if (n.startsWith("BAIXO") || n.startsWith("OBSERV")) return BAIXA;
        return MODERADA;
    }

    public boolean exigeEvacuacao() { return this == ALTA || this == MUITO_ALTA; }
}

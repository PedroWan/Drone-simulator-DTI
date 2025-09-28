package com.example.dronesim.model;

import java.util.UUID;

public class Pedido {
    private final UUID id;
    private final double x;
    private final double y;
    private final double pesoKg;
    private final Enums.Prioridade prioridade;
    private Enums.StatusPedido status;
    private final long dataChegadaTimestamp; // Novo: para fila de prioridade (FIFO)

    public Pedido(double x, double y, double pesoKg, Enums.Prioridade prioridade) {
        this.id = UUID.randomUUID();
        this.x = x;
        this.y = y;
        this.pesoKg = pesoKg;
        this.prioridade = prioridade;
        this.status = Enums.StatusPedido.PENDENTE;
        this.dataChegadaTimestamp = System.currentTimeMillis();
    }

    public String getId() { return id.toString(); }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getPesoKg() { return pesoKg; }
    public Enums.Prioridade getPrioridade() { return prioridade; }
    public Enums.StatusPedido getStatus() { return status; }
    public void setStatus(Enums.StatusPedido status) { this.status = status; }
    public long getDataChegadaTimestamp() { return dataChegadaTimestamp; } // Novo getter

    @Override
    public String toString() {
        return String.format("Pedido[%s] (x=%.2f,y=%.2f, peso=%.2fkg, prioridade=%s, status=%s)", getId(), x, y, pesoKg, prioridade, status);
    }
}

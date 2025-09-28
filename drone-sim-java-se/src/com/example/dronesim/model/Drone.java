package com.example.dronesim.model;

import com.example.dronesim.model.Enums.StatusDrone;

public class Drone {
    private static int COUNTER = 1;
    private final int id;
    private double capacidadeKg;
    private double alcanceKm;
    private double posX;
    private double posY;
    private double bateriaPercent;
    private StatusDrone status;
    private Pedido currentPedido;

    public Drone(double capacidadeKg, double alcanceKm, double posX, double posY) {
        this.id = COUNTER++;
        this.capacidadeKg = capacidadeKg;
        this.alcanceKm = alcanceKm;
        this.posX = posX;
        this.posY = posY;
        this.bateriaPercent = 100.0;
        this.status = StatusDrone.IDLE;
    }

    public int getId() { return id; }
    public double getCapacidadeKg() { return capacidadeKg; }
    public double getAlcanceKm() { return alcanceKm; }
    public double getX() { return posX; }
    public void setX(double posX) { this.posX = posX; }
    public double getY() { return posY; }
    public void setY(double posY) { this.posY = posY; }
    public double getBateria() { return bateriaPercent; }
    public void consumirBateria(double valor) { bateriaPercent = Math.max(0, bateriaPercent - valor); }
    public void recarregar() {
        this.bateriaPercent = 100.0;
        this.status = StatusDrone.IDLE;
        this.currentPedido = null;
    }
    public void recarregarPosicao() {
        this.posX = 0;
        this.posY = 0;
    }
    public StatusDrone getStatus() { return status; }
    public void setStatus(StatusDrone status) { this.status = status; }
    public Pedido getCurrentPedido() { return currentPedido; }
    public void assignPedido(Pedido p) { this.currentPedido = p; p.setStatus(Enums.StatusPedido.ALOCADO); setStatus(StatusDrone.CARREGANDO);}
    public void finishPedido() { this.currentPedido = null; } // O status será atualizado pelo motor de simulação (Engine)

    @Override
    public String toString() {
        return String.format("Drone{id=%d, cap=%.1fkg, alcance=%.1fkm, bateria=%.1f%%, estado=%s, pedido=%s}",
                id, capacidadeKg, alcanceKm, bateriaPercent, status,
                currentPedido != null ? currentPedido.getId().substring(0, 4) : "Nenhum");
    }
}

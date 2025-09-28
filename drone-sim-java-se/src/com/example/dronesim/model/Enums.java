package com.example.dronesim.model;

public class Enums {
    // Adicionado RECARREGANDO e NAO_IDLE_ALOCADO (para uso temporário durante a alocação).
    public enum StatusDrone { IDLE, EM_VOO, ENTREGANDO, RETORNANDO, CARREGANDO, RECARREGANDO, NAO_IDLE_ALOCADO }
    public enum StatusPedido { PENDENTE, ALOCADO, EM_ENTREGA, ENTREGUE, NAO_ATENDIDO }
    public enum Prioridade { BAIXA, MEDIA, ALTA }
}

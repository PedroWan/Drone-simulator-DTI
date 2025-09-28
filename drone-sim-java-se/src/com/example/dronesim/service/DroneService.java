package com.example.dronesim.service;

import com.example.dronesim.model.*;
import com.example.dronesim.util.GeoUtils;
import java.util.*;

public class DroneService {
    private final List<Drone> drones = new ArrayList<>();
    private final List<Pedido> pedidos = new ArrayList<>();
    // Mapeia o pedido para o drone que o atende (útil para relatórios)
    private final Map<Pedido, Drone> droneDoPedido = new HashMap<>();

    public DroneService() {
        // Criar drones padrão (Base: 0, 0)
        drones.add(new Drone(10.0, 100.0, 0, 0));
        drones.add(new Drone(12.0, 120.0, 0, 0));
        drones.add(new Drone(8.0, 80.0, 0, 0));
        drones.add(new Drone(15.0, 150.0, 0, 0));
        drones.add(new Drone(9.0, 90.0, 0, 0));
    }

    /**
     * Reseta o status de todos os pedidos para PENDENTE e move drones para a base.
     */
    public void resetPedidos() {
        for (Pedido p : pedidos) {
            p.setStatus(Enums.StatusPedido.PENDENTE);
        }
        for (Drone d : drones) {
            d.recarregarPosicao(); // Coloca na base (0,0)
            d.recarregar(); // Garante status IDLE e 100% de bateria
        }
        droneDoPedido.clear();
        System.out.println("Status de pedidos e drones resetados. Prontos para nova alocação.");
    }

    public Drone criarDrone(double capacidadeKg, double alcanceKm, double x, double y) {
        Drone d = new Drone(capacidadeKg, alcanceKm, x, y);
        drones.add(d);
        return d;
    }

    public Pedido criarPedido(double x, double y, double pesoKg, Enums.Prioridade prioridade) {
        if (pesoKg > 15.0) {
            System.err.println("Pacote rejeitado. Peso (" + pesoKg + "kg) excede a capacidade máxima de qualquer drone.");
            return null;
        }
        Pedido p = new Pedido(x, y, pesoKg, prioridade);
        pedidos.add(p);
        System.out.println("Pedido criado: " + p.getId().substring(0, 4) + "...");
        return p;
    }

    public List<Drone> listarDrones() {
        return new ArrayList<>(drones);
    }

    public List<Pedido> listarPedidos() {
        return new ArrayList<>(pedidos);
    }

    public List<Pedido> getPedidosDoDrone(Drone d) {
        // Agora, lista todos os pedidos que têm esse drone no mapeamento
        List<Pedido> list = new ArrayList<>();
        for (Map.Entry<Pedido, Drone> e : droneDoPedido.entrySet()) {
            if (e.getValue().getId() == d.getId()) list.add(e.getKey());
        }
        // Garante a ordem da rota (se o drone tiver uma rota atribuída)
        list.sort(Comparator
                .comparingInt((Pedido p) -> p.getPrioridade().ordinal()).reversed()
                .thenComparing(Pedido::getDataChegadaTimestamp)
        );
        return list;
    }

    /**
     * Aloca pedidos em rotas de 1, 2 ou 3 pacotes para minimizar o número de viagens.
     */
    public Map<Drone, List<Pedido>> alocarPedidos() {
        Map<Drone, List<Pedido>> plan = new HashMap<>();
        droneDoPedido.clear();

        // 1. Resetar e preparar drones
        for (Drone d : drones) {
            plan.put(d, new ArrayList<>());
            d.recarregarPosicao();
            d.recarregar(); // Garante IDLE
        }

        // 2. Coletar e ordenar pedidos
        List<Pedido> pedidosPendentes = new ArrayList<>();
        for (Pedido p : pedidos) {
            if (p.getStatus() == Enums.StatusPedido.PENDENTE) pedidosPendentes.add(p);
        }

        // Ordenação: Prioridade (ALTA > MEDIA > BAIXA); depois por FIFO (para otimizar o 'validarRota')
        pedidosPendentes.sort(Comparator
                .comparingInt((Pedido p) -> p.getPrioridade().ordinal()).reversed()
                .thenComparing(Pedido::getDataChegadaTimestamp)
        );

        int alocados = 0;
        int viagens = 0;

        // 3. Iterar sobre drones IDLE e tentar encontrar a melhor rota
        for (Drone d : drones) {
            if (d.getStatus() != Enums.StatusDrone.IDLE) continue;

            List<Pedido> melhorRota = buscarMelhorCombinacao(d, pedidosPendentes);

            if (!melhorRota.isEmpty()) {
                // Alocar todos os pedidos da melhor rota
                double pesoTotal = 0;
                for (Pedido p : melhorRota) {
                    plan.get(d).add(p);
                    droneDoPedido.put(p, d);
                    p.setStatus(Enums.StatusPedido.ALOCADO);
                    pesoTotal += p.getPesoKg();
                }

                // O drone é atribuído ao PRIMEIRO pedido da rota (para iniciar a simulação)
                d.assignPedido(melhorRota.get(0));
                alocados += melhorRota.size();
                viagens++;
                System.out.printf("Drone %d alocado com rota de %d pedidos (Peso: %.1fkg).%n", d.getId(), melhorRota.size(), pesoTotal);

                // Remover pedidos alocados da lista de pendentes
                pedidosPendentes.removeAll(melhorRota);
            }
        }

        // 4. Marcar pedidos restantes como NÃO_ATENDIDO (se necessário)
        for (Pedido p : pedidosPendentes) {
            p.setStatus(Enums.StatusPedido.NAO_ATENDIDO);
            System.out.println("AVISO: Pedido " + p.getId().substring(0, 4) + "... não pode ser atendido (sem drone disponível/otimização).");
        }

        System.out.println("--- Alocação concluída. " + alocados + " pedidos alocados em " + viagens + " viagens. ---");
        return plan;
    }

    /**
     * Busca a melhor combinação de 1, 2 ou 3 pedidos para o drone.
     */
    private List<Pedido> buscarMelhorCombinacao(Drone drone, List<Pedido> disponiveis) {
        List<Pedido> melhorRota = Collections.emptyList();
        int maxPedidos = 0;

        // Tentativa 1: 3 pacotes (maior otimização)
        for (int i = 0; i < disponiveis.size(); i++) {
            for (int j = i + 1; j < disponiveis.size(); j++) {
                for (int k = j + 1; k < disponiveis.size(); k++) {
                    List<Pedido> rota3 = Arrays.asList(disponiveis.get(i), disponiveis.get(j), disponiveis.get(k));
                    if (validarRota(drone, rota3)) {
                        melhorRota = rota3;
                        maxPedidos = 3;
                        return melhorRota; // Encontrou 3, retorna imediatamente
                    }
                }
            }
        }

        // Tentativa 2: 2 pacotes
        if (maxPedidos < 2) {
            for (int i = 0; i < disponiveis.size(); i++) {
                for (int j = i + 1; j < disponiveis.size(); j++) {
                    List<Pedido> rota2 = Arrays.asList(disponiveis.get(i), disponiveis.get(j));
                    if (validarRota(drone, rota2) && rota2.size() > maxPedidos) {
                        melhorRota = rota2;
                        maxPedidos = 2;
                    }
                }
            }
        }

        // Tentativa 3: 1 pacote
        if (maxPedidos < 1) {
            for (Pedido p : disponiveis) {
                List<Pedido> rota1 = Collections.singletonList(p);
                if (validarRota(drone, rota1)) {
                    // Como a lista já está ordenada por prioridade, o primeiro que cabe é a melhor opção "simples"
                    melhorRota = rota1;
                    maxPedidos = 1;
                    break;
                }
            }
        }

        return melhorRota;
    }

    /**
     * Verifica se o drone consegue realizar a rota (peso e distância).
     * Rota: Base(0,0) -> P1 -> P2 -> ... -> Base(0,0)
     */
    private boolean validarRota(Drone drone, List<Pedido> rota) {
        // 1. Validação de Peso
        double pesoTotal = rota.stream().mapToDouble(Pedido::getPesoKg).sum();
        if (pesoTotal > drone.getCapacidadeKg()) {
            return false;
        }

        // 2. Validação de Alcance (Distância Total)
        double distanciaTotal = 0.0;
        double atualX = 0.0;
        double atualY = 0.0;

        for (Pedido p : rota) {
            distanciaTotal += GeoUtils.distanciaKm(atualX, atualY, p.getX(), p.getY());
            atualX = p.getX();
            atualY = p.getY();
        }

        // Adicionar o retorno à base (último ponto para 0,0)
        distanciaTotal += GeoUtils.distanciaKm(atualX, atualY, 0, 0);

        return distanciaTotal <= drone.getAlcanceKm();
    }

    public Map<Integer, Integer> entregasPorDrone(Map<Drone, List<Pedido>> plan) {
        Map<Integer, Integer> result = new HashMap<>();
        for (Map.Entry<Drone, List<Pedido>> e : plan.entrySet()) {
            result.put(e.getKey().getId(), e.getValue().size());
        }
        return result;
    }

    public Drone droneMaisEficiente(Map<Drone, List<Pedido>> plan) {
        return plan.entrySet().stream()
                .max(Comparator.comparingInt(e -> e.getValue().size()))
                .map(Map.Entry::getKey)
                .orElse(null);
    }
}
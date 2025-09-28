package com.example.dronesim.service;

import com.example.dronesim.model.*;
import com.example.dronesim.util.AsciiMap;
import com.example.dronesim.util.GeoUtils;

import java.util.*;

public class Simulator {
    private final DroneService droneService;

    public Simulator(DroneService service) {
        this.droneService = service;
    }

    public static class SimulationReport {
        public Map<Drone, List<Pedido>> plano;
        public long totalEntregas;
        public double tempoMedioEntrega;
        public Drone droneMaisEficiente;
        public Map<Integer, Double> bateriaConsumidaPorDrone = new HashMap<>();
        public Map<Integer, Integer> recargasPorDrone = new HashMap<>();

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("=== RELATÓRIO TÉCNICO ===\n")
                    .append("Total de entregas: ").append(totalEntregas).append("\n");
            if (totalEntregas > 0) {
                sb.append("Tempo médio por entrega (km): ").append(String.format("%.2f", tempoMedioEntrega)).append("\n");
            }
            sb.append("Drone mais eficiente: ").append(droneMaisEficiente).append("\n")
                    .append("Bateria consumida por drone:\n");
            for (Map.Entry<Integer, Double> e : bateriaConsumidaPorDrone.entrySet()) {
                sb.append("  Drone ").append(e.getKey())
                        .append(": Consumo total=").append(String.format("%.2f", e.getValue())).append("%")
                        .append(", Recargas=").append(recargasPorDrone.getOrDefault(e.getKey(), 0))
                        .append("\n");
            }
            return sb.toString();
        }
    }

    /**
     * Simulação técnica SEQUENCIAL ponto a ponto com relatório detalhado.
     */
    public SimulationReport runSimulation(Map<Drone, List<Pedido>> plan) {
        SimulationReport report = new SimulationReport();
        report.plano = plan;

        long totalEntregas = 0;
        double totalTempo = 0.0; // Usamos a distância como proxy de tempo (em km)

        long allocatedCount = plan.values().stream().mapToLong(List::size).sum();
        System.out.println("--- INICIANDO SIMULAÇÃO TÉCNICA (Rotas Otimizadas) ---");
        System.out.println("Pedidos alocados para simulação: " + allocatedCount);

        if (allocatedCount == 0) {
            System.err.println("AVISO: Nenhum pedido alocado. Verifique se criou pedidos (opção 1) e se eles são compatíveis com a capacidade/alcance dos drones.");
        }

        for (Map.Entry<Drone, List<Pedido>> entry : plan.entrySet()) {
            Drone drone = entry.getKey();
            List<Pedido> pedidosRota = entry.getValue(); // A rota inteira

            if (pedidosRota.isEmpty()) continue;

            // Variavel para rastrear o consumo total durante a simulação
            double consumoTotalSimulacao = 0.0;
            int recargas = 0;

            // Inicia na base (0,0) e com 100%
            drone.recarregar();
            drone.setStatus(Enums.StatusDrone.CARREGANDO);

            double atualX = 0.0;
            double atualY = 0.0;
            double pesoTotalCarga = pedidosRota.stream().mapToDouble(Pedido::getPesoKg).sum();


            // SIMULAÇÃO DA ROTA (PONTO A PONTO)
            for (Pedido p : pedidosRota) {

                System.out.println("-> Drone " + drone.getId() + " em voo para Pedido " + p.getId().substring(0, 4) + " (" + p.getPesoKg() + "kg) em (" + p.getX() + ", " + p.getY() + ")");

                // 1. Distância até o próximo pedido
                double distanciaTrecho = GeoUtils.distanciaKm(atualX, atualY, p.getX(), p.getY());

                // Consumo (simplificado): 0.5% por km + 0.1% por kg de carga
                double consumoTrecho = distanciaTrecho * (0.5 + pesoTotalCarga * 0.1);
                drone.consumirBateria(consumoTrecho);
                consumoTotalSimulacao += consumoTrecho;
                totalTempo += distanciaTrecho;

                // 2. Verifica se precisa recarregar APÓS a chegada ao ponto
                if (drone.getBateria() <= 20) {
                    System.out.println("   [RECARGA URGENTE] Drone " + drone.getId() + " retornando à base (Bateria: " + String.format("%.1f", drone.getBateria()) + "%).");

                    // Simula o retorno e recarga
                    double distRetorno = GeoUtils.distanciaKm(p.getX(), p.getY(), 0, 0);
                    drone.consumirBateria(distRetorno * (0.5 + pesoTotalCarga * 0.1));

                    drone.recarregar();
                    atualX = 0.0;
                    atualY = 0.0;
                    recargas++;

                    // AVISO: Após a recarga, a simulação recomeçaria o trecho
                    System.out.println("   [RECARGA CONCLUÍDA] Drone " + drone.getId() + " retorna à missão a partir da base (0,0).");
                }

                // 3. Entrega do pedido
                atualX = p.getX(); // Drone assume a posição da entrega
                atualY = p.getY();
                p.setStatus(Enums.StatusPedido.ENTREGUE);
                totalEntregas++;

                pesoTotalCarga -= p.getPesoKg(); // A carga diminui após a entrega

            } // Fim do loop de pedidos na rota

            // 4. Retorna à base (distância final do último ponto (atualX, atualY) até (0,0))
            double distanciaBase = GeoUtils.distanciaKm(atualX, atualY, 0, 0);
            System.out.println("-> Drone " + drone.getId() + " finalizou entregas, retornando à base. Distância: " + String.format("%.2f", distanciaBase) + "km");

            double consumoBase = distanciaBase * 0.5;
            drone.consumirBateria(consumoBase);
            consumoTotalSimulacao += consumoBase;
            totalTempo += distanciaBase;

            // Finaliza na base
            if (drone.getBateria() <= 20) {
                drone.recarregar(); // Recarga final se necessário
                recargas++;
            } else {
                drone.recarregarPosicao();
                drone.setStatus(Enums.StatusDrone.IDLE);
            }

            // Guarda estatísticas
            report.bateriaConsumidaPorDrone.put(drone.getId(), consumoTotalSimulacao);
            report.recargasPorDrone.put(drone.getId(), recargas);
        }
        System.out.println("--- SIMULAÇÃO TÉCNICA CONCLUÍDA ---\n");


        report.totalEntregas = totalEntregas;
        report.tempoMedioEntrega = totalEntregas > 0 ? totalTempo / totalEntregas : 0;
        report.droneMaisEficiente = droneService.droneMaisEficiente(plan);

        return report;
    }

    /**
     * Simulação dinâmica com mapa ASCII, alertas e recarga (atualizado para rotas).
     */
    public void runSimulationAscii(Map<Drone, List<Pedido>> plan, int stepMillis) {
        AsciiMap mapa = new AsciiMap(20, 20);

        // Novo: Mapa para rastrear o estado da rota (qual pedido o drone está indo buscar/entregar)
        Map<Drone, Integer> nextPedidoIndex = new HashMap<>();
        plan.keySet().forEach(d -> nextPedidoIndex.put(d, 0));

        boolean dronesAtivos = true;

        System.out.println("--- Iniciando Simulação Dinâmica (Mapa ASCII) ---");

        while (dronesAtivos) {
            dronesAtivos = false;
            mapa.clear();

            for (Map.Entry<Drone, List<Pedido>> entry : plan.entrySet()) {
                Drone drone = entry.getKey();
                List<Pedido> pedidosRota = entry.getValue();

                int currentPedidoIndex = nextPedidoIndex.get(drone);

                // Lógica para determinar a próxima AÇÃO:
                if (drone.getStatus() == Enums.StatusDrone.IDLE) {
                    // Se não houver mais pedidos na rota, o drone está na base IDLE
                    if (currentPedidoIndex >= pedidosRota.size()) {
                        continue;
                    }

                    Pedido next = pedidosRota.get(currentPedidoIndex);
                    // O drone sempre volta IDLE se houver mais pedidos
                    drone.assignPedido(next); // currentPedido = next, Status = CARREGANDO
                    System.out.println("Drone " + drone.getId() + " CARREGANDO pedido " + next.getId().substring(0, 4) + "...");

                } else if (drone.getStatus() == Enums.StatusDrone.CARREGANDO) {
                    drone.setStatus(Enums.StatusDrone.EM_VOO); // Inicia voo
                }

                // ----------------------------------------------------
                // Lógica de Movimento e Simulação
                // ----------------------------------------------------
                if (drone.getStatus() == Enums.StatusDrone.EM_VOO) {
                    Pedido p = drone.getCurrentPedido(); // O pedido atual na rota

                    if (p != null) {
                        dronesAtivos = true;

                        // Movimento: 1 unidade por passo (simulação)
                        double dx = p.getX() > drone.getX() ? 1 : (p.getX() < drone.getX() ? -1 : 0);
                        double dy = p.getY() > drone.getY() ? 1 : (p.getY() < drone.getY() ? -1 : 0);

                        // Verifica se chegou ao pedido
                        if (GeoUtils.distanciaKm(drone.getX(), drone.getY(), p.getX(), p.getY()) <= 1.0) {
                            drone.setX(p.getX());
                            drone.setY(p.getY());

                            System.out.println("Drone " + drone.getId() + " **CHEGOU** e entregou pedido " + p.getId().substring(0, 4) + "!");
                            p.setStatus(Enums.StatusPedido.ENTREGUE);
                            drone.finishPedido(); // currentPedido = null

                            // Avança para o próximo pedido na rota
                            nextPedidoIndex.put(drone, currentPedidoIndex + 1);

                            // Verifica se há mais pedidos na rota
                            if (currentPedidoIndex + 1 >= pedidosRota.size()) {
                                System.out.println("Drone " + drone.getId() + " finalizou todas as entregas. **RETORNANDO à base**.");
                                drone.setStatus(Enums.StatusDrone.RETORNANDO);
                            } else {
                                drone.setStatus(Enums.StatusDrone.IDLE); // Fica IDLE na entrega para carregar o próximo
                            }
                        } else {
                            // Move o drone (simulação de 1km/passo)
                            drone.setX(drone.getX() + dx);
                            drone.setY(drone.getY() + dy);

                            double distRem = GeoUtils.distanciaKm(drone.getX(), drone.getY(), p.getX(), p.getY());

                            // Alertas de Proximidade
                            if (distRem < 5.0 && distRem > 1.0) {
                                System.out.println(">> ALERTA: Drone " + drone.getId() + " próximo do destino. Restam " + String.format("%.1f", distRem) + "km.");
                            }

                            drone.consumirBateria(0.5); // Simulação de consumo
                        }
                    }

                } else if (drone.getStatus() == Enums.StatusDrone.RETORNANDO) {
                    dronesAtivos = true;

                    // Movimento de retorno à base (0, 0)
                    double dx = 0 > drone.getX() ? 1 : (0 < drone.getX() ? -1 : 0);
                    double dy = 0 > drone.getY() ? 1 : (0 < drone.getY() ? -1 : 0);

                    if (GeoUtils.distanciaKm(drone.getX(), drone.getY(), 0, 0) <= 1.0) {
                        drone.recarregarPosicao();
                        nextPedidoIndex.put(drone, pedidosRota.size()); // Marca o fim da rota

                        if (drone.getBateria() <= 20) {
                            System.out.println("Drone " + drone.getId() + " chegou à base e precisa de **RECARGA**.");
                            drone.recarregar();
                        } else {
                            System.out.println("Drone " + drone.getId() + " chegou na base (0,0) e está **IDLE** a espera de novo ciclo.");
                            drone.setStatus(Enums.StatusDrone.IDLE);
                        }
                    } else {
                        // Move o drone
                        drone.setX(drone.getX() + dx);
                        drone.setY(drone.getY() + dy);
                        drone.consumirBateria(0.5);
                    }
                }


                // Lógica de Recarga (voltar à base se a bateria estiver baixa)
                if (drone.getBateria() <= 20 && drone.getStatus() != Enums.StatusDrone.RETORNANDO && drone.getStatus() != Enums.StatusDrone.IDLE) {
                    System.out.println("ALERTA CRÍTICO: Drone " + drone.getId() + " bateria baixa (" + String.format("%.1f", drone.getBateria()) + "%). RETORNANDO IMEDIATAMENTE!");
                    drone.setStatus(Enums.StatusDrone.RETORNANDO);
                    drone.finishPedido();
                }

                // Renderização no Mapa
                mapa.setDrone(drone.getX(), drone.getY(), drone.getId(), drone.getStatus());
                for (Pedido ped : pedidosRota) {
                    if (ped.getStatus() != Enums.StatusPedido.NAO_ATENDIDO) {
                        mapa.setPedido(ped.getX(), ped.getY(), ped.getStatus());
                    }
                }
                if (drone.getStatus() != Enums.StatusDrone.IDLE) dronesAtivos = true;
            }

            mapa.render();
            try { Thread.sleep(stepMillis); } catch (InterruptedException ignored) {}
        }
    }
}
package com.example.dronesim;

import com.example.dronesim.model.*;
import com.example.dronesim.service.*;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class App {
    public static void main(String[] args) {
        DroneService service = new DroneService();
        Simulator simulator = new Simulator(service);
        // Inicializa o runner de testes em Java Puro
        TesteUnitario pureTester = new TesteUnitario(service);

        Scanner sc = new Scanner(System.in);

        System.out.println("=== Drone Simulator (Java SE) ===");

        menuLoop:
        while (true) {
            System.out.println("\nMenu:");
            System.out.println("1) Criar pedido");
            System.out.println("2) Listar pedidos");
            System.out.println("3) Listar drones e alocações");
            System.out.println("4) Gerar Plano de Alocação (Heurística)");
            System.out.println("5) Rodar Simulação Técnica (Relatório)");
            System.out.println("6) Rodar Simulação Dinâmica (Mapa ASCII)"); // Novo Dashboard
            System.out.println("7) Rodar Testes Unitários (Java Puro)");
            System.out.println("8) Sair");
            System.out.print("Escolha: ");
            String opt = sc.nextLine().trim();

            switch (opt) {
                case "1": // Criar pedido
                    try {
                        System.out.print("x (double): ");
                        double x = Double.parseDouble(sc.nextLine().trim());
                        System.out.print("y (double): ");
                        double y = Double.parseDouble(sc.nextLine().trim());
                        System.out.print("pesoKg (double): ");
                        double peso = Double.parseDouble(sc.nextLine().trim());
                        System.out.print("prioridade (BAIXA/MEDIA/ALTA): ");
                        String pr = sc.nextLine().trim().toUpperCase();
                        Enums.Prioridade prioridade;
                        try { prioridade = Enums.Prioridade.valueOf(pr); }
                        catch (IllegalArgumentException e) {
                            System.out.println("Prioridade inválida. Usando BAIXA.");
                            prioridade = Enums.Prioridade.BAIXA;
                        }
                        service.criarPedido(x, y, peso, prioridade);
                    } catch (NumberFormatException ex) {
                        System.out.println("Erro: Entrada numérica inválida. " + ex.getMessage());
                    } catch (Exception ex) {
                        System.out.println("Erro ao criar pedido: " + ex.getMessage());
                    }
                    break;

                case "2": // Listar pedidos
                    List<Pedido> pedidos = service.listarPedidos();
                    if (pedidos.isEmpty()) System.out.println("Nenhum pedido.");
                    else pedidos.forEach(System.out::println);
                    break;

                case "3": // Listar drones
                    List<Drone> drones = service.listarDrones();
                    drones.forEach(d -> {
                        System.out.println(d);
                        List<Pedido> pedidosAlocados = service.getPedidosDoDrone(d);
                        if (!pedidosAlocados.isEmpty()) {
                            System.out.println("  Pedidos alocados neste ciclo:");
                            pedidosAlocados.forEach(p -> System.out.println("    - " + p));
                        }
                    });
                    break;

                case "4": // Alocar pedidos
                    service.resetPedidos();
                    Map<Drone, List<Pedido>> plan = service.alocarPedidos();
                    System.out.println("--- Gerou Plano de Alocação ---");
                    break;

                case "5": // Simulação técnica (Relatório)
                    service.resetPedidos();
                    Map<Drone, List<Pedido>> planTech = service.alocarPedidos();

                    if (planTech.values().stream().allMatch(List::isEmpty)) {
                        System.err.println("NÃO FOI POSSÍVEL ALOCAR PEDIDOS. Crie novos ou verifique o alcance/peso.");
                        break;
                    }

                    System.out.println("\n--- Iniciando Simulação Técnica Sequencial ---");
                    Simulator.SimulationReport report = simulator.runSimulation(planTech);
                    System.out.println("\n--- SIMULAÇÃO CONCLUÍDA ---");
                    System.out.println(report);
                    break;

                case "6": // Rodar Simulação Dinâmica (Mapa ASCII)
                    service.resetPedidos();
                    Map<Drone, List<Pedido>> planAscii = service.alocarPedidos();

                    if (planAscii.values().stream().allMatch(List::isEmpty)) {
                        System.err.println("NÃO FOI POSSÍVEL ALOCAR PEDIDOS. Crie novos ou verifique o alcance/peso.");
                        break;
                    }

                    System.out.println("\n--- Iniciando Simulação Dinâmica (Mapa ASCII) ---");
                    System.out.println("Pressione Ctrl+C para parar a simulação.");
                    // O valor 500 define que o mapa será atualizado a cada 500ms
                    simulator.runSimulationAscii(planAscii, 500);
                    System.out.println("\n--- Simulação Dinâmica Concluída ---");
                    break;

                case "7": // Rodar Testes em Java Puro
                    pureTester.runAllTests();
                    break;

                case "8": // Sair (Movido)
                    System.out.println("Saindo...");
                    break menuLoop;

                default:
                    System.out.println("Opção inválida.");
            }
        }
        sc.close();
    }
}

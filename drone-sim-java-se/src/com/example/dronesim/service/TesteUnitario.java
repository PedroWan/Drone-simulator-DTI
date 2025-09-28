package com.example.dronesim.service;

import com.example.dronesim.model.Drone;
import com.example.dronesim.model.Enums;
import com.example.dronesim.model.Pedido;
import com.example.dronesim.util.GeoUtils;

import java.util.List;
import java.util.Map;

/**
 * Classe para execução de Testes Unitários estritamente em Java Puro (Java SE).
 * Substitui o uso de frameworks de teste como o JUnit, usando lógica simples
 * de asserção (if/else) e controle de fluxo.
 */
public class TesteUnitario {

    private final DroneService service;
    private final double DELTA = 0.0001;
    private int testesExecutados = 0;
    private int testesFalhos = 0;

    public TesteUnitario(DroneService service) {
        this.service = service;
    }

    // --- Assertions Manuais ---
    private void assertTrue(boolean condition, String message) {
        testesExecutados++;
        if (!condition) {
            testesFalhos++;
            System.err.println("[FALHA] " + message);
        } else {
            System.out.println("[OK] " + message.substring(0, Math.min(message.length(), 60)) + "...");
        }
    }

    private void assertEquals(double expected, double actual, double delta, String message) {
        assertTrue(Math.abs(expected - actual) < delta, message);
    }

    private void assertNotNull(Object obj, String message) {
        assertTrue(obj != null, message);
    }

    private void assertNull(Object obj, String message) {
        assertTrue(obj == null, message);
    }

    // --- Métodos de Teste ---

    public void testDistanciaEuclidiana() {
        System.out.println("\n-- Teste: GeoUtils.distanciaKm --");
        service.resetPedidos();

        // Distância da base (0,0) para (3, 4) deve ser 5.0
        double dist = GeoUtils.distanciaKm(0, 0, 3, 4);
        assertEquals(5.0, dist, DELTA, "Distância (0,0) para (3,4) deve ser 5.0.");

        // Distância entre (10, 10) e (10, 20) deve ser 10.0
        dist = GeoUtils.distanciaKm(10, 10, 10, 20);
        assertEquals(10.0, dist, DELTA, "Distância vertical deve ser 10.0.");
    }

    public void testRejeitarPedidoPorPeso() {
        System.out.println("\n-- Teste: Validação de Peso Máximo --");
        service.resetPedidos();

        // Limite máximo de peso (15.0kg)
        Pedido pAcima = service.criarPedido(1, 1, 15.1, Enums.Prioridade.MEDIA);
        assertNull(pAcima, "Pacote acima do limite máximo (15.1kg) deve ser rejeitado.");

        Pedido pNoLimite = service.criarPedido(1, 1, 15.0, Enums.Prioridade.MEDIA);
        assertNotNull(pNoLimite, "Pacote no limite máximo (15.0kg) deve ser aceito.");
    }

    public void testAlocacaoPriorizacaoERejeicao() {
        System.out.println("\n-- Teste: Priorização e Regras de Alcance/Capacidade --");
        service.resetPedidos();

        // 1. Pedido A: ALTA, atende capacidade/alcance (deve ser alocado)
        service.criarPedido(10, 10, 5.0, Enums.Prioridade.ALTA);
        // 2. Pedido B: BAIXA, requer 11kg (cabe apenas no drone de 12kg/15kg - Alcance 120km/150km)
        service.criarPedido(1, 1, 11.0, Enums.Prioridade.BAIXA);
        // 3. Pedido C: Inacessível (Distância de 200km, o máximo é 150km, DistanciaTotal=400km)
        service.criarPedido(200, 0, 1.0, Enums.Prioridade.MEDIA);

        Map<Drone, List<Pedido>> plano = service.alocarPedidos();

        long alocados = plano.values().stream().mapToLong(List::size).sum();

        // 1. Deve alocar 2 pedidos (A e B) e rejeitar C.
        assertEquals(2, alocados, DELTA, "Devem ser alocados exatamente 2 pedidos (A e B).");

        // 2. O Pedido C deve estar como NÃO_ATENDIDO
        Pedido pedidoC = service.listarPedidos().stream()
                .filter(p -> p.getX() == 200)
                .findFirst().orElse(null);
        assertNotNull(pedidoC, "Pedido C deve existir na lista.");
        assertTrue(pedidoC.getStatus() == Enums.StatusPedido.NAO_ATENDIDO, "Pedido C deve ser rejeitado por alcance.");
    }

    public void runAllTests() {
        System.out.println("=================================================");
        System.out.println("        INICIANDO TESTES EM JAVA PURO            ");
        System.out.println("=================================================");

        // Resetar o serviço antes de rodar os testes
        service.resetPedidos();

        testesExecutados = 0;
        testesFalhos = 0;

        try {
            testDistanciaEuclidiana();
            testRejeitarPedidoPorPeso();
            testAlocacaoPriorizacaoERejeicao();
            // Adicione aqui outros testes de funcionalidade
        } catch (Exception e) {
            System.err.println("ERRO FATAL DURANTE TESTES: " + e.getMessage());
            e.printStackTrace();
        } finally {
            System.out.println("\n=================================================");
            System.out.println("  RESUMO: Testes Executados: " + testesExecutados + " | Falhas: " + testesFalhos);
            System.out.println("=================================================");
        }
    }
}

package com.example.dronesim.util;

import com.example.dronesim.model.Enums;

public class AsciiMap {
    private final int width;
    private final int height;
    private final char[][] grid;

    public AsciiMap(int width, int height) {
        this.width = width;
        this.height = height;
        this.grid = new char[height][width];
        clear();
    }

    public void clear() {
        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++)
                grid[y][x] = '.';
        grid[0][0] = 'B'; // Base
    }

    // Método atualizado com marcador de RETORNANDO ('<')
    public void setDrone(double x, double y, int id, Enums.StatusDrone status) {
        int dx = Math.min(width - 1, Math.max(0, (int)Math.round(x)));
        int dy = Math.min(height - 1, Math.max(0, (int)Math.round(y)));

        char marker;
        switch (status) {
            case EM_VOO:
                marker = '>'; // Indo para entrega
                break;
            case RETORNANDO:
                marker = '<'; // Voltando para a base
                break;
            case RECARREGANDO:
                marker = 'R';
                break;
            case IDLE:
            case CARREGANDO:
            default:
                marker = 'D'; // Drone parado ou carregando
                break;
        }

        // Evita sobrescrever a Base ou um 'E' de entrega concluída
        if (dx != 0 || dy != 0) {
            if (grid[dy][dx] != 'E') {
                grid[dy][dx] = marker;
            }
        } else if (dx == 0 && dy == 0) {
            // Se estiver na base, usa o marcador do drone/recarga
            if (status != Enums.StatusDrone.IDLE && status != Enums.StatusDrone.RECARREGANDO) {
                grid[dy][dx] = 'B';
            } else {
                grid[dy][dx] = marker;
            }
        }
    }

    // Método atualizado para marcar 'E' se for ENTREGUE
    public void setPedido(double x, double y, Enums.StatusPedido status) {
        int px = Math.min(width - 1, Math.max(0, (int)Math.round(x)));
        int py = Math.min(height - 1, Math.max(0, (int)Math.round(y)));

        char marker = 'P'; // Padrão: Pedido Pendente/Alocado
        if (status == Enums.StatusPedido.ENTREGUE) {
            marker = 'E'; // E de Entregue
        }

        // Se a posição estiver vazia ('.') ou for a Base ('B'), usa o marcador (P ou E)
        if (grid[py][px] == '.' || grid[py][px] == 'B') grid[py][px] = marker;
    }

    public void render() {
        System.out.println("----------------------------------------");
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) System.out.print(grid[y][x] + " ");
            System.out.println();
        }
        // Legenda atualizada
        System.out.println("Base=B | Pedido=P | Entregue=E | Drone parado=D | Indo=> | Voltando=< | Recarregando=R");
    }
}
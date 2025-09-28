# 🚀 Drone Simulator – Guia de Execução

O projeto é distribuído como um arquivo JAR executável.

A única dependência é o Java Development Kit (JDK 25+).

# ✅ Pré-requisito

- Instale o JDK 25 ou superior no seu sistema:
🔗 Baixar JDK

- Verifique a instalação no terminal:

 -java -version

# 📦 Obtenção do Projeto

- Clique no botão verde "Code" no topo do repositório.

- Selecione "Download ZIP".

- Descompacte o arquivo na sua máquina local.

# ▶️ Como Executar

Abra o Terminal / Prompt de Comando / PowerShell e navegue até o diretório do arquivo drone-simulador.jar.

Exemplo no Windows:
- C:\Users\User> cd Downloads
- C:\Users\User\Downloads> cd drone-simulator-dti
- C:\Users\User\Downloads\drone-simulator-dti> java -jar drone-simulador.jar

📖 Menu de Comandos
Nº	Opção	Descrição

1	Criar pedido	Cria um novo pedido, definindo coordenadas (x, y), peso (kg) e prioridade (ALTA, MÉDIA, BAIXA).

2	Listar pedidos	Exibe todos os pedidos com status: PENDENTE, ALOCADO ou ENTREGUE.

3	Listar drones e alocações	Mostra todos os drones, incluindo capacidade, alcance e status (IDLE, EM_VOO).

4	Gerar plano de alocação (heurística) ⚡	Distribui pedidos entre drones de forma otimizada (combinando rotas).

5	Rodar simulação técnica (relatório) 📊	Executa simulação completa e gera relatório (entregas, tempo médio, bateria, recargas, drone mais eficiente).

6	Rodar simulação dinâmica (mapa ASCII) 🎮	Mostra simulação em tempo real com mapa ASCII atualizado e alertas de entregas.

7	Rodar testes unitários (Java puro)	Executa TesteUnitario.java para validar regras de negócio e funções geométricas.

8	Sair	Encerra a aplicação.

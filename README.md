🚀 Guia de Execução – Drone Simulator

O projeto é empacotado como um arquivo JAR executável.
A única dependência é o Java Development Kit (JDK) 17+.

✅ Pré-requisito

JDK 17 ou superior instalado no sistema.
Para verificar, execute no terminal:

java -version

▶️ Como Executar

Abra o Terminal / Prompt de Comando / PowerShell.

Navegue até o diretório onde está o arquivo drone-simulador.jar.

Exemplo no Windows:

C:\Users\User> cd Downloads
C:\Users\User\Downloads> cd drone-simulator-dti
C:\Users\User\Downloads\drone-simulator-dti> java -jar drone-simulador.jar

📖 Menu de Comandos
Nº	Nome da Opção	Descrição
1	Criar pedido	Cria um novo pedido (pacote). Defina coordenadas, peso (kg) e prioridade (ALTA, MÉDIA, BAIXA).
2	Listar pedidos	Exibe todos os pedidos com seus status: PENDENTE, ALOCADO ou ENTREGUE.
3	Listar drones e alocações	Mostra todos os drones, com capacidade, alcance e status (IDLE, EM_VOO etc.).
4	Gerar plano de alocação (heurística)	⚡ Comando crucial! Executa a lógica de otimização para distribuir pedidos entre os drones.
5	Rodar simulação técnica (relatório)	📊 Comando crucial! Executa a simulação completa e gera relatório final (entregas, tempo médio, consumo de bateria, recargas e drone mais eficiente).
6	Rodar simulação dinâmica (mapa ASCII)	🎮 Simulação em tempo real no console, com mapa ASCII atualizando a posição dos drones e alertas de status.
7	Rodar testes unitários (Java puro)	Executa a classe TesteUnitario.java para validar regras de negócio e funções geométricas.
8	Sair	Encerra a aplicação.

🚀 Como Executar o Simulador
O projeto é empacotado como um arquivo JAR executável. A única dependência é o Java Development Kit (JDK) 17+.

1. Requisito
Java Development Kit (JDK) 17 ou superior instalado no sistema.

2. Execução Direta
Abra o Terminal (ou Prompt de Comando/PowerShell).

Navegue até o diretório onde você salvou o arquivo drone-simulador.jar.
# Usuário está na sua pasta inicial
C:\Users\User> cd Downloads

# Usuário entra na pasta do projeto
C:\Users\User\Downloads> cd drone-simulator-dti

# Usuário executa o JAR
C:\Users\User\Downloads\drone-simulator-dti> java -jar drone-simulador.jar

Comando	Nome da Opção	Descrição e Avaliação
1	Criar pedido	Cria um novo pedido (pacote) no sistema. Você pode definir as coordenadas, o peso (em kg) e a Prioridade (ALTA, MEDIA, BAIXA). Use este comando várias vezes para criar a carga de teste.
2	Listar pedidos	Exibe uma lista detalhada de todos os pedidos criados, mostrando o status atual (PENDENTE, ALOCADO, ENTREGUE).
3	Listar drones e alocações	Exibe uma lista de todos os drones disponíveis, mostrando sua capacidade, alcance e estado atual (IDLE, EM_VOO).
4	Gerar Plano de Alocação (Heurística)	COMANDO CRUCIAL DE AVALIAÇÃO! Executa a lógica de Otimização de Rotas. O sistema tenta combinar múltiplos pacotes por drone (rotas de 3, 2 ou 1 pedido) para minimizar o número total de viagens.
5	Rodar Simulação Técnica (Relatório)	COMANDO CRUCIAL DE AVALIAÇÃO! Executa a simulação sequencial completa das rotas alocadas (Opção 4). Gera o Relatório Técnico final, mostrando o consumo de bateria, número de recargas e o drone mais eficiente.
6	Rodar Simulação Dinâmica (Mapa ASCII)	Executa a simulação em tempo real no console. Exibe o Mapa ASCII se atualizando a cada passo, com marcadores de drone (>, <) e alerta de entregas concluídas (E).
7	Rodar Testes Unitários (Java Puro)	Executa a classe TesteUnitario.java para validar as regras de negócio e a geometria (GeoUtils).
8	Sair	Encerra a aplicação.

# Projeto Robocode - UniEVANGÉLICA SINACEN 2024/1

## Descrição

Projeto desenvolvido para a competição de Robocode da UniEVANGÉLICA, promovendo a exploração de conceitos de lógica de programação e estratégias de desenvolvimento de robôs.

## Tecnologias Utilizadas

- **Linguagem:** Java
- **Bibliotecas:** Robocode

## Como Rodar o Projeto

### Pré-requisitos

- [Robocode](https://robocode.sourceforge.io/) instalado
- JDK (Java Development Kit) instalado

### Passos para Executar

1. Clone o repositório:
    ```sh
    git clone https://github.com/Gust4dev/Robocode_gusta.git
    ```
2. Copie os arquivos `.java` dos robôs para o diretório `robots` do Robocode:
    - Windows: `C:\Robocode\robots`
    - macOS/Linux: `/home/usuario/Robocode/robots`
3. Abra o Robocode.
4. Importe os robôs:
    - Menu: `Robot -> Import robot...` e selecione os arquivos `.java`.
5. Configure uma nova batalha:
    - Menu: `Battle -> New...`
    - Adicione os robôs
    - Configure as regras:
        - Rounds: 5
        - Campo de batalha: 800 x 600 pixels
        - Taxa de resfriamento do canhão: 0.1 por “tick”
        - Tempo de Inatividade: 450 “ticks”
6. Inicie a batalha.

## Configuração das Batalhas

- Quantidade de Rounds: 5
- Tamanho do campo de batalha: 800 x 600 pixels
- Taxa de resfriamento do canhão: 0.1 por “tick”
- Tempo de Inatividade: 450 “ticks”

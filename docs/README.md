# Documentação do Receitas

Ponto de partida para quem chega ao projeto. Se esta é a primeira vez, leia [onboarding.md](onboarding.md) antes de qualquer outro arquivo.

## Qual documento vale mais

Quando dois documentos discordam, use esta ordem:

1. **O código** em `app/src/main`. É o que o aplicativo realmente faz.
2. **Regras de negócio:** [modelagem.md](modelagem.md), [lembretes.md](lembretes.md) e [reposicao.md](reposicao.md).
3. **Interface:** [DESIGN.md](../DESIGN.md), que descreve as decisões vigentes, e [cadastro.md](cadastro.md), que descreve o fluxo de cadastro.
4. **Pesquisa e histórico:** servem para entender o porquê de uma decisão. Não são instruções. Veja a tabela abaixo.

Em 15/10/2026 o autor decidiu que, na divergência sobre o botão de criação, vale o código (ação "+" contextual junto à cápsula). `AGENTS.md`, `visual.md` e `DESIGN.md` foram alinhados. Frases antigas no histórico do `DESIGN.md` e em `revisao-publicacao.md` estão marcadas como **[Substituído]**.

## Mapa dos documentos

| Documento                                                  | Status                           | Quando ler                                                                            |
| ---------------------------------------------------------- | -------------------------------- | ------------------------------------------------------------------------------------- |
| [onboarding.md](onboarding.md)                             | Vigente                          | Primeiro dia: o que é o app, como rodar, onde mexer, glossário.                       |
| [arquitetura.md](arquitetura.md)                           | Vigente                          | Antes de alterar camadas, navegação, banco ou notificações.                           |
| [pendencias.md](pendencias.md)                             | Vigente                          | Antes de planejar trabalho: decisões em aberto, divergências e dívidas.               |
| [modelagem.md](modelagem.md)                               | Vigente                          | Regras de datas, vínculos, renovação, arquivamento e migração do Room.                |
| [lembretes.md](lembretes.md)                               | Vigente                          | Regras de alertas, WorkManager, notificações e roteiro de teste manual.               |
| [reposicao.md](reposicao.md)                               | Vigente                          | Regras de retirada e previsão. Curto; complementa a modelagem.                        |
| [cadastro.md](cadastro.md)                                 | Vigente, com resumo no topo      | Fluxo de cadastro em etapas, regras de Pular e validações. O corpo é cronológico.     |
| [apresentacao.md](apresentacao.md)                         | Vigente                          | Padrão `Route → estado → Screen/Content` e onde alterar o visual.                     |
| [visual.md](visual.md)                                     | Vigente                          | Resumo dos componentes e da barra de navegação.                                       |
| [../DESIGN.md](../DESIGN.md)                               | Vigente, com guia rápido no topo | Decisões de interface. O corpo é um registro cronológico.                             |
| [textos.md](textos.md)                                     | Referência                       | Por que certos textos da interface foram trocados.                                    |
| [auditoria.md](auditoria.md)                               | Referência                       | Resultado da auditoria de falhas e da limpeza de código.                              |
| [revisao-publicacao.md](revisao-publicacao.md)             | Referência                       | Revisão antes de publicar no GitHub, com limites de validação.                        |
| [one-ui-research.md](one-ui-research.md)                   | Pesquisa                         | Referências Samsung consultadas. Retrata o piloto inicial, não o estado atual.        |
| [pillo-research.md](pillo-research.md)                     | Pesquisa                         | Observação de um app de terceiros. Não são requisitos do Receitas.                    |
| [design-standard-proposal.md](design-standard-proposal.md) | Histórico                        | Comparação que levou às listas agrupadas. Não implementar as alternativas.            |
| [../DESIGN-HISTORY.md](../DESIGN-HISTORY.md)               | Histórico                        | Decisões substituídas. Nunca restaurar um padrão a partir dele.                       |
| [../AGENTS.md](../AGENTS.md)                               | Instruções para agentes de IA    | Regras que assistentes devem seguir. Atualizado em 15/10/2026 para refletir o código. |

`docs/references/pillo/` guarda capturas de pesquisa de um app de terceiros. Não são recursos de produção.

## Como manter esta documentação

- **Uma regra, um lugar.** Regra de negócio vai em `modelagem.md`, `lembretes.md` ou `reposicao.md`. Decisão de interface vai em `DESIGN.md`. Se outro documento precisar citar, use link.
- **Registre o que mudou no lugar certo.** Ao substituir uma decisão, atualize o texto vigente e mova o antigo para `DESIGN-HISTORY.md`. Não deixe as duas versões lado a lado no texto vigente.
- **Contagens de testes são pontuais.** Os documentos citam 36, 45, 56, 65, 69 e 72 testes unitários porque cada número pertence à sua rodada. A contagem atual está em [onboarding.md](onboarding.md#verificar-o-estado-do-projeto).
- **Marque divergências em vez de ignorá-las.** Se encontrar um documento que não bate com o código, corrija-o ou acrescente a divergência em [pendencias.md](pendencias.md).
- **Datas.** Use o formato `dd/mm/aaaa` e informe a data de qualquer validação citada.

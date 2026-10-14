# Comparação histórica de padrões — listas agrupadas

As propostas abaixo são históricas. O padrão aprovado e implementado está em [DESIGN.md](../DESIGN.md): ícones junto à identidade substituem os menus de gerenciamento propostos; medicamento usa detalhe único, timeline e quantidade de receitas com acesso à lista filtrada. Não implementar as alternativas deste documento como decisões vigentes.

Pesquisa de 14/10/2026, solicitada após o usuário identificar mistura excessiva de componentes. Este documento é proposta, não aprovação nem alteração da interface. A implementação atual e as alterações locais foram preservadas.

Atualização: o usuário aprovou **Listas agrupadas**. A decisão vigente e o estágio da implementação estão em `DESIGN.md`; o texto abaixo conserva a comparação que fundamentou a escolha.

## Referências consultadas

- [Samsung One UI — Lists](https://developer.samsung.com/one-ui/comp/list.html): organização para leitura e busca, prioridade, rótulos claros, subtítulos e agrupamento visual.
- [Samsung One UI — Buttons](https://developer.samsung.com/one-ui/comp/button.html): estilos por ênfase e consistência na tela, complementando a pesquisa em `one-ui-research.md`.
- [Google Material — Lists](https://m3.material.io/components/lists/overview): rótulo, texto de apoio e elemento final como estrutura do item; resultado indexado. O site depende de JavaScript e a página não forneceu texto completo nesta consulta.
- [Android Developers — Card](https://developer.android.com/develop/ui/compose/components/card): um cartão representa uma peça de conteúdo; é distinto de containers genéricos de layout.
- [Android Developers — Menus](https://developer.android.com/develop/ui/compose/components/menu): menu temporário acionado por um controle, com opções, ícones e divisórias.
- [Apple HIG — Buttons](https://developer.apple.com/design/human-interface-guidelines/buttons?changes=latest_1): hierarquia pela aparência, ações relacionadas com tamanhos coerentes, poucas ações proeminentes e papel destrutivo distinto. Referência comparativa; não adotar Liquid Glass ou unidades iOS no Android.
- [IBM Carbon — Menu buttons](https://www.carbondesignsystem.com/building-blocks/core/components/menu-buttons/guidelines): ações de partes menores da página podem usar overflow; ação principal deve permanecer visível. Referência de organização, não adoção de estética desktop.

As páginas Material Cards/Menus e Apple Buttons abertas diretamente retornaram apenas estrutura dependente de JavaScript. As orientações acima se apoiam nos resultados oficiais indexados e na documentação oficial Android, não em imagens estimadas. As propostas não atribuem dimensões próprias a essas marcas.

## Diagnóstico do projeto

O problema principal é composição, não ausência de tokens. A mesma finalidade aparece como texto azul, botão amplo, ação em par ou grupo dentro de cartão. Cartões estão sendo usados tanto para representar registros quanto para enquadrar informações comuns. O aumento de títulos, divisórias e explicações não definiu regras de densidade e prioridade. É necessário decidir funções e distribuição antes de novas mudanças isoladas.

## Direção recomendada: listas agrupadas

- One UI permanece a referência visual principal. Material Compose fornece os componentes de implementação. Apple/Carbon servem para comparar hierarquia e organização, não para importar outra identidade visual.
- Dados de detalhe usam linhas consistentes dentro de grupos: rótulo à esquerda e valor à direita, com reflow vertical para textos grandes ou extensos. Seções identificam o assunto; não repetir títulos dentro de cada item.
- Listas de registros e histórico usam linhas repetíveis com título, texto de apoio e um controle de ações. Cartão fica reservado a um resumo ou unidade de conteúdo autônoma; não envolver cada campo em cartão.
- Uma ação principal destacada por contexto. Ações secundárias usam botão discreto reconhecível. Ações raras e destrutivas ficam em menu acionado por botão “Ações”, com confirmação quando já exigida pela regra existente.
- No histórico: data de retirada em destaque e data/hora do registro como apoio; Corrigir/Excluir em menu. Total no subtítulo, sem um cartão enorme dedicado ao número.
- No detalhe da receita: identidade e validade no topo; grupo de dados; grupo de acompanhamento com acesso ao histórico; Editar visível; Arquivar/Excluir no menu; renovação como ação principal.
- No cadastro: mesmos grupos e controles, com progresso e avanço previsíveis. Não criar uma nova aparência por etapa.
- Manter os tokens de cor, tipografia, margens, raios e alvos do projeto. Nenhum novo valor visual deve nascer como correção local de tela.

## Alternativa: cartões compactos

Resumo em cartão, dados em grade adaptativa e ações locais visíveis. O histórico usa cartão por evento com botões compactos lado a lado. Favorece a descoberta imediata das ações, mas tem mais superfícies e maior repetição em listas longas. É uma alternativa completa à composição de listas agrupadas; não alternar entre elas arbitrariamente por tela.

## Como seguir após a escolha

Definir o conjunto de componentes por função e registrar a decisão em `DESIGN.md`. Implementar primeiro detalhe da receita e histórico como piloto conjunto; validar normal/200%, 320 dp, nomes longos e claro/escuro. Após aprovação desse piloto, aplicar o mesmo vocabulário a medicamentos, listas e cadastro. Preservar todos os fluxos, callbacks e regras de negócio. A barra de navegação mantém Início, Receitas e Remédios, sem criação, conforme a decisão do usuário.

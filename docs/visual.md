# Identidade visual

A referência atual é a One UI, documentada em [DESIGN.md](../DESIGN.md) e [one-ui-research.md](one-ui-research.md). A referência inicial Pillo foi substituída na direção aprovada pelo usuário.

- `DesignSystem.kt`: paleta, tipografia de sistema e tokens compartilhados.
- `ReceitasTheme`: tema global claro/escuro, sem cores dinâmicas.
- `ContentCard`, `PageHeader`, `DetailHeader` e botões compartilhados: hierarquia consistente entre listas, detalhes e cadastro.
- `FormComponents.kt`: campos, seleção, grupos opcionais e alternâncias.
- `WizardQuestionScreen`: conteúdo rolável, erro acessível e ação principal junto à base, respeitando o teclado.

A barra flutuante mostra somente Início, Receitas e Remédios. Nova receita e Novo medicamento ficam no início rolável de suas páginas, inclusive no vazio. Não há “+” na barra nem FAB separado. A cápsula preserva transparência externa e sua altura medida determina a reserva ao fim da lista.

O padrão aprovado é listas agrupadas: `GroupedSection`/`GroupedValueRow` para informação; cartões para registros e resumos. `DetailIdentityHeader` oferece ícones tonais de editar/remover e reativar quando aplicável. `RemovalChoiceDialog` permite escolher arquivar ou excluir, seguido da confirmação existente. `TimelineEntry` organiza a prévia de três retiradas e o histórico completo, com menus por evento somente no histórico completo.

Receita oferece um acesso à lista de medicamentos, sem expandir acompanhamentos. Selecionar um medicamento abre o mesmo detalhe de Remédios. Receitas vinculadas mostra somente a quantidade e um botão abre receitas filtradas pelo medicamento, com identificação do filtro e opção de mostrar todas. A receita escolhida para retiradas é independente dessa participação.

Experimentos substituídos estão em [DESIGN-HISTORY.md](../DESIGN-HISTORY.md); siga somente DESIGN.md para implementar a direção atual.

Use cores semânticas para dados, ações e status, com texto além da cor. Evite valores locais nas telas; adicione padrões recorrentes aos componentes e tokens compartilhados. A distinção entre medidas documentadas Samsung e adaptações do projeto fica em `DESIGN.md`.

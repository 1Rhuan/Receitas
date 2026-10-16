# Instruções do projeto

Para contexto geral, comece por `docs/onboarding.md`; o índice está em `docs/README.md`. Divergências e dívidas conhecidas ficam em `docs/pendencias.md`.

## Interface

Use nomes descritivos para componentes, arquivos e tokens, sem prefixar tudo com `OneUi`. A referência à linguagem visual Samsung fica em `DESIGN.md`.

Antes de alterar a interface, leia `DESIGN.md` e siga suas decisões e estágio de migração. Reutilize componentes em `app/src/main/java/cc/rhuan/receitas/ui/components` e os tokens, tipografia e cores centralizados em `ui/theme/DesignSystem.kt`. Não crie estilos isolados por tela ou copie valores locais. Novos padrões recorrentes devem virar componentes compartilhados.

O usuário aprovou listas agrupadas para informações de detalhe, cartões para registros/resumos e botões reconhecíveis para ações. Receitas e Remédios compartilham `RecordListScaffold`, componentes e tokens. Nova receita e Novo medicamento são criados pela ação “+” contextual junto à cápsula de navegação, que aparece somente em Receitas e Remédios. As listas preenchidas não têm botão de criação no topo. No estado vazio, a orientação central oferece a ação de criar. Medicamentos da receita preserva sua própria ação de adicionar no início rolável da lista. Detalhes e Cadastro já usam o sistema compartilhado; preserve suas composições vigentes em `DESIGN.md`. Edição/remoção/reativação ficam em ícones tonais junto à identidade. A lixeira oferece arquivar ou excluir definitivamente com as confirmações existentes. Preserve funcionalidades e regras de negócio, documente decisões do projeto separadamente das especificações Samsung e valide conteúdo longo, telas pequenas, fontes ampliadas e temas claro/escuro.

Receita oferece acesso à lista de seus medicamentos; selecionar um item abre o mesmo detalhe de Remédios. No medicamento, Receitas vinculadas mostra somente a quantidade e um botão abre receitas filtradas por participação, com contexto, opção de mostrar todas e retorno preservado. A receita escolhida para retiradas é independente; não criar vínculo automaticamente, exceto ao adicionar medicamento pela lista de uma receita ativa, exceção aprovada pelo usuário. O histórico usa timeline, com prévia somente de leitura e menus por evento na tela completa. Consulte `DESIGN-HISTORY.md` somente como registro histórico, sem restaurar padrões substituídos.

Preserve alterações locais preexistentes. Não descarte dados, funcionalidades ou trabalho não relacionado para obter consistência visual.

A barra flutuante deve mostrar somente Início, Receitas e Remédios, com ícones vetoriais arredondados. Em Receitas e Remédios, a ação “+” contextual fica junto à cápsula, com o mesmo acabamento; no Início ela não aparece. Não criar FAB em outro lugar da tela. Não restaurar Menu nessa barra. Preserve transparência fora da cápsula e use a altura medida como reserva no fim da lista; não recorte toda a área de conteúdo acima da barra.

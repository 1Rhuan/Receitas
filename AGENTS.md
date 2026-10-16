# Instruções do projeto

Para contexto geral, comece por `docs/README.md`. O escopo está em `docs/requisitos.md`, os fluxos em `docs/funcionamento.md` e os critérios e limites da avaliação em `docs/validacao.md`. Ambiente e comandos ficam em `docs/desenvolvimento.md`.

## Interface

Use nomes descritivos para componentes, arquivos e tokens, sem prefixar tudo com `OneUi`. As orientações de implementação ficam em `docs/desenvolvimento.md`.

Antes de alterar a interface, leia `docs/desenvolvimento.md` e preserve os padrões compartilhados existentes. Reutilize componentes em `app/src/main/java/cc/rhuan/receitas/ui/components` e os tokens, tipografia e cores centralizados em `ui/theme/DesignSystem.kt`. Não crie estilos isolados por tela ou copie valores locais. Novos padrões recorrentes devem virar componentes compartilhados.

O usuário aprovou listas agrupadas para informações de detalhe, cartões para registros/resumos e botões reconhecíveis para ações. Receitas e Remédios compartilham `RecordListScaffold`, componentes e tokens. Nova receita e Novo medicamento são criados pela ação “+” contextual junto à cápsula de navegação, que aparece somente em Receitas e Remédios. As listas preenchidas não têm botão de criação no topo. No estado vazio, a orientação central oferece a ação de criar. Medicamentos da receita preserva sua própria ação de adicionar no início rolável da lista. Detalhes e Cadastro já usam o sistema compartilhado; preserve suas composições vigentes no código e em `docs/desenvolvimento.md`. Edição/remoção/reativação ficam em ícones tonais junto à identidade. A lixeira oferece arquivar ou excluir definitivamente com as confirmações existentes. Preserve funcionalidades e regras de negócio, documente decisões do projeto separadamente das especificações Samsung e valide conteúdo longo, telas pequenas, fontes ampliadas e temas claro/escuro.

Receita oferece acesso à lista de seus medicamentos; selecionar um item abre o mesmo detalhe de Remédios. No medicamento, Receitas vinculadas mostra somente a quantidade e um botão abre receitas filtradas por participação, com contexto, opção de mostrar todas e retorno preservado. A receita escolhida para retiradas é independente; não criar vínculo automaticamente, exceto ao adicionar medicamento pela lista de uma receita ativa, exceção aprovada pelo usuário. O histórico usa timeline, com prévia somente de leitura e menus por evento na tela completa.

Preserve alterações locais preexistentes. Não descarte dados, funcionalidades ou trabalho não relacionado para obter consistência visual.

A barra flutuante deve mostrar somente Início, Receitas e Remédios, com ícones vetoriais arredondados. Em Receitas e Remédios, a ação “+” contextual fica junto à cápsula, com o mesmo acabamento; no Início ela não aparece. Não criar FAB em outro lugar da tela. Não restaurar Menu nessa barra. Preserve transparência fora da cápsula e use a altura medida como reserva no fim da lista; não recorte toda a área de conteúdo acima da barra.

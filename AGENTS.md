# Instruções do projeto

## Interface

Use nomes descritivos para componentes, arquivos e tokens, sem prefixar tudo com `OneUi`. A referência à linguagem visual Samsung fica em `DESIGN.md`.

Antes de alterar a interface, leia `DESIGN.md` e siga suas decisões e estágio de migração. Reutilize componentes em `app/src/main/java/cc/rhuan/receitas/ui/components` e os tokens, tipografia e cores centralizados em `ui/theme/DesignSystem.kt`. Não crie estilos isolados por tela ou copie valores locais. Novos padrões recorrentes devem virar componentes compartilhados.

O usuário aprovou a direção de Receitas e autorizou avançar para Medicamentos. Sua preferência mais recente é uma barra de navegação flutuante com a ação de criar integrada, em vez de um FAB separado. A ação “+” abre receita ou medicamento avulso conforme a rota e continua disponível no estado vazio, sem duplicação no cartão de orientação. Essas duas listas devem compartilhar `RecordListScaffold` e os mesmos componentes e tokens. A migração seguinte inclui Detalhes e a reformulação do fluxo de Cadastro e deve eliminar padrões visuais antigos para manter um sistema consistente. Preserve funcionalidades e regras de negócio. Documente mudanças no sistema em `DESIGN.md`, distinguindo decisões do projeto de especificações verificadas nas referências Samsung. Valide conteúdo longo, telas pequenas, fontes ampliadas e temas claro/escuro.

Preserve alterações locais preexistentes. Não descarte dados, funcionalidades ou trabalho não relacionado para obter consistência visual.

A barra flutuante deve mostrar somente Início, Receitas e Remédios, com ícones vetoriais arredondados e a ação de criar integrada. Não restaurar Menu nessa barra. Preserve transparência fora da cápsula e use a altura medida como reserva no fim da lista; não recorte toda a área de conteúdo acima da barra.

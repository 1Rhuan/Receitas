# Cadastro de receitas e medicamentos

O cadastro de receita começa com dados da receita, medicamentos, frequência de retirada e revisão. Medicamento avulso começa diretamente pelo medicamento. Quando há ao menos uma retirada recorrente, uma etapa adicional de lembretes, local e vínculo de receita aparece antes da revisão: são cinco etapas para receita e quatro para medicamento avulso. Sem recorrência, essa etapa é omitida (quatro e três, respectivamente).

Nome do medicamento é obrigatório. Concentração, princípio ativo, orientação escrita e quantidade prescrita são opcionais. Orientação e quantidade prescrita pertencem ao item da receita; quantidade usual de retirada é independente. O app não acompanha doses ou estoque.

Validade pode vir de sugestão do tipo, prazo em dias ou vencimento personalizado. O profissional é opcional e seus dados são preservados em cada receita.

Retiradas recorrentes exigem última data real, intervalo e antecedência. O intervalo aceita dias, semanas, meses e anos. A etapa permite escolher a receita que sustenta o acompanhamento, inclusive a receita sendo cadastrada, ou acompanhar sem receita.

Registrar nova receita preenche o formulário a partir da anterior, preserva o documento antigo e oferece continuidade dos acompanhamentos. Renovação não reinicia retiradas. O usuário pode mudar profissional e medicamentos.

Revisão permite retornar diretamente após corrigir um grupo. O rascunho conserva IDs antes do salvamento e usa SavedStateHandle sem objetos de sugestões. O salvamento completo usa uma transação Room.

Detalhes da receita e medicamento oferecem edição/remoção por ícones junto ao nome. A lixeira permite escolher arquivar ou excluir definitivamente, com as confirmações existentes. Arquivados oferecem reativação; receitas substituídas mantêm suas restrições. Eventos são corrigidos/excluídos no histórico completo em timeline.

Ver medicamentos na receita abre os itens daquele documento. Selecionar um item abre o detalhe único de Remédios. Nele, Receitas vinculadas mostra a quantidade; Ver receitas vinculadas abre a lista filtrada por participação, com acesso aos documentos e opção de mostrar todas. Isso não seleciona automaticamente a receita para retiradas.

Emissão/tipo e validade ficam em grupos distintos. A ação principal na base mantém transparência externa e reserva medida na rolagem, adaptando ao teclado. O sistema de componentes e o padrão vigente estão em [DESIGN.md](../DESIGN.md).

Veja [modelagem.md](modelagem.md) para as regras completas e a migração.

A ação principal inclui o medicamento em edição e avança; não exige confirmar Adicionar antes. Adicionar outro medicamento confirma o item e abre o próximo. Nome personalizado e profissional ficam no grupo opcional. Validade em dias e data personalizada são alternativas de entrada. A revisão permite editar frequência e preferências de lembrete separadamente.

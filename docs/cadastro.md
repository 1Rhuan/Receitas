# Cadastro de receitas e medicamentos

O cadastro de receita tem quatro etapas: receita, medicamentos, retiradas e revisão. Medicamento avulso tem três: medicamento, retirada e revisão.

Nome do medicamento é obrigatório. Concentração, princípio ativo, orientação escrita e quantidade prescrita são opcionais. Orientação e quantidade prescrita pertencem ao item da receita; quantidade usual de retirada é independente. O app não acompanha doses ou estoque.

Validade pode vir de sugestão do tipo, prazo em dias ou vencimento personalizado. O profissional é opcional e seus dados são preservados em cada receita.

Retiradas recorrentes exigem última data real, intervalo e antecedência. O intervalo aceita dias, semanas, meses e anos. A etapa permite escolher a receita que sustenta o acompanhamento, inclusive a receita sendo cadastrada, ou acompanhar sem receita.

Registrar nova receita preenche o formulário a partir da anterior, preserva o documento antigo e oferece continuidade dos acompanhamentos. Renovação não reinicia retiradas. O usuário pode mudar profissional e medicamentos.

Revisão permite retornar diretamente após corrigir um grupo. O rascunho conserva IDs antes do salvamento e usa SavedStateHandle sem objetos de sugestões. O salvamento completo usa uma transação Room.

Detalhes da receita permitem arquivar, reativar e excluir separadamente. Detalhes do medicamento permitem editar o acompanhamento, registrar retirada, consultar/corrigir/excluir eventos e arquivar o acompanhamento.

Veja [modelagem.md](modelagem.md) para as regras completas e a migração.

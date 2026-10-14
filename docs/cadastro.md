# Cadastro de receitas

O fluxo começa no botão Nova Receita da tela inicial ou Nova receita na lista de receitas. Ambos abrem o formulário diretamente, sem parâmetros de edição.

## Etapas

1. Receita: emissão, validade, identificação opcional, lembrete de renovação e profissional opcional.
2. Medicamentos: inclusão, edição e remoção de itens. Nome obrigatório; concentração e orientação escrita opcionais. Nenhum controle de doses ou estoque é exigido.
3. Retiradas: acompanhamento opcional por medicamento, última retirada real, intervalo, antecedência e local. A próxima data é mostrada antes da revisão.
4. Revisão: resumo com ações de edição. Corrigir um grupo de dados e continuar retorna diretamente à revisão. Existe apenas um botão de salvar.

O cadastro de medicamento avulso usa três etapas: Medicamento, Retirada e Revisão.

## Voltar e concluir

- A seta e o botão Voltar do Android retornam à etapa anterior. Ao editar a partir da revisão, retornam à revisão mantendo o preenchimento.
- O botão de fechar pede confirmação antes de descartar alterações.
- A lista distingue editar um medicamento existente de incluir outro, evitando duplicação ao voltar.
- O cabeçalho mostra o número de etapas e o rodapé mantém a ação principal acessível com o teclado aberto.
- O rascunho é armazenado em SavedStateHandle; sugestões de médicos e medicamentos são recarregadas separadamente.
- Uma confirmação mostra o resumo dos lembretes após salvar. Ver receita abre os detalhes e remove o formulário da navegação.

## Persistência e testes

Receita, profissional novo e medicamentos são escritos em uma única transação Room. As preferências de antecedência e ativação de alertas são preservadas nas conversões entre domínio e banco. Os IDs são mantidos antes do salvamento para permitir tentativas após falha sem duplicação.

Os testes cobrem avanço e retorno à revisão, edição de itens, datas inválidas, múltiplos medicamentos, medicamento avulso, falha e nova tentativa, IDs persistidos antes do término do salvamento, recriação da tela, preferências de lembretes e rollback do cadastro completo.

O teste de interface percorre o aplicativo desde a tela inicial, revisa receita e medicamento, recria a atividade, salva e abre os detalhes. Seus registros temporários são removidos ao concluir. Capturas geradas pelo teste são copiadas para a pasta receitas-cadastro-preview em Download do emulador.

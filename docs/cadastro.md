# Cadastro de receitas e medicamentos

## Resumo vigente

Verificado em 15/10/2026 contra `PrescriptionWizardUiState.kt` e `PrescriptionWizardViewModel.kt`. O restante deste documento é um registro cronológico das rodadas: quando houver conflito, vale este resumo e, acima dele, o código. Substituições conhecidas estão marcadas com **[Substituído]**.

### Percursos e quantidade de etapas

| Percurso                 | Sem retirada recorrente                    | Com retirada recorrente |
| ------------------------ | ------------------------------------------ | ----------------------- |
| Receita com medicamentos | 7 etapas                                   | 9 etapas                |
| Receita sem medicamentos | 6 etapas (a etapa de frequência é omitida) | 6 etapas                |
| Medicamento avulso       | 4 etapas                                   | 6 etapas                |

Ordem das etapas da receita:

1. **Nome** da receita. Obrigatório, sem Pular.
2. **Emissão e tipo.** O tipo abre uma tela própria; Personalizado abre outra tela, de validade.
3. **Alertas de vencimento.** Ativação e antecedência em grupo aberto.
4. **Medicamentos.** Opcionais na receita: zero, um ou vários.
5. **Frequência de retirada.**
6. **Antecedência dos lembretes.** Só com recorrência.
7. **Local e receita para retiradas.** Só com recorrência.
8. **Detalhes opcionais** (profissional, observações, forma, princípio ativo, quantidade e orientação), cada um em tela própria.
9. **Revisão.**

O medicamento avulso começa pelo medicamento, segue com frequência, antecedência e local (os dois últimos só com recorrência), detalhes opcionais e revisão.

### Pular

O botão fica **no topo, à direita**, como botão textual neutro, e só aparece quando a regra permite. A base tem apenas a ação principal e o progresso. Pular segue com os valores atuais, sem limpar campos nem desativar configurações, e usa as mesmas validações do avanço.

| Etapa                                       | Pular                                                                                    |
| ------------------------------------------- | ---------------------------------------------------------------------------------------- |
| Nome                                        | Não. O nome é obrigatório.                                                               |
| Emissão e tipo; validade personalizada      | Não.                                                                                     |
| Alertas de vencimento                       | Sim, se a configuração for válida.                                                       |
| Medicamentos da receita                     | Sim, vazio ou com itens válidos. Rascunho incompleto precisa ser corrigido ou cancelado. |
| Medicamento avulso / inclusão pela lista    | Não.                                                                                     |
| Frequência de retirada                      | Somente se nenhum medicamento tiver recorrência.                                         |
| Antecedência e local/receita para retiradas | Sim, se os dados forem válidos.                                                          |
| Detalhes opcionais                          | Sim.                                                                                     |
| Revisão                                     | Não.                                                                                     |

Carregamento, falha inicial, conclusão e edição pendente de tipo também não permitem pular.

---

O cadastro divide as decisões em etapas com perguntas diretas e progressão intermediária padronizada como Avançar. A quantidade de etapas pode crescer para reduzir a informação apresentada de uma vez; essa é uma decisão do projeto, apoiada pela pesquisa de fluxo em [pillo-research.md](pillo-research.md).

Os campos e as configurações aparecem em grupos abertos, sem cards de fundo. Títulos ficam acima do conteúdo, e configurações de alertas aparecem diretamente em grupo aberto. Cards representam medicamentos já incluídos, registros na revisão/confirmação e opções de tipo. `FormSection`, `FormRecordSection`, `RecordSummarySection` e `ReviewRecordCard` centralizam essa diferença; não criar variantes locais por etapa.

Tocar em Tipo de receita abre uma tela própria, sem modal, com cards explicativos. Escolher um tipo predefinido aplica os dias sugeridos e avança aos alertas sem outra confirmação. Apenas Personalizado apresenta o grupo de validade em dias ou por data; Usar validade personalizada valida e avança. Em edição pela revisão, ambos retornam à revisão. Voltar preserva os dados anteriores. O rascunho separado é restaurado com a tela, inclusive o modo dias/data. Abrir a tela por si só não modifica o prazo existente.

A etapa Alertas de vencimento apresenta a ativação e a antecedência diretamente. Não há menu/seção recolhida; a antecedência aparece quando alertas estão ativados. Não há campos de validade nessa etapa.

Receita percorre nome → emissão/tipo → alertas de vencimento → profissional e observações opcionais → medicamentos → frequência de retirada → revisão. **[Substituído]** Os detalhes opcionais agora ficam depois das retiradas, na última tela antes da revisão (veja o resumo vigente). Tipo e validade são configurados na tela interna aberta pelo campo da emissão. **[Substituído]** O nome não pode mais ficar em branco: a validação exige um nome e o primeiro passo não oferece Pular. Registros antigos com nome automático são preservados. Medicamento avulso começa diretamente pelo medicamento.

Quando há ao menos uma retirada recorrente, duas etapas aparecem antes da revisão: antecedência dos lembretes e local/receita para retiradas. São nove etapas para receita e cinco para medicamento avulso; sem recorrência, são sete e três. **[Substituído]** Com a tela de detalhes opcionais, o medicamento avulso tem seis etapas com recorrência e quatro sem. Desativar a recorrência remove essas etapas do percurso. Tipo e validade precisam estar válidos para concluir a emissão; alertas configuram somente ativação e antecedência.

Medicamentos são opcionais no cadastro de receita: é possível salvar sem nenhum, incluir um ou incluir vários. Sem medicamentos, a frequência de retirada é omitida e o fluxo tem seis etapas. A revisão mostra zero medicamentos e orienta a inclusão posterior; sem nome informado, o título automático usa Receita e a data de emissão. **[Substituído]** O nome da receita agora é obrigatório; o título automático só existe em registros antigos. Para cada medicamento iniciado, o nome continua obrigatório; um rascunho parcial precisa ser completado ou cancelado explicitamente, sem descarte silencioso. No cadastro avulso, ao menos um medicamento continua obrigatório. Concentração, princípio ativo, orientação escrita e quantidade prescrita são opcionais. Orientação e quantidade prescrita pertencem ao item da receita; quantidade usual de retirada é independente. O app não acompanha doses ou estoque.

Depois de salvar, Ver medicamentos abre a lista da receita. Adicionar medicamento fica no início rolável dessa lista, tanto vazia quanto preenchida, sem botão adicional no detalhe nem repetição no estado vazio. A ação abre o cadastro de medicamento existente, com contexto da receita. Salvar acrescenta o item de forma transacional e retorna automaticamente à mesma lista atualizada; cancelar retorna sem gravar. Itens já presentes não podem ser duplicados. Documentos substituídos continuam somente para consulta. Os itens e retratos dos medicamentos anteriores são preservados. Por decisão explícita do usuário, essa entrada contextual vincula automaticamente a receita de origem tanto à participação do medicamento quanto ao acompanhamento de retiradas, sem perguntar novamente. O estado, a revisão e a gravação usam a mesma origem, mesmo sem recorrência ativada. Na etapa de local, Receita para retiradas mostra o nome como informação, sem seletor e sem Sem receita para retiradas. Na revisão, Receita do medicamento identifica a origem e o resumo das retiradas repete a referência quando há recorrência. A transação cria primeiro o item e depois grava o vínculo, preservando o histórico existente. A origem precisa estar ativa; receitas arquivadas mostram a ação desabilitada com orientação para reativar pelo gerenciamento existente, e substituídas continuam somente para consulta. O cadastro avulso e a criação geral de receita mantêm a escolha independente de referência para retiradas. Esta exceção contextual substitui a orientação anterior de não criar vínculo automático nessa entrada. O banco usa as tabelas existentes, sem migração de schema.

Validade pode vir de sugestão do tipo, prazo em dias ou vencimento personalizado. O profissional é opcional e seus dados são preservados em cada receita.

Retiradas recorrentes exigem última data real, intervalo e antecedência. O cadastro oferece somente Intervalo entre retiradas (dias), sem seletor de unidade e sem campo adicional. Novos intervalos e alterações desse campo usam dias. Registros antigos em semanas, meses ou anos mostram a duração equivalente até a próxima retirada, calculada a partir da última data; se o campo não for editado, a unidade e o calendário originais são preservados. Ao editar o número, o intervalo passa a dias, sem alterar o histórico nem o vínculo de retirada. Nenhuma migração reescreve dados antigos. Fora da inclusão contextual pela lista, a etapa de local/receita permite escolher a receita que sustenta o acompanhamento, inclusive a receita sendo cadastrada, ou acompanhar sem receita.

Registrar nova receita preenche o formulário a partir da anterior, preserva o documento antigo e oferece continuidade dos acompanhamentos. Renovação não reinicia retiradas. O usuário pode mudar profissional e medicamentos.

Revisão oferece correção separada de nome, emissão/tipo, tipo/validade, alertas, profissional e observações, medicamentos, frequência, lembretes e local/receita para retiradas. Corrigir um grupo retorna diretamente à revisão. Observações são preenchidas antes da revisão e nela aparecem para conferência. O rascunho conserva IDs antes do salvamento e usa SavedStateHandle sem objetos de sugestões. O salvamento completo usa uma transação Room.

Detalhes da receita e medicamento oferecem edição/remoção por ícones junto ao nome. A lixeira permite escolher arquivar ou excluir definitivamente, com as confirmações existentes. Arquivados oferecem reativação; receitas substituídas mantêm suas restrições. Eventos são corrigidos/excluídos no histórico completo em timeline.

Ver medicamentos na receita abre os itens daquele documento. Selecionar um item abre o detalhe único de Remédios. Nele, Receitas vinculadas mostra a quantidade; Ver receitas vinculadas abre a lista filtrada por participação, com acesso aos documentos e opção de mostrar todas. Isso não seleciona automaticamente a receita para retiradas.

Emissão/tipo e validade ficam em grupos distintos. A ação principal na base mantém transparência externa e reserva medida na rolagem, adaptando ao teclado. O sistema de componentes e o padrão vigente estão em [DESIGN.md](../DESIGN.md).

Veja [modelagem.md](modelagem.md) para as regras completas e a migração.

A ação principal inclui o medicamento em edição e avança; não exige confirmar Adicionar antes. Adicionar outro medicamento confirma o item e abre o próximo. Nome personalizado fica na primeira etapa; profissional e observações ficam na etapa opcional posterior à validade. Validade em dias e data personalizada são alternativas de entrada. A revisão permite editar frequência, lembretes e local/receita para retiradas separadamente.

Validação desta divisão em 14/10/2026: 47 testes unitários passaram, incluindo validação por etapa, identificação em branco, correção com retorno à revisão e remoção das etapas condicionais. No emulador passaram dois fluxos completos (cadastro e renovação), dois testes de texto e 20 cenários de composição. Os cenários do cadastro cobrem 320 × 640 dp, fonte 200%, nomes longos, temas claro/escuro e acesso às ações. Capturas inspecionadas em `app/build/outputs/guided-registration/`. Os dois fluxos completos usam ações semânticas para evitar instabilidade de toque durante animações de teclado/rolagem; os testes de composição mantêm interação por toque. Validação em aparelho Samsung e TalkBack permanece pendente.

Ajuste posterior: nome passou a ser a primeira etapa, com edição própria na revisão. Voltar dessa edição retorna à revisão. Build, lint e 47 testes unitários passaram; os dois fluxos completos e os 20 cenários de composição foram repetidos com a nova ordem. O fluxo de cadastro foi repetido após corrigir o retorno da edição do nome. Capturas da entrada em claro e escuro/200% inspecionadas em `app/build/outputs/guided-registration/name-first-light.png` e `name-first-dark-200.png`.

Ajuste de composição: formulários e grupos opcionais deixaram de usar cards; medicamentos já incluídos e resumos mantêm `RecordSummarySection`. Build, lint e os 20 cenários de composição passaram. A entrada e validade em escuro/200% foram repetidas; a captura da tela inteira confirmou os textos completos onde a captura isolada do Compose apresentou cortes. O helper preserva agora as duas capturas. Evidências em `app/build/outputs/guided-registration/form-groups-light.png` e `form-validity-dark-200-display.png`.

Validação do seletor de tipo: build e lint passaram, com os quatro avisos anteriores de versões de dependências. Dois testes instrumentados verificam descrição/seleção, cancelamento sem alteração, confirmação e alcance da última opção. O cenário escuro foi repetido com `font_scale=2.0` no Android para validar a fonte do próprio diálogo (a densidade local do formulário não amplia essa janela); a configuração original do emulador foi restaurada. Capturas inspecionadas em `app/build/outputs/recipe-type-choice/type-choice-light.png` e `type-choice-system-font-200.png`. A tela de formulário usa a caixa de teste 320 × 640 dp; o diálogo usa a janela do emulador.

Dias no seletor: as descrições incluem a validade sugerida derivada do tipo, com plural. Build e os dois testes do seletor passaram; o cenário escuro foi repetido com fonte real de 200% e a configuração do emulador foi restaurada. Capturas atualizadas inspecionadas em `app/build/outputs/recipe-type-choice/type-choice-days-light.png` e `type-choice-days-system-font-200.png`.

Personalizado foi acrescentado ao seletor sem prazo sugerido. Selecioná-lo a partir de um tipo predefinido limpa o prazo sugerido e exige preencher a validade em dias ou por vencimento; confirmar novamente o mesmo tipo preserva o valor informado. Prazos que já foram preenchidos manualmente também são preservados ao escolher Personalizado. O tipo e o prazo escolhido são salvos no documento. A frase “Confirme na próxima etapa” foi retirada dos cards.

Validação de Personalizado: build, lint e 48 testes unitários passaram. A regressão cobre exigência de prazo, edição em dias/data, preservação do valor manual, salvamento e conversão do novo tipo. Os dois cenários do seletor passaram, com repetição do personalizado em fonte real de 200%; a configuração original do emulador foi restaurada. Captura inspecionada em `app/build/outputs/recipe-type-choice/type-choice-custom-system-font-200.png`. O tipo usa o conversor textual existente e não altera o schema Room.
Ao mudar a emissão, um prazo em dias conserva a duração; uma data de vencimento escolhida explicitamente conserva o dia e recalcula a duração. Datas que ficam anteriores/iguais à emissão precisam ser corrigidas antes de prosseguir. A antecedência do alerta é limitada ao prazo aplicado. FormChoiceField e FormChoiceCards compõem o padrão compartilhado; as descrições não ficam no rascunho persistido. Os registros de rodadas anteriores que citam modal ou etapa separada de validade descrevem implementações substituídas por esta decisão.
Validação da tela própria e dos alertas abertos: build, lint e 53 testes unitários passaram. Três cenários da tela verificam avanço automático dos tipos sugeridos, ausência de campos de validade nesses tipos, cancelamento de Personalizado, entrada em dias/data e configuração aberta de alertas. Dois fluxos completos verificam cadastro, renovação, recriação durante a edição pendente, retorno pela revisão e salvamento da validade personalizada. A validação visual usa 320 × 640 dp, claro/escuro e fonte 200%; há verificações de posição do vencimento e da última antecedência acima da ação flutuante. Relatórios e capturas foram preservados em `app/build/outputs/type-validity-page/`. Restam os quatro avisos anteriores de versões de dependências; TalkBack e tablets não foram validados.

A execução inicial de testes incluiu o Samsung por duas conexões e apresentou conflitos de instalação/limpeza. A leitura posterior não encontrou o pacote no aparelho; sem um registro anterior imediato, a preservação dos dados locais não pôde ser confirmada. Após o relato, nenhuma nova alteração no Samsung foi feita por este trabalho; as execuções seguintes fixaram `ANDROID_SERIAL` e `android.injected.device.serial` em `emulator-5554`. As evidências estão em `app/build/outputs/type-validity-page/device-validation-notes.txt`.

## Avançar e Pular

**[Substituído]** A posição descrita nesta seção ("Pular fica na base, junto de Avançar") e a linha "Nome da receita: Sim" da tabela abaixo foram trocadas: Pular fica no topo à direita e o nome não pode ser pulado. Use a tabela do resumo vigente.

As etapas intermediárias usam Avançar. Salvar receita/medicamento, Salvar alterações, Ver receita, Concluir, Usar validade personalizada e Voltar à revisão conservam suas finalidades. O botão Pular fica na base, junto de Avançar, com empilhamento em fonte ampliada e reserva de altura medida na rolagem. Pular não limpa campos nem desativa configurações: segue com os valores atuais, usando as mesmas validações e o retorno direto da revisão.

| Etapa                                         | Pular                                                                                                                 |
| --------------------------------------------- | --------------------------------------------------------------------------------------------------------------------- |
| Nome da receita                               | Sim, inclusive se já houver nome preenchido.                                                                          |
| Emissão e tipo / tela de tipo                 | Não.                                                                                                                  |
| Validade personalizada                        | Não; o prazo precisa ser válido.                                                                                      |
| Alertas de vencimento                         | Sim, se a configuração atual for válida.                                                                              |
| Profissional e observações                    | Sim, preservando o que foi preenchido.                                                                                |
| Medicamentos da receita                       | Sim, vazio ou com itens válidos; preserva os dados preenchidos. Rascunho sem nome precisa ser corrigido ou cancelado. |
| Medicamento avulso / inclusão pela lista      | Não; é preciso cadastrar um medicamento.                                                                              |
| Agenda de retiradas                           | Somente quando nenhum medicamento tem recorrência ativada.                                                            |
| Antecedência / local e receita para retiradas | Sim, se os dados de acompanhamento já forem válidos.                                                                  |
| Revisão e conclusão                           | Não.                                                                                                                  |

Os estados de carregamento, falha inicial, conclusão e edição pendente de tipo também não permitem pular. Prazos ou preferências inválidos precisam ser corrigidos; Pular não contorna validações de salvamento. A etapa de alertas segue aberta, e apenas Personalizado apresenta campos de validade.
Validação de Avançar/Pular: build, lint e 56 testes unitários passaram. Oito cenários instrumentados afetados passaram no emulador: três de navegação, três da tela de tipo/alertas e dois fluxos completos de cadastro/renovação. Cobrem etapas obrigatórias sem Pular, etapas opcionais com dados preservados, vínculo de retirada e quantidade mantidos ao pular, ações finais preservadas, telas de 320 × 640 dp e fonte 200%. A captura ampliada foi repetida após sincronização e conferida pela tela nativa; o último campo fica acima do rodapé. O botão Pular usa fundo opaco apenas dentro de sua forma para impedir que o conteúdo prejudique sua leitura; a transparência ao redor permanece. Capturas e relatórios em `app/build/outputs/wizard-navigation/`, especialmente `navigation-dark-200-display.png`.

Todas as execuções desta rodada fixaram o emulador por ANDROID_SERIAL e android.injected.device.serial. Não houve novas alterações no Samsung. A preservação dos dados do aparelho após o incidente anterior continua sem confirmação; TalkBack e tablets também permanecem sem validação. O lint mantém os quatro avisos anteriores de versões de dependências.

## Observações e separação visual das etapas sete a nove

Observações da receita pertence ao documento, sem depender de profissional. Na etapa quatro, o profissional e as observações usam grupos abertos distintos; a página pergunta por informações adicionais da receita. O mesmo texto é gravado em PrescriptionItem.notes e aparece no grupo Receita da revisão e em Dados da receita no detalhe. Alterar ou cadastrar um profissional não transfere nem apaga essas observações. Não há migração de dados.

No percurso de nove etapas, sete configura a antecedência dos lembretes e oito configura local e receita para retiradas. Cada medicamento tem identidade, dosagem somente se existente, espaçamento de seção e divisor antes do próximo registro. Local e referência de receita ficam em subgrupos abertos. Os campos não foram colocados indiscriminadamente em cards, e nenhuma etapa foi movida ou acrescentada.

A etapa nove foi revisada integralmente, mantendo as seções Receita, Medicamentos e Retiradas e lembretes. Dados da receita usam lista agrupada com lápis tonal junto ao valor correspondente. Cada medicamento usa um cartão de registro com identidade, dosagem opcional e lápis associado, sem botão Editar solto nem concentração inventada. Retiradas e lembretes têm grupos separados por medicamento e divisores; quantidade, próxima data, antecedência, local e referência continuam próximos ao respectivo nome. As ações gerais de agenda, lembretes e local/receita ficam associadas a essa seção e mantêm os destinos anteriores. A alternativa de reunir toda a configuração de cada medicamento em um novo bloco/fluxo não foi adotada. Confirmação reutiliza a composição sem ações de edição.

## Validação de medicamentos opcionais e revisão visual

Build do app e dos testes, lint e 65 testes unitários passaram. A rodada final de interface/persistência teve 36 cenários afetados, sem falhas ou testes ignorados: três fluxos completos de cadastro/renovação, 16 testes Room, oito de navegação e fonte ampliada, dois de texto/calendário, três da nova composição e quatro cenários afetados de formulários/detalhes. A validação cobre receita vazia e nome automático, inclusão de um/vários itens durante o cadastro, inclusão posterior em listas vazia/preenchida, cancelamento, retorno à lista, vínculo automático da origem ativa, limites de intervalo, compatibilidade com semanas/meses/anos, histórico e retratos dos itens preservados, notas sem profissional e após sua alteração, recriação, destinos de edição e etapas sete a nove em 320 × 640 dp, claro/escuro e fonte 200%.

Nome, valores extensos da receita e observações usam apresentação vertical no grupo para evitar colunas estreitas; as observações também usam essa leitura no detalhe. Os cartões de medicamento associam nome, dosagem existente e lápis, e não inventam concentração quando ausente. Os campos das etapas sete e oito continuam abertos e os lembretes ficam separados por medicamento na revisão, com as ações gerais preservadas nessa seção.

Relatórios finais usam o sufixo -final-results.xml em app/build/outputs/optional-medications/. As capturas inspecionadas estão nessa pasta e em app/build/outputs/guided-review/. O lint final tem zero erros e os quatro avisos anteriores de versões de dependências. Foram corrigidos os avisos novos de assinatura dos componentes compartilhados. Houve repetições após indisponibilidade da captura nativa, falha transitória de encerramento do ActivityScenario e falha interna do detector de lint; a captura usa o Compose como alternativa quando necessário, mantendo as verificações funcionais. Nenhum teste foi desabilitado.

Todos os testes/instalações dessa rodada fixaram ANDROID_SERIAL e android.injected.device.serial em emulator-5554. Não houve nova alteração no Samsung, e as limitações do incidente anterior permanecem registradas acima. Receitas arquivadas precisam ser reativadas antes da inclusão contextual, conforme a regra existente para referência de retiradas; substituídas continuam somente para consulta. TalkBack, tablets e validação física no Samsung permanecem pendentes.

### Refinamento de opcionais em 15/10/2026

Os dados opcionais agora ficam depois da configuração principal e antes da revisão. A ordem é nome/identificação/renovação da receita, medicamentos, retiradas e suas preferências quando aplicável, detalhes opcionais e revisão. O medicamento independente começa pela identidade do medicamento e compartilha o encerramento com opcionais/revisão. Formulários opcionais abrem telas próprias; concluir confirma o rascunho e voltar cancela somente a alteração desse editor. Os demais valores e a rolagem da lista são preservados. O cabeçalho mostra o título da tela, Voltar e Pular quando disponível; a saída continua protegida pela confirmação ao voltar do início. Campos têm rótulo externo e apenas os obrigatórios são marcados, sem repetir (opcional).

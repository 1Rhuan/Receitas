# Validação do protótipo

O TCC propõe verificar o funcionamento por cenários de uso, sem medir efeitos clínicos ou adesão ao tratamento. A avaliação deve demonstrar o comportamento descrito nos requisitos e distinguir a correção dos cálculos da clareza da interface e da entrega efetiva de notificações.

Os testes automatizados do projeto ajudam nessa verificação, mas não substituem a execução documentada dos cenários nem uma avaliação com leitor de tela ou pessoas usuárias.

## Cenários relacionados aos requisitos

A tabela define um roteiro de verificação. Não é uma lista de execuções aprovadas. Registre entrada, ação, resultado esperado e observado, versão do código e ambiente ao executar cada cenário.

| Cenário | Requisitos | Entrada e ação | Resultado esperado |
| --- | --- | --- | --- |
| C01 — Receita sem medicamento | RF02, RNF02 | Preencher nome, emissão, validade e salvar | Documento persistido; confirmação oferece inclusão posterior |
| C02 — Medicamento avulso | RF01, RNF02 | Cadastrar medicamento sem receita | Registro salvo sem criar documento automaticamente |
| C03 — Associação contextual | RF03 | Adicionar medicamento pela lista de uma receita ativa | Item incluído; origem também escolhida para retiradas, preservando histórico existente |
| C04 — Validade sugerida | RF05, RF06 | Selecionar prazo de 10 dias, confirmar e depois cancelar outra escolha | Prazo confirmado usado no cálculo; escolha cancelada não altera o valor aplicado |
| C05 — Validade por data | RF04, RF06 | Escolher Personalizado e informar vencimento | Data selecionada preservada e apresentada na revisão |
| C06 — Próxima retirada | RF07, RF08 | Registrar 10/10/2026 com intervalo de 30 dias | Previsão para 09/11/2026 |
| C07 — Evento retroativo | RF07, RF08 | Após C06, registrar 10/09/2026 | Evento entra no histórico; previsão permanece em 09/11/2026 |
| C08 — Vencimento próximo | RF09 | Receita ativa com lembrete de cinco dias | Aviso passa a ser devido na fronteira configurada; vencimento hoje ainda é válido |
| C09 — Retirada próxima | RF10 | Previsão 09/11/2026 e antecedência de três dias | Aviso próximo devido a partir de 06/11/2026; fase muda quando chega a data |
| C10 — Conflito de prazos | RF11 | Receita escolhida vence em 20/11 e retirada prevista para 25/11 | Conflito identificado mesmo antes da janela normal de retirada |
| C11 — Fronteiras do conflito | RF11 | Igualar vencimento e retirada; depois remover a receita escolhida | Não há conflito em ambos os casos; participação em outro documento não substitui a escolha |
| C12 — Reabertura | RNF05 | Salvar receita e medicamento, encerrar o processo e reabrir sem limpar dados | Registros confirmados permanecem disponíveis |
| C13 — Permissão de notificação | RF09, RF10, RNF04 | Negar permissão, gerar alerta e consultar; depois autorizar | Alerta interno disponível; entrega externa respeita permissão e estado do aviso |
| C14 — Fonte e conteúdo | RNF01, RNF03 | Usar nomes longos, 320 × 640 dp, fonte 200%, claro e escuro | Conteúdo legível e rolável; ações finais e retornos acessíveis |
| C15 — Falha e recuperação | RNF02 | Simular falha de carregamento ou salvamento | Mensagem recuperável, sem perda silenciosa nem duplicação na nova tentativa |

Os cenários C08–C11 também podem ser verificados com data de referência fixa nos testes do domínio. Verificar o momento exato de exibição de um aviso no Android é uma avaliação diferente de testar se o alerta é devido naquela data.

## O que os testes automatizados cobrem

| Grupo | Exemplos no projeto |
| --- | --- |
| Cadastros e estado | `PrescriptionRegistrationFlowTest`: confirmação da validade, rascunhos, campos obrigatórios, salvamento e inclusão contextual |
| Cálculos e alertas | `EvaluateRemindersUseCaseTest` e `ReplenishmentStatusTest`: antecedência, fases, conflitos e intervalos |
| Recuperação | `FailureHandlingTest`: erro, nova tentativa e observação sem coletores duplicados |
| Banco | `PrescriptionRegistrationPersistenceTest` e `DatabaseMigrationTest`: transações, histórico, relacionamentos e migrações |
| Interface | `PrescriptionTypeChoiceTest`, `WizardNavigationTest` e outros cenários Compose: navegação e acessibilidade dos controles em telas/fontes diferentes |

As classes ficam em `app/src/test/` ou `app/src/androidTest/`. Compilar os testes instrumentados não comprova sua execução. Repetir um teste isolado depois de uma falha de captura também não equivale a uma execução completa da suíte sem falhas.

## Proteção de dados e persistência

O manifesto desativa backup, os arquivos de extração excluem dados da transferência e as notificações usam `VISIBILITY_PRIVATE`. O banco é local e o manifesto principal não solicita internet. São medidas concretas relacionadas a RNF04, não uma certificação de segurança ou conformidade legal.

A verificação deve considerar o conteúdo apresentado na tela e nas notificações, as configurações de privacidade do aparelho e o acesso aos dados no ambiente de teste. RNF05 trata de dados confirmados no Room, não da garantia de recuperar qualquer formulário ainda não salvo.

## Avaliações ainda necessárias

O projeto ainda precisa de percurso completo com TalkBack, avaliação de tablets e paisagem, medição abrangente de contraste e testes de usabilidade com pessoas usuárias. A entrega periódica sob restrição de bateria e após reiniciar o aparelho também precisa de evidências em ambiente dedicado.

Essas limitações impedem declarar todos os requisitos não funcionais integralmente comprovados. Não se deve inferir melhoria de adesão ou de acesso a medicamentos a partir do funcionamento técnico do protótipo.

## Registro dos resultados

Para cada execução, registre o commit ou a identificação do estado local, a data, a versão do Android e o dispositivo, o comando ou as ações executadas e as falhas. Os comandos de desenvolvimento estão em [Desenvolvimento](desenvolvimento.md#testes-locais).

Os guias descrevem o funcionamento; os relatórios da avaliação e a seção de resultados do TCC devem registrar o que foi efetivamente observado. Contagens de testes de uma versão anterior não comprovam a entrega seguinte.

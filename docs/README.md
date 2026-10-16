# Documentação do Receitas

Uma receita pode continuar válida quando chega a hora de retirar um medicamento, ou vencer antes dessa data. O Receitas foi desenvolvido para acompanhar essas duas informações em conjunto: a validade do documento e a previsão de uma nova retirada ou compra.

O projeto é uma prova de conceito Android para uso pessoal, desenvolvida como TCC. A pessoa cadastra seus documentos e medicamentos, informa a última retirada e o intervalo que utiliza e acompanha os prazos e avisos calculados. A aplicação não consulta farmácias nem autoriza dispensações. Também não avalia adesão ao tratamento ou resultados clínicos.

## Um exemplo

Considere uma receita com vencimento em 20 de novembro e um medicamento cuja próxima retirada esteja prevista para 25 de novembro. O medicamento ainda não está na data da retirada, mas já existe um conflito de prazos. Se essa for a receita escolhida para seu acompanhamento, o aplicativo pode avisar antes da janela habitual de lembrete de retirada.

Esse exemplo reúne os conceitos que orientam o sistema:

- **Receita** é o documento, com emissão, validade e itens prescritos.
- **Medicamento** é um cadastro reutilizável, que pode aparecer em mais de uma receita.
- **Acompanhamento de retiradas** define intervalo, última data real e, opcionalmente, a receita usada como referência de validade.

A participação em uma receita e a escolha da receita para retiradas são relações distintas. Essa separação aparece nos fluxos, no banco e nos alertas.

## Leitura da documentação

| Documento | O que explica |
| --- | --- |
| [Requisitos](requisitos.md) | O escopo do TCC, os casos de uso e a relação entre requisitos e implementação |
| [Funcionamento](funcionamento.md) | Cadastro, validade, retiradas, conflitos de prazo e ciclo dos registros |
| [Arquitetura](arquitetura.md) | Organização do código e caminho de uma retirada da tela ao banco e de volta |
| [Banco de dados](banco-de-dados.md) | Tabelas, chaves, cardinalidades, histórico e migrações |
| [Desenvolvimento](desenvolvimento.md) | Ambiente, execução, localização do código e comandos de testes |
| [Validação](validacao.md) | Cenários de verificação, evidências e limites da avaliação |

Para entender o aplicativo, leia Requisitos e Funcionamento. Para trabalhar no código, comece por Desenvolvimento e siga para Arquitetura. Os diagramas estão incluídos nos respectivos capítulos em PNG; as versões vetoriais SVG ficam em `imagens/`.

## Limites do protótipo

Os dados ficam no dispositivo, sem conta, servidor ou sincronização. Não há exportação nem restauração; backup e transferência pelo Android estão desativados. Limpar os dados ou desinstalar o aplicativo remove os registros locais.

O aplicativo não controla horários de doses, estoque, anexos ou informações de outras pessoas. A ausência desses recursos é coerente com o escopo do trabalho. Notificações externas dependem das permissões e da execução em segundo plano do Android; não há compromisso de entrega em horário exato.

# Pesquisa One UI aplicada ao Receitas

As comparações com o código e propostas abaixo retratam as primeiras versões do piloto. Listas agrupadas já foram aprovadas; a implementação e as pendências vigentes estão em [DESIGN.md](../DESIGN.md). Preservar esta pesquisa como fonte das referências consultadas, sem tratar as descrições do piloto como estado atual.

Consulta: 14/10/2026. A análise das tabelas retrata a primeira versão do piloto; a segunda aplicou parte das propostas naquele momento. A coluna de aplicação contém interpretações do projeto, não instruções literais Samsung. A pesquisa não muda regras médicas, ciclos ou dados persistidos. Para o estado atual, consulte DESIGN.md.

## Componentes

| Referência acessada | Orientação documentada | Aplicação proposta |
| --- | --- | --- |
| [Buttons](https://developer.samsung.com/one-ui/comp/button.html) | Ação imediata e compreensível; coerência do estilo na tela; botões planos para pouca ênfase e diálogos, preenchidos cinza ou coloridos para ênfase maior. Exemplo XML usa raio 18 dp. | Criar estilos centrais de ação; distinguir navegação, salvar e editar pelo papel. Rever o botão arredondado de 28 dp do piloto; 18 dp é o exemplo publicado, não medida de todos os componentes. |
| [Lists](https://developer.samsung.com/one-ui/comp/list.html) | Organização para leitura rápida, prioridade e subtítulos. Recomenda nomes curtos, se possível até 31 caracteres, para configurações e menus; switches à direita em opções liga/desliga. | Agrupar dados de receita, medicamento e acompanhamento. A recomendação de 31 caracteres não limita nomes de medicamentos ou textos do usuário; permitir quebra de linha. Notas longas podem ganhar consulta no detalhe. |
| [Search](https://developer.samsung.com/one-ui/comp/search.html) | Sugestões contextuais, buscas recentes, preenchimento e previsão ajudam a encontrar conteúdo sem digitar o termo exato. | Avaliar sugestões a partir dos registros locais e busca que ignore acentos. O piloto atual usa comparação por substring sem diferenciar maiúsculas; não implementa essas melhorias. |
| [Dialog](https://developer.samsung.com/one-ui/comp/dialog.html) | Decisão e confirmação aparecem na base; informação crítica/progresso no centro. A decisão não desaparece por tempo. | Criar confirmação inferior para descarte e ações relevantes; manter resultado e erro próximos da tarefa. Uma bottom sheet pode ser uma adaptação Compose, não equivalência automática ao diálogo descrito. |
| [Bottom bar](https://developer.samsung.com/one-ui/comp/bottom-bar.html) | Toolbar inferior admite até cinco ações com ícone e texto; excesso vai para Mais. O exemplo de um único botão é desaconselhado. | Separar toolbar contextual de botão de formulário. A faixa atual de Nova receita é escolha do piloto, não reprodução da toolbar Samsung; rever seu custo de espaço junto à navegação. |
| [Bottom navigation](https://developer.samsung.com/one-ui/comp/bottom-navigation.html) | Abas somente com texto, menos de quatro preferencialmente, até cinco se necessário; sem mudar abas por swipe. | Avaliar abas de texto com nomes curtos que caibam com fonte grande. A navegação atual com quatro destinos, ícones e texto é uma adaptação Material e ainda diverge desse padrão. |

## Cores e leitura

| Referência acessada | Orientação documentada | Aplicação proposta |
| --- | --- | --- |
| [Color system](https://developer.samsung.com/one-ui/color/system.html) | Grandes áreas calmas, destaque em pequenas áreas e continuidade da cor do app. Publica azuis por função: Primary dark claro #0072DE/escuro #3E91FF; Primary #0381FE; controle ativo #3E91FF. | Usar cores por papel sem pintar todos os cartões. Comparar a paleta do piloto com os valores publicados; não substituir cores isoladamente sem testar o texto sobre elas. |
| [Theme](https://developer.samsung.com/one-ui/color/theme.html) | Texto deve contrastar; diferença entre fundo e bloco deve ser confortável. Verificar ambos os temas antes da entrega. | Validar também menus, diálogos, seleção, desabilitados e teclado; capturas de tela principal não cobrem todas essas situações. |
| [Layout and typography](https://developer.samsung.com/one-ui/accessibility/layout-and-typo.html) | Texto redimensionável até 200%, com exceções indicadas na página; espaço para evitar cortes; alvos grandes e espaçados; rótulos ao manter ícones pressionados. | Testar 2×, 320 dp, teclado e paisagem. Cabeçalho deve compactar pela área disponível, e controles crescer com o conteúdo. O teste atual de 1,3× não encerra essa validação. |
| [Color and contrast](https://developer.samsung.com/one-ui/accessibility/color-contrast.html) | Não transmitir informação apenas por cor. Contraste mínimo recomendado: 4,5:1 em texto pequeno e 3:1 em texto grande. | Validar pares semânticos e status em escala de cinza; usar texto para validade e pendências. Medir valores efetivos, incluindo opacidade nos estados desabilitados. |

## Textos e fluxo

| Referência acessada | Orientação documentada | Aplicação proposta |
| --- | --- | --- |
| [Focused and purposeful](https://developer.samsung.com/one-ui/writing/focused-and-purposeful.html) | Cada texto deve ajudar a escolher, agir ou entender. Uma tarefa/decisão por vez pode justificar mais passos. Estados vazios devem dar um caminho útil. | Medir o esforço do cadastro, não só a contagem de telas. Usar uma pergunta principal por contexto e detalhes relevantes; mostrar Criar receita, Limpar busca ou Alterar filtros conforme o vazio. |
| [Simple and human](https://developer.samsung.com/one-ui/writing/simple-and-human.html) | Remover avisos hipotéticos e linguagem técnica sem utilidade; interromper só quando necessário; explicar benefícios concretos. | Explicar o que o lembrete fará no momento da configuração. Corrigir erros junto aos campos. Usar mensagens discretas para sucesso, confirmação explícita para descarte de trabalho. |
| [Empowering and engaging](https://developer.samsung.com/one-ui/writing/empowering.html) | Confirmar com perguntas claras, fornecer solução aos problemas e não culpar o usuário; introduzir recursos sem bombardear com dicas. | Preferir “Informe a data da última compra” a um erro genérico. Nomear ações como “Descartar cadastro” e “Continuar cadastro”; evitar confirmações redundantes a cada avanço. |

## Acessibilidade, movimento e adaptação

| Referência acessada | Orientação documentada | Aplicação proposta |
| --- | --- | --- |
| [Focus order](https://developer.samsung.com/one-ui/accessibility/focus-order.html) | Foco lógico e visível; agrupar elementos relacionados; excluir decoração; manter foco onde a ação ocorre. | Organizar leitura de título, instrução, campos, erro e ação. Ao inserir medicamento, manter foco previsível; ao editar pela revisão, retornar ao contexto correspondente. |
| [Screen reader](https://developer.samsung.com/one-ui/accessibility/screen-reader.html) | Nome e estado quando aplicável; descrições curtas, sem repetir tipo/estado na descrição; feedback de ações, transições e resultados. | Nomear switches e filtros, anunciar seleção e salvar; não duplicar texto com contentDescription. Validar a experiência com TalkBack, além dos testes por texto do Compose. |
| [Motion](https://developer.samsung.com/one-ui/motion/basic.html) | Movimento deve explicar relações e responder ao toque; evitar sobreposição de textos. Curva básica [0.22, 0.25, 0.00, 1.00]; duração de 100 a 500 ms conforme objeto/deslocamento. | Centralizar transições de cabeçalho, etapas e confirmação. Não animar para decorar; respeitar ajustes de animação do dispositivo. A transição discreta atual não implementa essa curva. |
| [Large screen layout](https://developer.samsung.com/one-ui/largescreen-and-foldable/large_screen_layout.html) | Classes: <600 dp, 600–<840 dp, ≥840 dp; adaptar estrutura, menus e grids; manter contexto e alcance. | Usar largura disponível da janela. Avaliar lista e detalhe lado a lado em telas amplas e formulário com largura confortável. Não esticar o layout de celular indiscriminadamente. |

## Proposta para o cadastramento existente

O código inspecionado já possui quatro etapas para receita e três para medicamento avulso, além de retorno direto da edição à revisão. Isso é comportamento do app, não regra da Samsung. O componente `WizardQuestionScreen` já posiciona a ação na base e trata espaço do teclado; essas capacidades devem ser preservadas e incorporadas ao sistema central.

A proposta é organizar o fluxo por decisões, com apresentação progressiva:

1. **Receita:** emissão e validade como dados centrais; identificação e profissional com detalhe opcional. Preservar como o domínio distingue validade escolhida, tipo e data explícita.
2. **Medicamentos:** adicionar um por vez, manter resumo dos já incluídos e permitir edição. Apresentar concentração, posologia e quantidades com rótulos distintos; reduzir a densidade inicial dos dados opcionais.
3. **Compra ou retirada recorrente:** primeiro decidir se haverá acompanhamento; mostrar data, intervalo, origem e lembrete conforme a escolha. Manter diferentes modalidades de obtenção e o fluxo de renovação.
4. **Revisão:** agrupar por assunto, permitir corrigir e retornar diretamente. A ação final deve explicar o que será salvo; resultado acessível e caminho claro para o registro.

A quantidade final de telas pode mudar após validar cada contexto. Nenhuma dessas etapas é um fluxo prescrito pela Samsung. Não eliminar campos importantes ou confirmações de regras de negócio apenas para encurtar o percurso.

## Critérios para revisar o piloto

Uma verificação aritmética dos pares opacos mostra a importância de migrar a cor junto com o texto: branco sobre o azul documentado claro `#0072DE` tem contraste de aproximadamente 4,71:1; branco sobre o azul escuro `#3E91FF` tem 3,13:1, insuficiente para texto pequeno pelo critério de 4,5:1. Portanto, não copiar o mesmo texto branco para ambos sem avaliar o papel e o tamanho. O par atual do piloto branco/`#185BCC` tem 6,16:1, e `#002D70`/`#9DBEFF` tem 6,99:1. Estes são cálculos do projeto por luminância relativa sRGB, não novos valores publicados pela Samsung nem auditoria completa da paleta.

- Cabeçalho e próxima compra não devem impedir o acesso à lista quando o espaço é reduzido.
- Selecionar uma composição coerente de navegação e ações inferiores; evitar sobreposição e barras redundantes.
- Aplicar valores Samsung onde documentados e adequados ao papel, mantendo exceções justificadas em DESIGN.md.
- Verificar 200% de fonte, contraste, foco, TalkBack, teclado e rolagem até a última ação.
- Estados vazios, erros e conclusão devem oferecer um próximo passo útil.

## Acesso e limites

As 17 referências acima foram acessadas. Os caminhos inicialmente tentados `comp/buttons.html`, `comp/lists.html` e `color/color-system-and-usage.html` falharam; os links reais do índice são `comp/button.html`, `comp/list.html` e `color/system.html`, que foram lidos. Valores em imagens não foram estimados. Não foi feita uma auditoria de todas as páginas do site, nem teste novo de interface nesta rodada: as alterações desta pesquisa são documentais.

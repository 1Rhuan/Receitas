# Design do Receitas

Decisões vigentes em 14/10/2026. Este documento orienta a implementação. As experiências substituídas e seus resultados estão em [DESIGN-HISTORY.md](DESIGN-HISTORY.md); não devem ser usadas como instruções atuais.

## Direção aprovada

One UI é a referência visual principal; Material Compose é a base de implementação. O padrão aprovado é **listas agrupadas**. Pillo contribui como pesquisa de fluxo de cadastro; Apple e Carbon serviram para comparar organização, sem acrescentar outra identidade visual.

- Informações de detalhe ficam em grupos com título externo e linhas de rótulo/valor. Com fonte ampliada, as linhas passam à organização vertical.
- Cartões representam registros, resumos ou contextos autônomos. Não criar um cartão para cada campo.
- Uma ação principal destacada por contexto. Ações secundárias usam botões reconhecíveis, inclusive quando discretos; evitar texto colorido solto como ação.
- Gerenciamento do registro usa ícones tonais junto à identidade. Ações de eventos do histórico usam menu próprio.
- Preservar conteúdo longo, estados vazios/erro/carregamento, funcionalidades e regras de negócio.

## Navegação e listas

A barra flutuante contém somente **Início, Receitas e Remédios**, com ícones arredondados e texto. Não contém Menu, botão “+” ou FAB separado. A transparência externa permanece, e a altura medida da cápsula determina a reserva no fim das listas; não recortar toda a área acima da barra.

Receitas e Remédios compartilham `RecordListScaffold`, busca, filtros e componentes de registro. **Nova receita** e **Novo medicamento** ficam no início do conteúdo rolável de suas páginas, inclusive no vazio, sem duplicação na orientação. A criação integrada à barra foi substituída por esta decisão.

## Detalhe da receita

Identidade e validade no topo; **Dados da receita** agrupa emissão, tipo, vencimento, situação e informações opcionais. `DetailIdentityHeader` apresenta lápis de edição e lixeira. Receitas arquivadas também oferecem Reativar; substituídas não oferecem edição, reativação ou novo arquivamento. Com fonte ampliada, os ícones ficam acima do nome; em escala normal, ao lado quando houver espaço.

Registrar nova receita continua como ação principal, respeitando o estado atual. Um único acesso **Ver medicamentos**, com contagem, abre **Medicamentos da receita**. Essa lista preserva concentração, orientação e quantidade prescritas. Selecionar um item abre a mesma rota de detalhe usada em Remédios. Voltar percorre medicamento → lista de medicamentos → receita e preserva a rolagem do documento.

Não expandir dados completos de acompanhamento sob cada medicamento na receita e não restaurar o grupo separado “Gerenciar receita”.

## Detalhe do medicamento e receitas vinculadas

Nome e concentração identificam o registro. **Dados do medicamento** agrupa forma, princípio ativo, orientações existentes e a **quantidade de receitas vinculadas**: “1 receita”, “2 receitas”, “0 receitas”. Não listar os nomes dos documentos neste grupo.

O botão **Ver receitas vinculadas** abre a lista de receitas filtrada pelo ID do medicamento. O filtro usa a participação nos itens/documentos, inclusive receitas arquivadas e substituídas, e não o vínculo opcional do acompanhamento. A tela reutiliza `PrescriptionsScreenContent` e `RecordListScaffold`, mostra o medicamento no contexto “Filtrado por medicamento” e permite **Mostrar todas as receitas**. Busca, situação e ordenação continuam combináveis com o filtro. Ao abrir uma receita e voltar, o filtro permanece; Voltar da lista retorna ao medicamento. O filtro pertence a essa entrada de navegação e não altera a aba Receitas. Com zero vínculos, o botão fica desabilitado. Criação e próxima compra ficam fora do contexto filtrado; reaparecem ao mostrar todas.

**Acompanhamento de retiradas** reúne situação, datas, intervalo, quantidade e local. **Receita para retiradas** aparece somente quando uma receita foi escolhida explicitamente e mostra o nome desse documento. Participar de receitas e escolher uma receita para retiradas são relações distintas. Não mostrar “Sem vínculo” como se o medicamento não pertencesse às receitas nem criar um vínculo de retirada automaticamente. Mudanças em receitas são observadas pelos repositórios.

Editar, remover e, quando arquivado, Reativar usam o mesmo cabeçalho contextual da receita. Registrar retirada permanece disponível apenas com recorrência não arquivada.

## Histórico e remoção

O detalhe do medicamento mostra as três últimas retiradas em timeline somente de leitura. **Ver histórico de retiradas** abre a timeline completa, da mais recente para a mais antiga. Cada evento mostra data real, dia da semana e momento do registro; Corrigir/Excluir ficam em menu com as confirmações existentes. A linha conecta eventos ordenados, sem representar intervalos proporcionais ou doses tomadas.

A lixeira de receita e medicamento abre `RemovalChoiceDialog`, com **Arquivar** ou **Excluir definitivamente**, seguido das confirmações existentes. Não repetir arquivamento para registros já arquivados ou substituídos. Cancelar não executa operação.

- Arquivar medicamento preserva eventos e encerra lembretes. Excluir remove acompanhamento e eventos e permanece bloqueado enquanto receitas referenciam o medicamento.
- Arquivar receita encerra os acompanhamentos/lembretes vinculados, seguindo a regra atual. Excluir remove o documento e preserva acompanhamentos e retiradas sem esse vínculo.

## Cadastro

Receita começa por emissão/tipo e validade em grupos distintos; nome e profissional ficam opcionais. Medicamento avulso começa pelo medicamento. Frequência de retirada, preferências condicionais e revisão seguem o mesmo vocabulário de componentes. Quantidade prescrita e quantidade de retirada são independentes.

A ação principal fica junto à base, com transparência externa, altura medida reservada na rolagem e adaptação ao teclado. O cabeçalho participa da rolagem e compacta com pouca altura ou fonte ampliada. A revisão permite corrigir um grupo e voltar diretamente. Renovação preserva o documento anterior, eventos e regras existentes. Veja [docs/cadastro.md](docs/cadastro.md).

## Sistema e referências

Fonte de valores: `ui/theme/DesignSystem.kt`; tema global: `ReceitasTheme`, claro/escuro e sem cores dinâmicas. Usar nomes descritivos sem prefixar componentes com OneUi. Reutilizar `ui/components`, incluindo `GroupedSection`, `GroupedValueRow`, `DetailIdentityHeader`, `TimelineEntry`, `RemovalChoiceDialog`, botões e componentes de formulário. Não copiar valores ou estilos locais por tela.

Especificações consultadas nas referências Samsung:

| Referência | Informação verificada e limite |
| --- | --- |
| [Grid](https://developer.samsung.com/one-ui/layout/grid.html) | Margens laterais de pelo menos 24 dp. |
| [Buttons](https://developer.samsung.com/one-ui/comp/button.html) | Consistência por ênfase; raio de 18 dp em exemplo XML, não medida universal. |
| [Lists](https://developer.samsung.com/one-ui/comp/list.html) | Agrupamento, prioridade e rótulos claros. |
| [Color system](https://developer.samsung.com/one-ui/color/system.html) | Primary dark claro #0072DE e escuro #3E91FF. |
| [Layout and typography](https://developer.samsung.com/one-ui/accessibility/layout-and-typo.html) | Texto ampliável até 200%, com as exceções indicadas na fonte. |
| [Color and contrast](https://developer.samsung.com/one-ui/accessibility/color-contrast.html) | Contraste de 4,5:1 para texto pequeno e 3:1 para grande. |

A barra flutuante, a posição dos ícones, a timeline, o filtro de receitas, os limiares adaptativos e a composição das telas são **decisões do projeto**, não especificações Samsung. A pesquisa detalhada e suas limitações estão em [docs/one-ui-research.md](docs/one-ui-research.md).

## Estágio e validação

Limpeza antes dos commits: removidos 33 textos sem referência, o ícone antigo de ordenação, sete arquivos Poppins, imports sem uso e o slot de ações sem consumidores em `DetailHeader`. A barra e tipografia legadas já substituídas foram retiradas. Mantidos componentes ainda usados, prévias e entradas de framework. Build do app/testes, 45 testes unitários e lint passaram; não restam avisos de recursos sem uso. Cinco cenários de interface passaram, com repetição isolada do cenário de receita após falha transitória da captura do diálogo. Persistem quatro avisos de versões de dependências. Configurações locais do Android Studio foram preservadas fora dos commits; cache `.kotlin/` fica ignorado.

Rodada de quantidade e filtro: build do app/testes passou; 45 testes unitários e quatro cenários instrumentados passaram no emulador. Cobrem resumo sem nomes, navegação medicamento → receitas filtradas → documento → retorno, remoção do filtro, participação histórica, combinação com busca/situação/ordenação e zero vínculos com acesso desabilitado. O cenário da lista com fonte 200% passou após ajustar o teste para localizar itens ainda não compostos fora da área visível. Capturas inspecionadas em `app/build/outputs/medication-recipe-filter/`. As fixtures do fluxo integrado são de demonstração e são limpas pelo teste. Links locais dos documentos foram verificados; não houve alteração de dados reais.

Listas, detalhes de receita/medicamento, histórico e cadastro já compartilham o sistema. As telas descritas acima adotam as composições aprovadas. Não interpretar isso como auditoria completa de todas as superfícies do app.

Validar build e testes adequados ao fluxo, conteúdo longo, 320 × 640 dp, fonte 200%, temas claro/escuro, estados vazios/erro e retorno. Capturas e resultados das rodadas anteriores estão no histórico. TalkBack, contraste de todas as superfícies e layout dedicado a tablets permanecem pendentes; não foram comprovados pelas capturas. O Samsung não foi validado nas rodadas recentes; a validação usa o emulador.

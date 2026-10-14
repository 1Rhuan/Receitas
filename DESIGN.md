# Design do Receitas

## Status e escopo

O usuário aprovou a direção visual de **Receitas** e autorizou a migração de **Medicamentos**. Sua escolha mais recente é uma barra de navegação flutuante com a ação de criar integrada, sem um FAB separado. Ambas as listas compartilham cabeçalho, estrutura de rolagem, cartões, busca, filtros, tema e navegação com ícones e rótulos. Detalhes e Cadastro ainda não foram migrados nesta etapa. Funcionalidades, dados e regras de negócio devem ser preservados. Componentes e tokens usam nomes descritivos, sem o prefixo `OneUi`, conforme preferência explícita do usuário.

## Referências consultadas em 14/10/2026

As duas referências solicitadas foram acessadas antes da edição:

- [Basic structure](https://developer.samsung.com/one-ui/structure/basic.html): descreve superfícies do sistema (Home, Lock, Recents, Quick panel, Edge panel); não fornece uma tabela de tokens para aplicativos.
- [App bar](https://developer.samsung.com/one-ui/comp/app-bar.html): cabeçalho expandido, título central e informações abaixo; versão condensada durante a rolagem; preservação do estado ao retornar; no máximo três ações de cabeçalho recomendadas.
- [Basic layout](https://developer.samsung.com/one-ui/layout/basic.html): área superior de visualização e área inferior de interação; agrupamento lógico do conteúdo e controles; títulos centrais na área de visualização.
- [Grid](https://developer.samsung.com/one-ui/layout/grid.html): margens de **pelo menos 24 dp** nas laterais para informação e controles.
- [Interaction and control](https://developer.samsung.com/one-ui/accessibility/interaction-and-control.html): posições consistentes dos controles, alternativas a gestos complexos e erros com caminhos claros de recuperação.
- [Overview](https://developer.samsung.com/one-ui/index.html): referência adicional enviada pelo usuário e acessada após o piloto. Define quatro princípios: foco na tarefa, interação natural, conforto visual e adaptação a dispositivos e usuários. Recomenda jornadas curtas e fluidas, controles acessíveis na área inferior e suporte à leitura com temas e fontes ajustáveis. Não acrescenta medidas numéricas aos tokens.

## Aplicação dos princípios gerais

As decisões abaixo são interpretações para este projeto dos princípios do Overview, e devem orientar a reformulação do cadastro após aprovação do piloto:

- **Foco na tarefa:** pedir apenas os dados necessários ao contexto; aproveitar informações já cadastradas; mostrar campos opcionais sem obrigar o usuário a preencher ou atravessar etapas vazias. Avaliar o número de passos pelo esforço total, sem criar uma etapa por campo.
- **Interação natural:** manter a ação principal próxima da base, retorno previsível e dados preenchidos preservados ao voltar; permitir corrigir informações pela revisão sem reiniciar o cadastro.
- **Conforto visual:** apresentar instruções curtas, hierarquia clara, estados de erro junto ao campo e status com texto; respeitar ampliação de fonte e os dois temas.
- **Adaptação:** ajustar a área de visualização ao espaço disponível para que o cabeçalho não impeça o acesso à tarefa em telas pequenas; manter campos e ações alcançáveis com teclado aberto e conteúdo rolável.

A primeira versão foi validada em 320 dp e fonte 1,3×, mas seu cabeçalho ocupava muita área inicial. Na segunda versão, a altura disponível e a escala da fonte determinam o cabeçalho compacto. A nova rodada inclui 2×. A aprovação visual não substitui a validação de acesso à tarefa.

Não foram extraídas medidas de imagens por estimativa. As referências iniciais não estabeleciam as cores, raios, fontes ou altura de cabeçalho usados no piloto; esses valores continuam identificados como **decisões do projeto**. A exploração posterior encontrou cores de sistema, um raio em exemplo de botão e critérios numéricos de acessibilidade e movimento. Eles estão registrados abaixo, sem atribuir retroativamente à Samsung os tokens do piloto. A implementação é inspirada na One UI; não depende da fonte SamsungOne ou de bibliotecas proprietárias Samsung.

## Exploração aprofundada do site

Em 14/10/2026 foram consultadas mais 17 páginas sobre componentes, cores, escrita, acessibilidade, movimento e telas grandes. O relatório com links, recomendações e comparação com o código atual está em [docs/one-ui-research.md](docs/one-ui-research.md). Todas as páginas listadas no relatório foram acessadas pelos links do índice; tentativas iniciais em caminhos inferidos de Buttons, Lists e Color falharam e foram substituídas pelos endereços reais. Não há referência indisponível sendo usada como fundamento.

### Valores documentados encontrados

| Item | Valor publicado | Fonte e limite de aplicação |
| --- | --- | --- |
| Azul `Primary dark` | claro `#0072DE`; escuro `#3E91FF` | [Color system](https://developer.samsung.com/one-ui/color/system.html), exemplos para botões preenchidos e subtexto |
| Azul `Primary` | `#0381FE` nos dois temas | mesma página, exemplos de FAB e slider |
| `Color control activated` | `#3E91FF` nos dois temas | mesma página, checkbox e switch |
| Raio de botão | 18 dp | [Buttons](https://developer.samsung.com/one-ui/comp/button.html), exemplo XML de fundo/máscara; não é raio universal de cartões |
| Ampliação de texto | até 200% | [Layout and typography](https://developer.samsung.com/one-ui/accessibility/layout-and-typo.html); a página excetua subtítulos e texto em imagens |
| Contraste | texto pequeno 4,5:1; grande 3:1 | [Color and contrast](https://developer.samsung.com/one-ui/accessibility/color-contrast.html); a fonte define grande como mais de 18 dp normal ou 14 dp bold |
| Movimento | de 100 a 500 ms; curva `[0.22, 0.25, 0.00, 1.00]` | [Motion](https://developer.samsung.com/one-ui/motion/basic.html), duração depende do objeto e deslocamento |
| Classes de largura | compacta <600 dp; média de 600 a <840 dp; expandida ≥840 dp | [Large screen layout](https://developer.samsung.com/one-ui/largescreen-and-foldable/large_screen_layout.html), largura da janela disponível |

Na segunda versão foram aplicados os azuis `Primary dark` aos temas e o raio de 18 dp aos botões. Texto escuro sobre o botão azul no tema escuro é uma escolha do projeto para preservar contraste. A fonte dá medidas em dp para os limiares de texto grande; a implementação continua usando sp e contabiliza a escala do usuário. Para textos do projeto, adotar 4,5:1 como critério conservador simplifica a verificação. Valores publicados de curva, duração e classes de largura continuam disponíveis para a migração; não são implementados automaticamente em toda a interface.

### Revisões necessárias antes da migração

1. Rever o cabeçalho em pouca altura disponível, com teclado e fontes grandes. Ampliar a validação de 1,3× para 2×; a rodada anterior não comprova o critério de 200%.
2. Rever a navegação inferior: a documentação apresenta abas somente com texto, enquanto o piloto mantém ícones + texto do Material. A faixa de ação única atual é uma adaptação de formulário; não reproduz a toolbar de ações descrita em Bottom bar, que rejeita um único botão. Avaliar a composição completa, evitando barras empilhadas que consumam a área útil.
3. Ajustar raio e pares de cores de botões com base nos novos valores publicados; manter a rastreabilidade das adaptações necessárias à leitura.
4. Equilibrar menos esforço com uma decisão principal por etapa. A Samsung aceita mais passos quando isso ajuda a focar; não comprimir o cadastro inteiro em uma tela apenas para reduzir a contagem.
5. Extrair padrões de campo, erro, lista de opções, confirmação e revisão editável para componentes centrais; manter foco previsível e feedback acessível ao adicionar medicamentos ou salvar.
6. Oferecer ações úteis nos estados vazios e nas buscas sem resultado; revisar notas extensas em cartões para preservar a leitura rápida sem perder sua consulta no detalhe.

Na segunda versão, as revisões de cabeçalho, botão, navegação textual e recuperação do estado vazio foram implementadas. O teste de fonte 2× foi acrescentado. Validação com TalkBack, teclado e todas as demais superfícies continua necessária antes da migração; Cadastro ainda aguarda a avaliação solicitada pelo usuário.

## Sistema centralizado

Código fonte: `ui/theme/DesignSystem.kt`. Nunca repetir números ou hexadecimais em telas.

| Categoria | Valor | Origem |
| --- | --- | --- |
| Margem lateral | 24 dp | mínimo documentado no Grid Samsung |
| Espaçamentos | 8, 12, 16, 24 dp | projeto |
| Respiro vertical do cabeçalho expandido | 24 dp acima e abaixo | projeto; altura cresce com texto |
| Raios | 12 / 16 / 24 / 28 dp | projeto |
| Raio dos botões | 18 dp | exemplo documentado Samsung Buttons |
| Cabeçalho compacto | altura disponível <600 dp ou escala de fonte ≥1,5× | projeto; esse limiar de altura não é a classe de largura Samsung |
| Alvo mínimo dos filtros | 48 dp | projeto |
| Busca | mínimo 56 dp | projeto |
| Ícones | 24 dp | projeto / tamanho padrão Material |
| Reserva inferior da lista | 24 dp | projeto; somada à altura medida da barra no contentPadding da lista |
| Fonte | sistema Android (`FontFamily.Default`) | projeto, substitui Poppins no piloto |
| Título expandido | 36 sp / linha 44 sp, bold | projeto |
| Título condensado | 22 sp / linha 28 sp, semibold | projeto |
| Títulos de seção e registro | 18 sp / linha 26 sp, semibold | projeto |
| Corpo | 16/24, 14/20, 12/18 sp | projeto |
| Rótulos | 14/20 e 12/18 sp | projeto |

Paleta semântica definida no mesmo arquivo. Claro: fundo `#F2F2F2`, cartão `#FFFFFF`, texto `#171717`, secundário `#595959`, ação `#0072DE`, destaque `#E8F0FF`. Escuro: fundo `#000000`, cartão `#1C1C1E`, texto `#F5F5F5`, secundário `#BDBDBD`, ação `#3E91FF`, texto sobre ação `#001A3D`, destaque `#1B355C`. Os azuis de ação são documentados; os demais pares são decisões do projeto. Alertas usam `errorContainer`/`onErrorContainer` e rótulo textual. Não usar cores dinâmicas no novo sistema para manter a direção aprovada.

O tema do piloto acompanha a seleção claro/escuro do tema hospedeiro. Durante a migração somente Receitas e sua barra inferior usam `DesignTheme`. Depois da aprovação, elevar o tema para a raiz e remover o tema visual anterior, evitando dois sistemas permanentes.

## Componentes e comportamento

`ui/components/Components.kt` concentra `PageHeader`, `SectionHeading`, `ContentCard`, `EmptyStateCard`, `StatusLabel`, `FilterPill`, `ActionButton`, `SecondaryActionButton` e `SearchField`. O cartão reutiliza `AppCard`. `RecordListScaffold` centraliza cabeçalho, insets, rolagem e estado de expansão. A criação pertence ao `TextNavigationBar`, evitando ações repetidas em cada tela. O campo de busca usa “Buscar” com fonte grande e um rótulo específico em escala normal.

### Refinamento da ação e estado vazio

Após avaliar ações no cabeçalho, junto à lista e em FAB separado, o usuário escolheu explicitamente “Barra flutuante com a ação de criar integrada”. A barra tem superfície arredondada com raio de 28 dp, margem lateral de 24 dp, sombra de 8 dp e uma ação “+” de 48 dp. Raio, sombra e composição são escolhas do projeto, não medidas atribuídas à navegação atual da Samsung. Não há FAB separado nem ação de criação no cabeçalho.

Sem receitas cadastradas e sem busca/filtro ativo, `EmptyStateCard` mostra “Nenhuma receita cadastrada” e uma explicação curta. A única ação de criação permanece no “+” da barra. Busca, filtros e seção de lista são omitidos nesse contexto. O resumo de compra só aparece quando há compra programada ou falha de consulta; uma biblioteca vazia ainda pode mostrar uma compra de medicamento avulso existente.

Busca ou filtro sem resultado é um estado diferente: mantém os controles, informa que nenhuma receita foi encontrada e oferece “Limpar busca e filtros”, sem alterar a ordenação selecionada. Erros de carregamento não são apresentados como biblioteca vazia.

`ui/navigation/TextNavigationBar.kt` apresenta abas com ícones e texto e ação contextual na mesma superfície flutuante. A barra flutuante mostra Início, Receitas e Remédios; a aba Menu foi removida por solicitação do usuário. A rota Menu existente continua no app e aguarda reorganização no restante da migração. O rótulo “Remédios” abre Medicamentos. O “+” tem descrição “Nova receita” ou “Novo medicamento”; `MainScreen` abre respectivamente o wizard completo ou `STANDALONE_MEDICATION`. Em fonte ≥1,5× as abas ficam em duas linhas e a criação em uma linha adicional, uma adaptação do projeto para manter alcance e leitura. Seleção usa uma superfície azul suave atrás do ícone, peso e estado semântico, além da cor; com fonte ampliada mantém também o sublinhado. Os três ícones vetoriais exclusivos da navegação têm 24 dp, traço de 1,8 unidades e terminações arredondadas (escolhas do projeto) e não repetem descrições de acessibilidade dos rótulos. A barra é sobreposta à lista em um Box, sem faixa de fundo externa. A lista recebe a altura medida da barra como reserva em seu contentPadding inferior: o conteúdo aparece ao redor da cápsula durante a rolagem e o último cartão pode subir completamente acima dela. Snackbar também respeita essa reserva. Nesta etapa ela aparece em Receitas e Medicamentos; as demais rotas aguardam migração visual.

- Cabeçalho fica na área superior do Scaffold e condensa quando o gesto de rolagem avança mais de 8 dp. Um gesto na direção de retorno, estando no topo, o expande novamente se houver espaço disponível. O limiar de 8 dp é escolha do projeto. O estado expandido/compacto usa `rememberSaveable`, sem derivá-lo das mudanças de altura causadas pelo próprio cabeçalho, evitando oscilação em listas curtas. `rememberLazyListState` mantém a posição em restaurações suportadas pelo Compose. Esta transição discreta é uma adaptação do projeto; não reproduz a animação proprietária Samsung.
- Próxima compra considera medicamentos de compra própria com acompanhamento ativo e data calculada pelo domínio. Escolhe a data mais antiga, inclusive vencida, para não esconder pendências. Não altera cálculo de ciclos ou registra compras automaticamente. O cartão abre o detalhe do medicamento. Falha nessa consulta aparece separadamente da lista de receitas, com nova tentativa.
- Busca, limpeza, todos os filtros, ordenação, histórico, abertura de detalhe, cadastro, carregamento, erro e estado vazio continuam disponíveis.
- Títulos, profissionais, notas e rótulos de status podem ocupar várias linhas. Status fica abaixo do título, evitando disputa horizontal em telas pequenas. Filtros têm rolagem horizontal. O conteúdo rola, inclusive busca e próxima compra. Busca sem resultado oferece “Limpar busca e filtros”, restaurando a lista completa sem modificar a ordenação escolhida.
- Não depender só de cores para validade e pendências. Usar textos de status e descrições de acessibilidade nas ações por ícone.

## Medicamentos

`MedicationsScreenContent` usa a mesma estrutura de Receitas. Busca por nome, princípio ativo ou concentração e filtro por origem continuam conectados ao ViewModel existente. O valor interno “TODOS” foi preservado e seu rótulo visual é “Todos”. Não há FAB sobre a lista.

Os cartões mantêm nome, concentração ou forma, princípio ativo, local de obtenção, quantidade, posologia, última retirada, próxima retirada, erro de acompanhamento e estado arquivado. A ação secundária “Registrar retirada” usa raio e alvo centralizados. Ela abre o `PickupDialog` existente, mantendo escolha de data, confirmação de antecipação e ciclo esperado. O diálogo e snackbar desta rota herdam o tema compartilhado; a persistência e os cálculos de reposição não foram alterados.

Biblioteca vazia mostra uma explicação e o “+” da barra continua abrindo o cadastro avulso. Busca/filtro sem resultado mantém controles e oferece limpar ambos. Carregamento e falhas continuam distintos do vazio. A navegação flutuante é usada tanto na rota Receitas quanto em Medicamentos; as demais rotas aguardam migração.

## Próximas etapas

1. Migrar Detalhes para os mesmos tokens e componentes, mantendo registro de retirada, histórico e edição.
2. Reformular o cadastramento: apresentar etapas por assunto, instruções curtas, campos relacionados agrupados, ação principal próxima da base, retorno previsível e revisão editável. Preservar rascunho, validação, renovação e persistência. A sequência concreta deve ser definida ao inspecionar o fluxo atual; não atribuir à Samsung um número de etapas inventado.
3. Migrar demais superfícies necessárias à consistência e remover padrões visuais antigos sem uso. Usar um único tema global.
4. Verificar telas claras/escuras, 320 dp, texto ampliado, conteúdo longo, teclado, erros e navegação completa.

## Prévia e validação

As prévias Compose das duas listas incluem claro, escuro e 320 × 640 dp com fonte 2×. `ReceitasPreviewTest` captura a implementação real com dados fictícios, verifica rolagem até um título longo, recolhimento do cabeçalho, recuperação da busca e callbacks de cadastro, detalhe de receita e compra. `MedicamentosPreviewTest` verifica também cadastro avulso, origem, abertura de medicamento, limpeza de busca/filtro e a confirmação de retirada antecipada com o ciclo esperado. As capturas e callbacks de teste não inserem dados no banco. Testes unitários verificam também que recarregar mantém uma única assinatura de cada repositório.

Validação da migração para Medicamentos: `:app:testDebugUnitTest` (39 testes), `:app:assembleDebug`, `:app:lintDebug`, oito testes de `ReceitasPreviewTest` e sete de `MedicamentosPreviewTest` passaram. Em `app/build/outputs/medicamentos-preview/` estão `light.png`, `dark.png`, `empty.png`, `no-results.png`, `font-200.png`, `font-200-list.png` e `receitas-top.png`, além dos relatórios XML das duas rodadas de instrumentação. A rolagem com fonte ampliada mantém acesso à busca, aos títulos longos e às ações. Os testes verificam uma única ação no estado vazio e preservação dos callbacks e da confirmação de antecipação. Não constituem uma auditoria completa de TalkBack ou de todos os cenários de teclado. A migração de Detalhes e Cadastro continua fora desta etapa.

O build ainda informa avisos preexistentes sobre versões de KSP/Kotlin e APIs Hilt/Compose obsoletas; não impediram as verificações e não foram alterados como parte do design.

### Barra flutuante com criação integrada

Refinamento posterior: removida a faixa opaca externa da barra, que recortava visualmente a lista; adotada sobreposição com reserva dinâmica ao fim da rolagem e três ícones vetoriais arredondados. Menu foi retirado da barra flutuante. Build, lint e sete testes de Medicamentos passaram, incluindo uma verificação dos limites da última ação acima da cápsula em 320 dp com fonte 200%. Capturas desta versão em `app/build/outputs/floating-icons-preview/`.

Os oito testes de Receitas também passaram nesta versão, cobrindo os dois temas, estado vazio, conteúdo longo, fonte ampliada, navegação e recuperação de busca.

Os dois testes de `CreationNavigationTest` passaram no app completo: o “+” em Receitas abre o cadastro de receita; em Medicamentos abre o cadastro avulso. Nenhum registro é salvo por esses testes.

A rodada da barra integrada passou em build, lint e 39 testes unitários. Os sete cenários de Medicamentos passaram; em Receitas, sete passaram na rodada completa e o cenário de fonte 200% passou na repetição após corrigir o alvo da rolagem no teste. As capturas atuais estão em `app/build/outputs/navigation-preview/`, com as duas listas, seus estados vazios, temas escuros e títulos longos em fonte ampliada. O componente de navegação compartilha o tema da lista. `NavRoute.bottomNavItems` é calculado no acesso para evitar referências nulas por inicialização circular dos objetos de rota.

### Limpeza após a migração das listas

Removidos o cartão antigo RecordCard, sem consumidores após a adoção de ContentCard, a paleta Catppuccin sem referências e 12 textos obsoletos identificados pelo Android Lint. O tema hospedeiro e AppNavigationBar continuam necessários nas rotas ainda não migradas. Esta limpeza não altera o estágio da migração, os dados ou as regras de negócio.

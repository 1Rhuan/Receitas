# Histórico do design do Receitas

Decisões e validações anteriores, preservadas em 14/10/2026. As descrições de “padrão aprovado”, “vigente” e “mais recente” abaixo refletem cada rodada e podem ter sido substituídas. Para implementar, siga [DESIGN.md](DESIGN.md). Os nomes de receitas no detalhe do medicamento foram substituídos por quantidade e acesso à lista filtrada.

## Padrão aprovado: listas agrupadas — 14/10/2026

**Vínculo no detalhe do medicamento:** a tela observa medicamentos e receitas juntos. Em Dados do medicamento, “Receitas vinculadas” mostra os nomes dos documentos que contêm o item, inclusive quando não há receita selecionada para o acompanhamento de retiradas. A linha “Receita para retiradas” aparece somente quando existe seleção explícita e mostra o nome daquele documento; não usar “Sem vínculo” para descrever participação em receitas. Um medicamento pode constar em mais de uma receita, e a receita de acompanhamento pode ser outra. Renomear ou remover um documento atualiza a tela pela observação dos repositórios. Nenhum vínculo de retirada é criado automaticamente; persistência, validade e regras de retirada permanecem intactas. Esta é uma decisão de conteúdo do projeto.

Validação do vínculo: build passou, 43 testes unitários passaram e dois cenários instrumentados passaram no emulador. Os testes cobrem participação sem receita de retirada, documentos distintos para cada relação, renomeação/remoção observadas e o cadastro completo abrindo o medicamento pela receita. Captura em tema escuro, 320 × 640 dp e fonte 200%, inspecionada em `app/build/outputs/medication-recipe-link/`. O fluxo integrado usa e limpa somente suas fixtures de demonstração.

**Ações da receita junto à identidade:** o detalhe da receita também usa `DetailIdentityHeader`, com botões tonais de lápis e lixeira junto ao nome, e Reativar quando arquivada. A edição deixa de ser um botão amplo e o grupo separado de gerenciamento é retirado; Situação passa às linhas de Dados da receita. Registrar nova receita e o acesso à lista de medicamentos mantêm suas funções. Com fonte ampliada, os ícones ficam acima do título, usando o mesmo componente do medicamento.

Receita e medicamento compartilham `RemovalChoiceDialog`. A lixeira da receita oferece Arquivar ou Excluir definitivamente e preserva as confirmações existentes. As explicações respeitam as regras distintas: arquivar a receita encerra os lembretes/acompanhamentos vinculados; excluir o documento preserva acompanhamentos e retiradas sem o vínculo. Receitas arquivadas ou substituídas não oferecem arquivamento novamente; receitas substituídas também não oferecem edição ou reativação. Esta composição é decisão do projeto e substitui o antigo grupo de gerenciamento, sem alterar persistência ou regras de negócio.

Validação: build do app e dos testes passou, assim como cinco cenários instrumentados no emulador. Cobrem ações da receita, edição, renovação, cancelamento/confirmação de arquivamento e exclusão, reativação e regressão do diálogo compartilhado de medicamento. Capturas claro/escuro inspecionadas em `app/build/outputs/recipe-header-actions/`, com tela de 320 × 640 dp, nome extenso e fonte 200%. Os callbacks destrutivos são de demonstração e não removem dados reais. TalkBack continua pendente.

**Ações do medicamento no topo:** o usuário aprovou o detalhe com timeline e pediu ações no cabeçalho, com ícones. Editar (lápis) e remover (lixeira) substituem o botão de edição e o menu dentro da rolagem. As ações possuem descrições acessíveis e alvos dos tokens compartilhados. Com fonte ampliada ficam em uma segunda linha do cabeçalho para não comprimir o título. Para acompanhamento arquivado, o cabeçalho oferece também Reativar.

Correção após o usuário informar que os ícones não apareciam no conteúdo: as ações foram movidas da barra de navegação superior para `DetailIdentityHeader`, junto ao nome do medicamento, em botões com preenchimento tonal discreto. Em fonte normal ficam ao lado do nome; com fonte ampliada ficam acima dele para não desaparecer após nomes muito extensos. O cabeçalho de navegação conserva Voltar e título da tela. Não há duplicação de ações. A imagem enviada começava pelo conteúdo; não foi possível confirmar a versão instalada no Samsung, que não estava conectado nesta rodada. A validação usa o emulador.

A lixeira abre uma escolha explícita: Arquivar preserva o histórico e encerra lembretes; Excluir definitivamente remove acompanhamento e retiradas, sujeito às regras existentes. A seleção leva às confirmações existentes, e nenhum callback destrutivo ocorre ao abrir a escolha ou ao cancelá-la. Para itens já arquivados, não repetir a opção de arquivar. Edição, reativação, bloqueios de exclusão e mensagens permanecem nos callbacks/repositórios atuais. Essa composição é decisão do projeto e substitui o menu de ações do detalhe do medicamento, sem mudar os menus de cada evento da timeline.

Validação: build do app/testes e três cenários instrumentados passaram, cobrindo edição, arquivamento com cancelar/confirmar, exclusão permanente com cancelar/confirmar e reativação, nos dois temas e em 320 × 640 dp com fonte 200%. Captura inspecionada em `app/build/outputs/medication-header-actions/`. Testes executam callbacks de demonstração, sem exclusão de dados reais.

**Detalhe ampliado e timeline:** o usuário pediu que a seleção do medicamento dê acesso a uma visualização mais detalhada e propôs histórico em timeline. A tela única agora apresenta dados do medicamento (forma, princípio ativo e orientações existentes), acompanhamento e uma prévia somente de leitura das três últimas retiradas. O acesso ao histórico abre a timeline completa, ordenada da mais recente para a mais antiga, com correção/exclusão no menu e confirmações preservadas. `TimelineEntry` compartilha marcador/conector e usa apenas tokens do sistema; as datas e textos comunicam a cronologia também sem depender da linha/cor. A próxima previsão permanece no grupo de acompanhamento, separada dos eventos realizados. A lista da receita continua como resumo, com forma e concentração como apoio ao nome. Não são inventadas quantidades ou locais históricos e nenhuma informação existente é descartada.

Validação da timeline: build passou e três cenários instrumentados passaram, cobrindo dados adicionais, prévia limitada a três eventos, acesso a todos os eventos no histórico, cancelamento/confirmação da exclusão, arquivamento e retorno em 320 × 640 dp nos temas claro/escuro e fonte 200%. Capturas inspecionadas em `app/build/outputs/medication-timeline-preview/`. A linha não escala o tempo proporcionalmente; conecta eventos ordenados. Não representa doses tomadas, apenas retiradas registradas.

**Fluxo vigente:** o usuário pediu retirar os medicamentos da página principal da receita. O acesso único “Ver medicamentos”, com contagem, abre **Medicamentos da receita**, uma lista própria identificada pelo nome do documento. A lista usa somente os itens daquela receita, preservando concentração, orientação e quantidade prescritas. Selecionar um medicamento continua navegando à mesma rota `medication_detail/{medicationId}` usada em Remédios. Voltar retorna à lista e depois à receita. O estado da subvisualização é restaurável e a rolagem do detalhe da receita é preservada.

A tela única de medicamento passou ao vocabulário aprovado: nome/concentração no topo, acompanhamento em linhas agrupadas, botão “Editar acompanhamento”, gerenciamento no botão “Ações” e confirmações existentes. Registrar retirada continua disponível somente para acompanhamento recorrente não arquivado; histórico permanece disponível em todos os estados. “Receita para retiradas” descreve a configuração do acompanhamento, sem confundi-la com a participação do medicamento em um documento. Não houve novo vínculo ou alteração de regras/dados.

Referências revisitadas: [One UI Lists](https://developer.samsung.com/one-ui/comp/list.html) recomenda nomes curtos nos acessos e deixar o conteúdo explicativo na página de destino; [Android Common layouts](https://developer.android.com/design/ui/mobile/guides/layout-and-content/common-layouts?hl=en) descreve lista e detalhe separados em telas compactas. A contagem, o acesso, o agrupamento e a separação desta receita são decisões do Receitas. Não foi implementado layout de duas colunas para tablets nesta rodada. As decisões anteriores abaixo são histórico, substituídas por este fluxo.

Validação: build do app e testes passou; cinco cenários instrumentados passaram juntos, verificando seleção do medicamento correto, ausência da lista na página principal da receita, lista vazia, ida/volta pelas três telas, callbacks de edição, arquivamento com cancelar/confirmar, histórico e exclusão, temas claro/escuro e 320 × 640 dp com fonte 200%. Capturas finais inspecionadas em `app/build/outputs/recipe-medication-flow-preview/`. Os testes usam dados de demonstração e removem suas fixtures; não alteram dados reais. Persistem avisos preexistentes de dependências/deprecações. TalkBack continua pendente.

**Refinamento vigente para medicamentos da receita:** após revisar a extensão do acompanhamento, o usuário pediu um resumo compacto e confirmou que abrir um medicamento deve usar a mesma tela acessada por Remédios. O detalhe da receita agora apresenta um cartão por medicamento, com nome destacado, concentração e dados prescritos quando existentes. Toque no cartão ou no botão “Ver medicamento” navega para `medication_detail/{medicationId}`. O acompanhamento completo e o histórico ficam na tela única de medicamento. As listas agrupadas ficam para dados da receita e histórico; não expandir uma tabela de acompanhamento sob cada medicamento da receita. Esta decisão substitui a extensão `MedicationPickupSection` descrita abaixo; o componente e seus testes específicos sem consumidores foram retirados.

Pertencer à receita (`medicationIds`/itens prescritos) e selecionar uma receita para validar retiradas (`linkedPrescriptionId`) são relações diferentes no modelo. A interface compacta não apresenta “sem vínculo” como se o medicamento não pertencesse à receita aberta. Corrigida a verificação de acompanhamento para consultar todas as receitas carregadas, em vez de apenas a receita aberta, evitando ausência de vínculo falsa quando ele aponta para outro documento. Nenhum vínculo é criado automaticamente e as regras de validade/retirada são preservadas.

Validação: build do app e dos testes passou; 41 testes unitários passaram, incluindo a distinção entre pertencer a uma receita e acompanhar retiradas com outra receita explicitamente vinculada. Dois testes instrumentados passaram: cartão/botão selecionam o medicamento correto em 320 × 640 dp, tema escuro e fonte 200%; fluxo completo de cadastro abre a rota de medicamento existente, permite acessar histórico e retorna à receita. Captura inspecionada em `app/build/outputs/compact-medication-preview/`. As orientações e quantidades da receita continuam no resumo; a tela completa usa o cadastro atual do medicamento, como em Remédios. Não houve modificação de dados reais.

O usuário escolheu **Listas agrupadas** após comparar as propostas em `docs/design-standard-proposal.md`. Esta decisão prevalece sobre as experiências anteriores de cartões por retirada, pares de ações e cartões de gerenciamento descritas no histórico abaixo. One UI é a referência visual principal; Material Compose é a base de implementação. Apple e Carbon são referências comparativas de hierarquia, não identidades visuais adicionais.

- Dados em linhas consistentes dentro de grupos com título externo. Rótulo e valor em duas colunas com reflow vertical para fonte ampliada; textos extensos permanecem completos.
- Cartões ficam reservados a resumos e unidades autônomas. Não criar um cartão ou botão amplo por informação comum.
- Uma ação principal destacada por contexto; ações secundárias como botões reconhecíveis. Gerenciamento e ações destrutivas no botão “Ações”, com confirmações existentes.
- Histórico como lista contínua: data de retirada, dia da semana e registro no aplicativo como apoio; Corrigir/Excluir no menu. Total no subtítulo do medicamento, sem cartão grande de introdução.
- Usar componentes, cores, espaçamentos, raios e tipografia centralizados. Mudanças recorrentes passam pelo sistema, não por estilos por tela.
- Manter a barra flutuante com Início, Receitas e Remédios, sem criação e com transparência externa/reserva medida. Preservar dados e regras de negócio.

**Piloto implementado:** detalhe da receita e histórico compartilhado. `GroupedSection`, `GroupedValueRow` e `ContextActionsButton` estabelecem o vocabulário. Receita apresenta identidade/validade, dados agrupados, edição/renovação e situação/gerenciamento; o histórico usa linhas lazy com chaves estáveis e menus. Os demais detalhes, listas e cadastro ainda precisam de migração de composição após avaliar este piloto; compartilhar tokens não significa que todas as telas já adotaram listas agrupadas. As descrições anteriores abaixo permanecem como histórico de decisões.

**Extensão aprovada:** o bloco de medicamento no detalhe da receita usa `MedicationPickupSection`. Nome e concentração identificam o item; dados prescritos aparecem somente quando preenchidos. O acompanhamento agrupa situação, última retirada, intervalo, próxima previsão, quantidade por retirada, local e ausência de vínculo em linhas identificadas. Alertas de receita vencida ou que vence antes da retirada permanecem explícitos. Registrar retirada é a ação principal, disponível somente com recorrência ativa e não arquivada; histórico é secundário e permanece acessível. Não foram alterados datas, intervalos, dados ou callbacks. O componente é compartilhado para futuras migrações, sem criar valores de estilo locais.

Validação dessa extensão: build passou e três testes instrumentados passaram, cobrindo datas/intervalo preservados, callbacks das ações, acesso pelo detalhe da receita, acompanhamento arquivado, conteúdo longo e fonte 200% em 320 × 640 dp. Os dois cenários do componente foram repetidos com o tema real do app para corrigir o hospedeiro das capturas. Imagens em `app/build/outputs/grouped-pickup-preview/`, inspecionadas nos dois temas. Os avisos preexistentes de dependências permanecem.

Validação do piloto: build do app e testes passou; quatro cenários instrumentados passaram juntos, cobrindo acesso aos menus, cancelamento/confirmação de exclusão, retorno, renovação/arquivamento, dados agrupados, nomes extensos, 320 × 640 dp e fonte 200%, em claro e escuro. Capturas inspecionadas em `app/build/outputs/grouped-lists-preview/`. O componente de pares de ações sem consumidores foi retirado para não manter um padrão concorrente. A navegação e as regras de persistência permanecem; correção continua usando o seletor de data existente. TalkBack permanece pendente.

## Referência complementar: cadastro do Pillo

O usuário autorizou registrar a referência observada no Pillo para Android em 14/10/2026. O relatório e as capturas estão em [docs/pillo-research.md](docs/pillo-research.md). Foram inspecionados nome, forma, frequência, horário e dose do cadastro de medicamento; consultas ainda não foram analisadas. A referência orienta hierarquia, escolhas e apresentação progressiva, preservando os componentes, tokens e regras do Receitas. Trata-se de pesquisa de um aplicativo de terceiro e de propostas para o projeto, não de especificações verificadas Samsung nem de uma migração já implementada.

## Status e escopo

### Histórico de retiradas em tela própria

Refinamento após as capturas: os cartões agora distinguem data da retirada (com dia da semana) e data/hora de registro no aplicativo, usando `occurredOn` e `recordedAt` existentes. A data de registro usa o fuso do dispositivo; valores ausentes/não positivos não são apresentados. O cabeçalho identifica o medicamento e o total de retiradas em um cartão de resumo. Quantidade, local e outras informações atuais não são atribuídos retroativamente a eventos que não armazenam esses dados.

“Corrigir” e “Excluir” ficam lado a lado com `DetailActionPair`, componente compartilhado que empilha as ações quando a fonte está ampliada. O mesmo padrão organiza Arquivar/Reativar e Excluir definitivamente no detalhe da receita. A receita passa a ter uma seção “Dados da receita”, divisórias, emissão/tipo/vencimento com rótulos, e um cartão “Gerenciar receita” com situação e botões, substituindo o texto azul solto. Datas, confirmações e regras permanecem iguais. Essas composições são decisões do projeto alinhadas ao agrupamento lógico One UI, não especificações numéricas da Samsung.

Build passou. Quatro cenários cobriram histórico, confirmações de exclusão, retorno, alinhamento horizontal e dados/ações de receita em 320 × 640 dp e fonte 200%; o cenário de gerenciamento passou após ajustar o teste para rolar até o botão. Capturas inspecionadas em `app/build/outputs/organized-details-preview/`. As capturas de demonstração usam datas de registro fictícias realistas, sem alteração de registros reais.

O usuário pediu melhorar a apresentação do histórico e autorizou movê-lo para uma visualização separada. Nos detalhes de receita e medicamento, o histórico extenso foi substituído por um botão com contorno “Ver histórico de retiradas”. A mesma tela compartilhada abre o histórico do medicamento selecionado, com nome completo, estado vazio e cartões de retirada em ordem decrescente de data. A lista usa composição lazy e chaves de evento. Voltar pelo cabeçalho ou pelo sistema retorna ao detalhe com sua posição de rolagem preservada; a seleção da visualização é restaurável.

O usuário prefere botões, mesmo discretos, a ações apresentadas como texto destacado. “Ver histórico”, “Corrigir” e “Excluir” usam `SecondaryActionButton`, tokens e temas compartilhados. Datas, seleção de correção, confirmações, callbacks, mensagens e recálculo da próxima previsão mantêm o comportamento existente. Não houve migração de dados nem mudança das regras de acompanhamento. A tela separada é uma subvisualização dos detalhes e continua usando o mesmo estado observado do medicamento, sem nova assinatura de repositório.

Esta composição é decisão do Receitas, seguindo o agrupamento lógico e a hierarquia descritos nas referências One UI registradas abaixo. Validação: build do app e testes passou; os três cenários de acesso pelo detalhe da receita, histórico vazio, fonte 200%, retorno e confirmação/cancelamento da exclusão passaram (o vazio após ajustar a correspondência ao texto com ponto final). Capturas claro/escuro em `app/build/outputs/pickup-history-preview/`, inspecionadas em 320 × 640 dp com nome longo. O teste de exclusão usa callback de demonstração e não remove registros reais. A correção reutiliza o seletor de data e confirmação existentes; seu diálogo nativo não foi exercitado nesta rodada. TalkBack permanece pendente.

### Entrada do cadastro de receita — refinamento One UI

A primeira etapa passa a separar **Emissão e tipo** de **Validade da receita**, em cartões do sistema. Nome/profissional e preferências de renovação continuam opcionais; emissão, tipo, validade em dias ou data, seleções e validações preservam seus callbacks e dados. Não há mudança de persistência ou de contagem de etapas.

O formulário reutiliza `PageHeader`: na entrada de receita, quando a altura disponível permite e a fonte não está ampliada, título e descrição ficam centralizados na área de visualização. O cabeçalho faz parte da rolagem. Em pouca altura, com teclado ou fonte ampliada, usa tipografia compacta e alinhamento inicial para manter a tarefa acessível. Títulos expandidos com quebra de linha recebem alinhamento central também nas demais telas que reutilizam esse componente. A ação “Continuar para medicamentos” permanece na base, com altura pelo conteúdo e os tokens compartilhados.

Após a revisão das capturas, o usuário pediu transparência atrás da ação inferior. Removida a Surface tonal e adotada sobreposição do botão, com transparência externa e altura medida reservada no fim da rolagem. O formulário pode passar atrás das margens da ação, em vez de ser recortado por uma faixa opaca. Os testes de toque rolam as opções para cima da ação antes de acioná-las; não presumem que o limite inferior do viewport inteiro esteja livre. A área interna do botão mantém a cor primária e seu contraste. Esta composição é decisão do projeto.

Validação: build do app e dos testes passou. Os sete cenários de `DetailAndFormDesignTest` e o fluxo de criação completo passaram com a sobreposição; renovação e preservação de histórico passaram na repetição após adequar o toque à área livre acima do botão. A versão anterior, antes da sobreposição, passou os nove cenários juntos. Inspecionadas capturas em `app/build/outputs/recipe-entry-one-ui/`, incluindo tema claro, tema escuro, 320 × 640 dp e fonte 200%. TalkBack ainda não foi auditado nesta rodada. Persistem os avisos preexistentes de dependências e deprecações.

Fundamento verificado: [Basic layout](https://developer.samsung.com/one-ui/layout/basic.html), revisitado nesta rodada, descreve título na área superior de visualização, texto centralizado e agrupamento lógico dos controles na área de interação; [App bar](https://developer.samsung.com/one-ui/comp/app-bar.html) orienta hierarquia expandida/compacta. A divisão em dois cartões, os limiares adaptativos e as dimensões adotadas são decisões do Receitas. Esta etapa refina a entrada; não representa uma nova implementação de todo o fluxo.

As listas compartilham o sistema aprovado. Nesta rodada, o tema foi elevado à raiz e Detalhes e Cadastro passaram a usar a mesma tipografia, paleta e componentes. O usuário apontou especificamente falta de clareza no **cadastro de receita ou remédio**, que passou a ser o foco do refinamento. As regras de persistência, renovação e acompanhamento foram preservadas; a configuração de lembretes recorrentes ganhou uma etapa condicional após autorização do usuário.

**Decisão mais recente da barra:** o usuário pediu remover a ação de adicionar da barra. A cápsula mostra somente Início, Receitas e Remédios e ocupa a largura disponível entre as margens compartilhadas. O botão “+” e seus tokens exclusivos foram retirados. A transparência externa e a medição da altura para reservar espaço no fim das listas permanecem. A referência One UI orienta consistência, hierarquia, cores e acessibilidade conforme as fontes abaixo; esta composição da barra é uma decisão do projeto. Esta decisão substitui as experiências anteriores com criação integrada ou botão separado descritas no histórico. A ação de criar fica no início do conteúdo rolável de cada página, logo abaixo do cabeçalho: “Nova receita” e “Novo medicamento”. As duas listas usam ActionButton no RecordListScaffold, com margens, cores, tipografia e raio compartilhados. A ação aparece também nos estados vazios, sem duplicação no cartão de orientação. Os callbacks existentes abrem receita e medicamento avulso respectivamente. Esta posição é decisão do projeto inspirada na organização e agrupamento One UI, não uma medida prescrita pela Samsung.

## Referências consultadas em 14/10/2026

Validação da criação nas páginas: build do app e dos testes passou. Dos 17 cenários de navegação e listas, 16 passaram na primeira rodada; o cenário de Medicamentos em 320 × 640 dp e fonte 200% passou na repetição após trocar a rolagem por índice pela localização por texto, pois a ação adicionou um item à lista. Os testes verificam abertura dos dois cadastros, callbacks, ação única no vazio, temas, conteúdo longo e última ação acima da barra. Capturas inspecionadas em `app/build/outputs/page-creation-actions/`. As verificações da rodada anterior abaixo descrevem a versão temporária sem ação de criação nas páginas.

Validação da retirada do botão de criação: `:app:assembleDebug` e `:app:assembleDebugAndroidTest` passaram. Os 17 testes de `CreationNavigationTest`, `ReceitasPreviewTest` e `MedicamentosPreviewTest` passaram no emulador, cobrindo navegação, listas, temas, estados vazios, conteúdo longo e 320 × 640 dp com fonte 200%. As capturas inspecionadas estão em `app/build/outputs/navigation-without-create/`. Com fonte 200%, a cápsula distribui as abas em duas linhas pelo padrão adaptativo existente. O build mantém os avisos preexistentes de KSP/Kotlin e APIs obsoletas.

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

O tema global `ReceitasTheme` usa a paleta e a tipografia de `DesignSystem.kt`, sem cores dinâmicas. `DesignTheme` reutiliza esse mesmo tema para prévias isoladas. A paleta hospedeira anterior, a tipografia Poppins e a antiga barra de navegação foram retiradas do código. Início também recebe a barra flutuante e reserva sua altura medida; seu “+” abre uma receita.

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

## Próximas etapas registradas antes desta rodada

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

## Refinamento de Detalhes e Cadastro — 14/10/2026

### Clareza do cadastro

A escolha do usuário foi melhorar a compreensão do cadastro. O percurso base mantém dados da receita, medicamentos, frequência de retirada e revisão; avulsos começam pelo medicamento. Havendo retirada recorrente, uma etapa adicional separa antecedência, local e vínculo de receita. Assim, receita tem quatro ou cinco etapas e avulso três ou quatro. A contagem é uma decisão do projeto, autorizada pelo usuário, e não uma regra Samsung. A validação de data e intervalo ocorre na frequência; a de antecedência ocorre nos lembretes; a validação completa continua obrigatória antes de salvar.

- A primeira etapa destaca emissão, tipo e validade. O tipo é escolhido em uma lista aberta sob demanda. Validade em dias e data de vencimento são alternativas de entrada, sem dois campos concorrentes.
- Nome personalizado e profissional ficam em um grupo opcional expansível, já aberto quando esses dados existem. Ajustes do lembrete de renovação ficam em seu grupo, preservando os valores e mostrando o resultado na revisão.
- A ação principal informa o próximo assunto: “Continuar para medicamentos”, “Continuar para lembretes”, “Revisar cadastro” e “Salvar”. Continuar inclui o medicamento em edição; não é necessário confirmar “Adicionar” antes. Em receitas, “Adicionar outro medicamento” mantém a inclusão de vários itens. Edição pela revisão continua retornando diretamente à revisão.
- Em retiradas, a decisão de acompanhar aparece antes da configuração. Data e intervalo ficam na etapa de frequência; antecedência, local e vínculo ficam na etapa condicional seguinte. Quantidade continua disponível também sem recorrência.
- Campos, grupos opcionais, seleção, alternância e seções de revisão usam componentes em FormComponents.kt e DetailComponents.kt. A área inteira do controle de alternância é acionável. Erros gerais são anunciados e trazidos ao início da área rolável; erros numéricos têm instrução junto ao campo.
- O botão principal usa altura mínima, não fixa, e o formulário respeita o teclado. Títulos e ações da revisão ficam em linhas separadas para não comprimir nomes extensos.

Essas são adaptações do projeto apoiadas na hierarquia, agrupamento e alcance descritos em [Basic layout](https://developer.samsung.com/one-ui/layout/basic.html) e [App bar](https://developer.samsung.com/one-ui/comp/app-bar.html), revisitados nesta rodada. A barra flutuante com botão separado é preferência do usuário, não uma reprodução documentada de um componente Samsung.

### Detalhes e consistência

Detalhes usam cabeçalho compartilhado com altura pelo conteúdo, cartões e ações do sistema. O detalhe da receita apresenta validade, emissão, profissional, tipo e notas sem a antiga composição de painéis aninhados. O detalhe do medicamento agrupa dados, ações e histórico. Renovação, edição, exclusão, arquivamento, retirada e correção de histórico mantêm seus callbacks e confirmações. O histórico distribui data e ações verticalmente para fonte ampliada. Foi corrigida a sobreposição entre o status e as ações de arquivamento no detalhe da receita.

### Limites e acompanhamento

A adoção do tema global não equivale a uma auditoria completa das superfícies: os componentes específicos de Início, Notificações e Menu ainda precisam de revisão de composição e acessibilidade. A prioridade desta rodada, conforme esclarecimento do usuário, foi o cadastro. Validação manual com TalkBack continua pendente. Os registros anteriores de validação descrevem versões anteriores; os resultados desta rodada constam abaixo.
### Validação desta rodada

- `:app:assembleDebug`, `:app:lintDebug` e 40 testes de `:app:testDebugUnitTest` passaram. O novo teste cobre a etapa condicional, a validação da antecedência no momento correto e o retorno à revisão após desativar a recorrência.
- No emulador Pixel 8/API 37, passaram sete cenários de Medicamentos; dois fluxos completos de cadastro/renovação; e os 18 cenários de `DesignMigrationSuite` (Receitas, navegação contextual, textos de revisão e seis cenários de detalhes/formulários). Na suíte, 17 passaram juntos; a busca de Receitas passou isoladamente após substituir o fechamento de teclado dependente do foco da janela no teste. A mesma correção estabilizou os fluxos completos. Nenhuma regra de produção foi relaxada para esses ajustes de teste.
- Verificados 320 × 640 dp, fonte 200%, nomes longos, temas claro/escuro, ações de renovação e arquivamento, edição por revisão, inclusão de vários medicamentos, recriação da Activity e preservação do histórico na renovação. As capturas finais dos novos cenários estão em `app/build/outputs/design-final-captures/`; relatórios preservados em `app/build/outputs/design-migration/`. As primeiras capturas do sistema ficaram vazias no emulador; foram substituídas por capturas diretas da árvore Compose e inspecionadas visualmente.
- Com fonte ampliada, o cabeçalho do cadastro prioriza a indicação de etapa; o título da tarefa continua na área rolável, liberando espaço para campos e teclado. TalkBack e revisão completa de todas as superfícies continuam pendentes.
- Persistem avisos de versões/deprecações das dependências e recursos sem uso; lint terminou sem erros. As alterações preexistentes em `.idea` foram preservadas.

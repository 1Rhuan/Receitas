# Pendências, divergências e dívidas

Registro do que está em aberto no projeto em 15/10/2026. Cada item diz o que foi verificado, o risco e a próxima ação. Itens resolvidos devem ser removidos daqui e, se relevante, registrados em `DESIGN-HISTORY.md`.

## Resumo

| ID | Tema | Tipo | Precisa do autor? |
| --- | --- | --- | --- |
| [D2](#d2) | Textos do cadastro e do DESIGN que o código já substituiu | Documentação | Não (corrigido) |
| [D3](#d3) | Itens do cadastro ainda sem decisão | Decisão | Sim |
| [D4](#d4) | Rota e tela Menu sem acesso | Código morto provável | Sim |
| [D5](#d5) | Validações que nunca foram feitas | Risco | Não |
| [D6](#d6) | Publicação, release e licença | Dívida | Sim |
| [D7](#d7) | Infraestrutura de testes instável | Dívida | Não |
| [D8](#d8) | Documentos longos e cronológicos | Dívida | Não |
| [D9](#d9) | Trabalho não commitado | Risco | Sim |
O ID D1 foi retirado porque foi resolvido (veja [Decisões registradas](#decisões-registradas)). Os demais IDs foram mantidos para não quebrar links.

## D2

### Textos substituídos pelo código

Verificados contra `PrescriptionWizardUiState.kt` e corrigidos nesta revisão:

| Texto antigo | Estado real no código |
| --- | --- |
| "Nome da receita permite continuar em branco e usa nome automático" (`cadastro.md`, trechos de `DESIGN.md`) | Nome é **obrigatório**. A validação exibe "Informe o nome da receita." e o primeiro passo **não oferece Pular**. |
| "Pular fica na base, junto de Avançar" (`cadastro.md`) | Pular fica **no topo à direita**, como botão textual neutro. A base tem só a ação principal e o progresso. |
| Tabela de Pular marcando "Nome da receita: Sim" (`cadastro.md`) | Não. Veja acima. |
| Asterisco em campos obrigatórios (`DESIGN.md`, seção "Opcionais no fim") | Substituído por ícone de informação "Obrigatório", sem asterisco. |
| Percurso do cadastro como "nome → emissão/tipo → alertas → profissional e observações → medicamentos → retiradas → revisão" | Os detalhes opcionais (profissional, observações, forma, princípio ativo etc.) ficam **na última tela antes da revisão**, em telas próprias. |

O corpo de `cadastro.md` e `DESIGN.md` continua cronológico. Cada documento agora abre com um resumo vigente. Em caso de dúvida, vale o resumo e, acima dele, o código.

## D3

### Pontos do cadastro que merecem atenção

- **Intervalo de retirada só em dias.** O campo aceita apenas dias. Registros antigos em semanas, meses ou anos mantêm a unidade original até serem editados. Não há migração. Se o autor quiser unificar, é uma mudança de dados e precisa de migração e teste.
- **Vínculo automático na inclusão pela lista da receita.** É a única exceção à regra "não criar vínculo automático". Está aprovada e documentada. Qualquer pedido para ampliá-la precisa de nova aprovação.

## D4

### Rota e tela Menu

`NavRoute.Menu` e `MenuScreen` continuam no código e no grafo, mas a barra filtra o Menu e nenhum outro lugar navega até ele. `NavRoute.bottomNavItems` ainda lista o Menu, e o `MainScreen` usa essa lista para decidir se a barra aparece. `MenuScreen` só oferece um atalho para Notificações, que o Início já oferece pelo sino.

**Risco:** código morto que confunde, e `AGENTS.md` proíbe restaurar o Menu na barra.

**Ação:** confirmar com o autor e remover `MenuScreen.kt`, `NavRoute.Menu` e a rota, em uma mudança isolada, com build e testes de navegação. `auditoria.md` trata declarações de framework como entrada, então confirme que nenhum teste instrumentado referencia a rota antes de apagar.

## D5

### Validações que nunca foram feitas

Nenhum documento comprova estes pontos. Não os trate como verificados.

- **TalkBack**, completo. Há descrições acessíveis nos controles, mas nunca houve percurso com leitor de tela.
- **Tablets e telas grandes** (≥ 600 dp). Não há layout dedicado.
- **Paisagem** e outros fabricantes além do emulador.
- **Aparelho físico.** O Samsung SM-A576B não foi validado nas rodadas recentes. Uma execução sem serial fixo atingiu esse aparelho, com conflitos de instalação e limpeza. Depois, o pacote não foi encontrado nele, e a preservação dos dados locais **não pôde ser confirmada** ([cadastro.md](cadastro.md)).
- **Contraste** de todas as superfícies. Só 12 pares de tokens foram medidos, com o menor em 4,71:1. Estados desabilitados e superfícies legadas ficaram de fora.
- **Entrega periódica de notificações** sob restrição de bateria, e após reinício do aparelho.
- **Restauração de backups antigos** (backup está desativado, então só importa se isso mudar).
- **Teste de usabilidade com usuários finais.** A avaliação foi heurística.
- **Workflow do GitHub Actions** em execução hospedada. O repositório não tem `origin`.

**Ação sugerida:** antes da defesa ou da publicação, executar o roteiro manual de [lembretes.md](lembretes.md#verificação-manual-em-dispositivo-ou-emulador) em um aparelho de teste dedicado e um percurso com TalkBack nas telas principais.

## D6

### Publicação, release e licença

- **Licença:** nenhuma escolhida. O autor precisa decidir antes de aceitar reutilização por terceiros.
- **Repositório remoto:** inexistente. Os passos estão no [README](../README.md#publicar-no-github).
- **Release:** `app/build.gradle.kts` desativa a otimização do build `release` e não define assinatura. O APK de debug usa a chave de desenvolvimento. Uma distribuição real exige chave própria, mantida fora do Git. `versionCode` é 1 e `versionName` é 1.0.
- **Avisos de lint:** quatro avisos de versões mais novas de dependências (Core, Navigation e plugins Kotlin/Compose). Atualizar é uma mudança própria, com nova validação completa.
- **Opção experimental:** `android.disallowKotlinSourceSets=false` em `gradle.properties` gera aviso em todo build. Tentar removê-la quando as dependências permitirem.
- **Java:** `compileOptions` em Java 11, com Gradle em JDK 25. Funciona, mas vale alinhar numa atualização futura.
- **Textos de domínio:** as mensagens de regra de negócio estão em Kotlin, em português. O app não está preparado para outros idiomas.
- **Fiação manual:** `ReminderSynchronizer` instancia repositórios sem Hilt (veja [arquitetura.md](arquitetura.md#notifications)).
- **Boilerplate:** `ExampleUnitTest` e `ExampleInstrumentedTest` são os modelos do assistente do Android Studio e não testam nada do app.

## D7

### Infraestrutura de testes

- Nenhuma execução única da suíte instrumentada completa passou sem falhas de infraestrutura. A rodada de publicação teve 87 cenários, com 82 aprovações e cinco falhas de captura (PixelCopy). Os cinco foram repetidos e passaram, mas isso não equivale a uma execução limpa.
- As rodadas usaram emuladores diferentes (Android 14/API 34 e Android 10/API 29).
- Rodadas recentes validaram só o que mudou. A suíte completa não foi repetida depois das últimas alterações de interface.
- Um emulador chegou a perder serviços do Android no meio de uma rodada.

**Ação:** uma execução completa da suíte, em um emulador único e recém-criado, com serial fixo, e registrar o resultado em `revisao-publicacao.md`.

## D8

### Documentos longos e cronológicos

- `DESIGN.md` (≈48 KB) e `DESIGN-HISTORY.md` (≈51 KB) acumulam rodadas em ordem de data. A decisão vigente de um assunto está espalhada, e algumas seções se contradizem (veja D2 e as marcas **[Substituído]**). O `DESIGN.md` mistura finais de linha CRLF e LF.
- Resultados de testes de rodadas antigas aparecem dentro das seções de decisão.
- Caminhos como `app/build/outputs/publication-review` estão **fora do Git**. As capturas e logs citados nos documentos existem apenas na máquina de quem rodou os testes e podem não estar mais lá.

**Ação sugerida (não feita aqui, para não perder informação):** separar `DESIGN.md` em uma especificação curta por assunto e um registro de validações. Fazer isso com cuidado e em uma mudança própria, conferindo cada decisão contra o código.

## D9

### Trabalho não commitado

Em 15/10/2026, antes desta revisão, o `git status` listava 103 entradas. Elas incluem `README.md`, `.github/workflows/android.yml`, `.gitattributes`, componentes novos (`ActionSheet`, `ContextMenu`, `EmptyCollectionState`, `FloatingListToolbar`, `ListOptionsMenu`, `ReviewComponents`, `OptionalDetailsFlow`), vários testes instrumentados e documentos. O último commit é `c33e729`.

**Risco:** perder trabalho por um `git clean` ou `git checkout` descuidado, e um próximo commit gigante e impossível de revisar.

**Ação:** commitar por assunto (infraestrutura e publicação, componentes compartilhados, wizard, testes, documentação) depois de revisar `git diff`. Não force nada nem descarte arquivos sem conferir.

## Ordem sugerida

1. Commitar o trabalho pendente em grupos (**D9**).
2. Rodar a suíte instrumentada completa em emulador limpo (**D7**).
3. Remover o Menu, se confirmado (**D4**).
4. Escolher a licença e criar o repositório remoto (**D6**).
5. Executar as validações manuais de **D5** que o prazo permitir, começando pelo roteiro de lembretes e pelo TalkBack.
6. Consolidar o `DESIGN.md` (**D8**).

## Decisões registradas

### Botão de criação (antiga D1) — 15/10/2026

**Situação encontrada:** o `AGENTS.md` mandava criar receita e medicamento no início rolável das páginas, sem "+" e sem FAB. O `DESIGN.md` e o código já tinham uma ação "+" contextual junto à cápsula, por esclarecimento explícito do usuário na mesma data.

**Decisão do usuário:** vale o código.

**Resultado:**

- Receitas e Remédios mostram a ação "+" (Criar receita, Criar medicamento) junto à cápsula. No Início ela não aparece.
- Listas preenchidas não têm botão de criação no topo. O estado vazio oferece a ação na orientação central.
- O "+" e a ação central coexistem no estado vazio de Receitas. Se isso for considerado duplicação, é uma decisão de interface nova e precisa de aprovação.
- `AGENTS.md`, `visual.md` e `DESIGN.md` foram alinhados. As frases antigas que ainda dizem "início rolável" no histórico do `DESIGN.md` e em `revisao-publicacao.md` ficaram marcadas como **[Substituído]**.
- Nenhuma alteração de código foi necessária.

## Histórico desta revisão (15/10/2026)

- Criados: `docs/README.md`, `onboarding.md`, `arquitetura.md` e este arquivo.
- Atualizados: resumo vigente no topo de `cadastro.md` e `DESIGN.md`; correção das afirmações substituídas listadas em D2; `AGENTS.md` e `visual.md` alinhados ao código (decisão acima); links no `README.md` da raiz.
- Verificado: `:app:testDebugUnitTest` com 74 testes aprovados. Lint, build dos testes instrumentados e testes instrumentados não foram executados.
- Nada foi removido do registro histórico dos documentos existentes: trechos superados foram marcados, não apagados. Nenhum arquivo de código foi alterado.

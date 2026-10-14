# Separação entre apresentação e lógica

Cada funcionalidade segue: `Route → estado e callbacks → Presentation/Content`.

- `*Route.kt`: coleta estado com o ciclo de vida e conecta callbacks aos ViewModels. As funções públicas HomeScreen, MedicationsScreen e equivalentes mantêm os nomes e parâmetros anteriores.
- `*Screen.kt`: visual, diálogos e efeitos de apresentação. Recebem UiState e callbacks, sem importar ViewModels, repositórios ou APIs de navegação.
- ViewModels: carregamento, salvamento, coordenação de ações e atualização de estado. No cadastro também tratam tipo de receita, validade, cálculo de vencimento, profissionais e medicamentos existentes e conversão do intervalo de retirada.
- UiState e modelos: dados e propriedades derivadas utilizados pelas telas.

Estado local de apresentação (diálogo aberto, seleção de data, expansão de campos e snackbar) fica nos composables. Validações e persistência continuam nas camadas de estado, domínio e dados.

## Onde alterar o design

Edite `*Screen.kt` e `ui/components` para mudar layouts. Cores e tipografia ficam em `ui/theme`.

`MainScreenContent.kt` contém a apresentação externa, e `AppNavigationBar.kt` a barra inferior. `MainScreen.kt` controla a navegação e a abertura de notificações. `ReceitasNavHost.kt` contém o grafo de rotas, a injeção dos ViewModels e os callbacks de navegação.

Ao substituir uma tela, preserve os estados de carregamento, erro e vazio e conecte os controles aos callbacks existentes. Use Presentation para incluir os diálogos e efeitos visuais, ou Content para substituir apenas o conteúdo principal. No detalhe de medicamento, MedicationDetailContent inclui ambos.

## Verificação

Execute `./gradlew :app:testDebugUnitTest :app:assembleDebug --no-configuration-cache`.

Os testes do cadastro cobrem etapas, edição, rascunhos, salvamento e as ações extraídas do visual. Os testes instrumentados existentes verificam os fluxos completos no dispositivo.

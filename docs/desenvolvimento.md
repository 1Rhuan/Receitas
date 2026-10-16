# Desenvolvimento

O projeto pode ser executado sem conta, servidor ou chave de API. É necessário preparar o ambiente Android e baixar as dependências na primeira compilação.

## Ambiente

| Item | Configuração do projeto |
| --- | --- |
| IDE | Android Studio compatível com AGP 9.4.1 |
| Gradle | Wrapper incluído no repositório |
| JDK do daemon | 25, conforme `gradle/gradle-daemon-jvm.properties` |
| SDK de compilação e destino | Android 37 |
| Android mínimo | 8.0, API 26 |
| Bibliotecas e plugins | Versões em `gradle/libs.versions.toml` |

Abra a raiz no Android Studio, instale a plataforma do SDK e aceite as licenças. O arquivo `local.properties`, gerado localmente, contém o caminho do SDK e não deve ser versionado. Após a sincronização, selecione um emulador ou aparelho de teste e execute `app`.

No Windows, a compilação pelo terminal é:

```powershell
.\gradlew.bat assembleDebug
```

No Linux e macOS, use `bash ./gradlew assembleDebug`. O APK fica em `app/build/outputs/apk/debug/app-debug.apk`.

## Onde trabalhar

Os caminhos abaixo partem de `app/src/main/java/cc/rhuan/receitas/`.

| Alteração | Local |
| --- | --- |
| Etapas, campos e salvamento de cadastro | `ui/feature/prescriptions/wizard/` |
| Lista e detalhe de medicamento | `ui/feature/medications/` |
| Lista, detalhe e itens de receita | `ui/feature/prescriptions/` |
| Agenda e acompanhamentos do Início | `ui/feature/home/` |
| Destinos e retorno entre telas | `ui/navigation/PrescriptionsNavHost.kt` e `NavRoute.kt` |
| Cálculos e condições de alerta | `domain/model/` e `domain/usecase/` |
| Gravação e integridade dos dados | `data/repository/` e `data/local/` |
| Entrega e sincronização de notificações | `notifications/` |

O [exemplo de retirada](arquitetura.md#exemplo-completo-registrar-uma-retirada) acompanha uma ação entre essas partes e identifica os arquivos envolvidos.

## Interface e mensagens

Os composables de apresentação recebem estado e callbacks. A coleta do ViewModel e a conexão das ações ficam nas funções de integração das rotas. Dados persistidos não devem ser lidos diretamente de um DAO pela tela.

`RecordListScaffold` organiza as listas; `GroupedSection` e `GroupedValueRow` apresentam detalhes; `WizardQuestionScreen` organiza as etapas de cadastro. Cores, tipografia e medidas estão centralizadas em `ui/theme/DesignSystem.kt` e nos arquivos do tema. Um padrão recorrente deve ser compartilhado em `ui/components/`.

A navegação principal mostra Início, Receitas e Remédios. Receitas e Remédios têm + contextual junto à cápsula; o Início não tem essa ação. Listas preenchidas não repetem criação no topo. O espaço final da lista usa a altura medida da barra, mantendo transparência ao redor da cápsula.

Detalhes usam grupos para informações, cartões para registros e ações reconhecíveis. Edição, remoção e reativação ficam junto à identidade. A prévia do histórico é somente de leitura; as ações por evento ficam na tela completa. A referência visual à One UI é uma adaptação do projeto, implementada com componentes Compose e Material 3.

A interface usa português. Textos estáticos, plurais e descrições acessíveis ficam em `app/src/main/res/values/strings.xml`; o pacote `text/` representa e resolve mensagens. Novos identificadores Kotlin devem usar os termos do código: `Prescription`, `Medication`, `Pickup` e `Tracking`.

## Testes locais

```powershell
.\gradlew.bat testDebugUnitTest lintDebug assembleDebug assembleDebugAndroidTest
```

`testDebugUnitTest` executa os testes da JVM; `lintDebug` faz análise estática. `assembleDebugAndroidTest` apenas compila o APK de testes instrumentados, sem executar os cenários.

Os testes da JVM ficam em `app/src/test/`. Os instrumentados ficam em `app/src/androidTest/` e verificam interface, Room, migrações e transações.

### Emulador dedicado

A suíte instrumentada cadastra, edita e remove dados fictícios. Use um emulador de testes e confira o serial com `adb devices`. Substitua o serial de exemplo nos dois lugares:

```powershell
$env:ANDROID_SERIAL = 'emulator-5554'
.\gradlew.bat connectedDebugAndroidTest '-Pandroid.injected.device.serial=emulator-5554' --no-configuration-cache
```

Evite executar a suíte em um aparelho com dados pessoais. A seleção explícita também evita atingir outro dispositivo conectado.

### Relatórios

| Verificação | Relatório local |
| --- | --- |
| Unitários | `app/build/reports/tests/testDebugUnitTest/index.html` |
| Instrumentados | `app/build/reports/androidTests/connected/debug/index.html` |
| Lint | `app/build/reports/lint-results-debug.html` |

Arquivos de `app/build/` não são versionados. O [workflow Android](../.github/workflows/android.yml) executa testes unitários, lint e compilação; não executa os cenários instrumentados em emulador. Os critérios para interpretar resultados estão em [Validação](validacao.md).

# Receitas

Protótipo Android para organização pessoal de receitas médicas, medicamentos e datas previstas de retirada ou compra, desenvolvido como projeto de TCC.

## Funcionalidades

- Cadastro, edição e associação de receitas e medicamentos.
- Sugestões de validade de 10, 30, 180 e 365 dias, com configuração personalizada por dias ou data.
- Acompanhamento recorrente de retiradas/compras, com intervalo e antecedência configuráveis.
- Histórico de retiradas com correção e exclusão de eventos.
- Renovação de receitas com preservação do documento anterior e do histórico.
- Avisos de vencimento, retirada e conflito entre validade da receita e próxima retirada.
- Arquivamento, reativação, busca e filtros.
- Armazenamento local com Room e interface em português, com temas claro e escuro.

As datas são informações de organização pessoal. Não confirmam autorização de dispensação nem disponibilidade na farmácia. O protótipo não controla horários de doses nem avalia resultados clínicos. Notificações dependem da permissão do Android e das condições de execução em segundo plano; WorkManager não garante horário exato.

## Executar

1. Clone o repositório e abra sua pasta no Android Studio compatível com AGP 9.4.
2. Instale Android SDK Platform 37 e aceite as licenças do SDK. O aplicativo requer Android 8.0 (API 26) ou superior.
3. Use JDK 25 para executar o Gradle, conforme `gradle/gradle-daemon-jvm.properties`. A configuração do daemon pode baixar a distribuição necessária.
4. Deixe o Android Studio gerar `local.properties` com o caminho do SDK. Esse arquivo é específico de cada computador e não é versionado.
5. Aguarde a sincronização e execute a configuração `app` em um emulador ou aparelho de teste.

Não são necessários servidor, conta de usuário ou chave de API. A primeira compilação requer internet para baixar Gradle, plugins e bibliotecas.

### Compilar e verificar no Windows

```powershell
.\gradlew.bat testDebugUnitTest assembleDebug assembleDebugAndroidTest lintDebug
```

No Linux/macOS, use `bash ./gradlew` no lugar de `.\gradlew.bat`.

O APK de desenvolvimento é gerado em `app/build/outputs/apk/debug/app-debug.apk`. É assinado com a chave de desenvolvimento; uma distribuição de produção exige assinatura própria, mantida fora do Git.

### Testar em emulador

Use um emulador dedicado a testes, com dados fictícios. A suíte cria registros de demonstração e executa ações de edição, renovação e remoção. Quando houver mais de um dispositivo, selecione explicitamente o emulador:

```powershell
$env:ANDROID_SERIAL = 'emulator-5554'
.\gradlew.bat connectedDebugAndroidTest '-Pandroid.injected.device.serial=emulator-5554' --no-configuration-cache
```

Relatórios locais:

- Unitários: `app/build/reports/tests/testDebugUnitTest/index.html`.
- Instrumentados: `app/build/reports/androidTests/connected/debug/index.html`.
- Lint: `app/build/reports/lint-results-debug.html`.

O workflow [Android](.github/workflows/android.yml) compila o aplicativo e os testes instrumentados, executa os testes unitários e o lint em pushes e pull requests. A execução dos testes instrumentados no emulador é local; o workflow não executa um emulador.

## Organização

Os dados ficam na área privada do aplicativo. O protótipo exclui seus arquivos do backup automático e da transferência entre dispositivos. Desinstalar o aplicativo ou limpar seus dados remove os registros locais; não há exportação ou restauração implementada. O armazenamento local não equivale a uma auditoria completa de segurança ou conformidade legal.

```text
app/src/main/java/cc/rhuan/receitas/
  data/           Banco Room, entidades, DAOs e repositórios
  domain/         Modelos e regras de datas e lembretes
  notifications/  Agendamento e sincronização com WorkManager
  di/             Injeção de dependências com Hilt
  ui/             Navegação, telas, componentes e tema Compose
app/src/test/     Testes unitários
app/src/androidTest/  Testes de interface, persistência e migração
app/schemas/      Schema versionado do Room
docs/             Documentação: onboarding, arquitetura, regras, fluxos e pendências
```

Quem está chegando ao projeto deve começar por [docs/onboarding.md](docs/onboarding.md); o [índice da documentação](docs/README.md) indica o que ler conforme a tarefa. Consulte [DESIGN.md](DESIGN.md) para as decisões atuais de interface, [arquitetura](docs/arquitetura.md), [cadastro](docs/cadastro.md), [lembretes](docs/lembretes.md), [modelagem](docs/modelagem.md) e [revisão para publicação](docs/revisao-publicacao.md). Divergências e dívidas conhecidas estão em [pendências](docs/pendencias.md). `DESIGN-HISTORY.md` registra decisões substituídas.

## Publicar no GitHub

O projeto já usa Git. Crie um repositório vazio no GitHub e configure seu endereço como `origin`. Revise `git status`, `git diff` e `git diff --cached` antes do commit e do envio. Se `origin` já existir, confira seu endereço antes de alterá-lo. Não envie `local.properties`, `.idea`, bancos de dados, capturas com informações pessoais, senhas ou chaves de assinatura.

Após revisar os arquivos, inclua as alterações com `git add --all` e crie o commit. Configure o endereço real com `git remote add origin URL_DO_REPOSITORIO` e envie a branch atual com `git push -u origin HEAD`. Substitua `URL_DO_REPOSITORIO` pelo endereço do repositório criado. A preparação local não executa esses passos nem publica seus arquivos automaticamente.

Nenhuma licença de redistribuição foi escolhida nesta preparação. O autor deve definir a licença desejada antes de oferecer o projeto para reutilização por terceiros.

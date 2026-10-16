# Receitas

Aplicativo Android para organizar receitas médicas, medicamentos e datas previstas de retirada ou compra. Desenvolvido como projeto de TCC, com interface em português e armazenamento local.

O foco é acompanhar dois prazos diferentes: até quando a receita é válida e quando está prevista uma nova retirada do medicamento. Quando a receita escolhida para o acompanhamento vence antes da próxima retirada, o aplicativo identifica esse conflito para apoiar o planejamento pessoal.

## Funcionalidades

- Cadastro e edição de receitas e medicamentos em fluxos separados.
- Validade da receita por prazo predefinido ou personalizado.
- Associação de medicamentos a receitas, com orientação e quantidade prescritas.
- Acompanhamento de retiradas recorrentes e previsão da próxima data.
- Histórico de retiradas com correção e exclusão de eventos.
- Renovação com preservação do documento anterior.
- Alertas de vencimento, retirada e conflito entre prazos.
- Busca, filtros, arquivamento e reativação.
- Temas claro e escuro.

As datas são previsões para organização pessoal. Não confirmam disponibilidade na farmácia ou autorização de dispensação. O aplicativo não controla doses nem estoque.

## Executar

1. Abra o projeto no Android Studio compatível com AGP 9.4.1.
2. Instale o Android SDK Platform 37 e aceite as licenças.
3. Configure o Gradle para usar JDK 25, conforme `gradle/gradle-daemon-jvm.properties`.
4. Aguarde a sincronização e execute `app` em um emulador ou aparelho de teste com Android 8.0 ou superior.

Não são necessários servidor, conta ou chave de API. A primeira compilação requer internet para baixar dependências. O caminho local do SDK fica em `local.properties`, gerado pelo Android Studio e não versionado.

Para compilar no Windows:

```powershell
.\gradlew.bat assembleDebug
```

No Linux e macOS, use `bash ./gradlew assembleDebug`. O APK fica em `app/build/outputs/apk/debug/app-debug.apk`.

## Tecnologias

Kotlin, Jetpack Compose, Material 3, Navigation Compose, Room, Hilt e WorkManager. As versões estão em `gradle/libs.versions.toml`.

```text
app/src/main/java/cc/rhuan/receitas/
  data/           Persistência e repositórios
  domain/         Modelos e regras de negócio
  notifications/  Agendamento e sincronização de lembretes
  di/             Injeção de dependências
  text/           Representação e resolução de mensagens
  ui/             Telas, navegação, componentes e tema
app/src/test/         Testes unitários
app/src/androidTest/  Testes instrumentados
app/schemas/          Schemas do Room
```

## Testes

```powershell
.\gradlew.bat testDebugUnitTest lintDebug assembleDebug assembleDebugAndroidTest
```

Esse comando não executa os testes instrumentados. O [guia de desenvolvimento](docs/desenvolvimento.md#testes-locais) explica como executá-los em um emulador dedicado e onde encontrar os relatórios.

O [workflow Android](.github/workflows/android.yml) executa testes unitários, lint e compilação em pushes e pull requests.

## Dados e limitações

Os registros ficam na área privada do aplicativo, sem sincronização com servidor. Backup e transferência entre dispositivos pelo Android estão desativados. Não há exportação ou restauração: desinstalar o aplicativo ou limpar seus dados remove os registros locais.

Notificações dependem da permissão do Android e das condições de execução em segundo plano. Não há garantia de horário exato. Outras limitações estão em [Validação](docs/validacao.md).

## Documentação

A [documentação do aplicativo](docs/README.md) apresenta o problema, os conceitos e o percurso de leitura. Os capítulos explicam o funcionamento, a arquitetura, o banco e a validação do protótipo.

Os [requisitos](docs/requisitos.md) relacionam o escopo do TCC à implementação. Os diagramas de casos de uso, fluxos, arquitetura e dados acompanham os capítulos correspondentes.

## Licença

O projeto ainda não possui uma licença de redistribuição definida.

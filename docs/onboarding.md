# Comece aqui

Guia para quem assume o projeto sem contexto prévio. Leva cerca de 15 minutos. Depois dele, o [índice](README.md) indica o que ler conforme a tarefa.

## O que é o Receitas

Protótipo Android de **organização pessoal** de receitas médicas, medicamentos e datas previstas de retirada ou compra. É um projeto de TCC. Pacote `cc.rhuan.receitas`, versão `1.0`.

O app guarda tudo localmente (Room), sem servidor, conta ou chave de API. A interface é toda em português.

### O que o app faz

- Cadastra e edita receitas e medicamentos, e os associa entre si.
- Calcula a validade da receita a partir de tipos sugeridos (10, 30, 180 e 365 dias) ou de um prazo personalizado.
- Acompanha retiradas recorrentes: a próxima data é a última retirada real mais o intervalo.
- Mantém o histórico de retiradas, com correção e exclusão de eventos.
- Renova receitas sem perder o documento anterior nem o histórico.
- Avisa sobre vencimento, retirada próxima ou atrasada e conflito entre a validade da receita e a próxima retirada.
- Arquiva, reativa, busca e filtra.

### O que o app não faz

Estes limites são deliberados. Não os transforme em funcionalidades sem decisão do autor.

- Não controla horários de dose, estoque, fotos, anexos nem outras pessoas.
- Não avalia resultados clínicos.
- Não confirma autorização de dispensação nem disponibilidade na farmácia. As datas são previsões.
- Não exporta nem restaura dados. Os dados ficam fora do backup do Android.
- Não oferece outros idiomas.

## Rodar o projeto

Requisitos, conforme [README.md](../README.md) e os arquivos de build:

| Item | Valor |
| --- | --- |
| Android Studio | Compatível com AGP 9.4.1 |
| JDK do Gradle | 25 (`gradle/gradle-daemon-jvm.properties`; o Gradle pode baixá-lo) |
| SDK | Plataforma 37 instalada, licenças aceitas |
| Aparelho | Android 8.0 (API 26) ou superior |
| Kotlin / KSP | 2.4.20 / 2.3.12 |
| Compose BOM | 2026.09.00 |
| Room / Hilt / WorkManager | 2.8.5 / 2.60.1 / 2.12.0 |

Passos:

1. Abra a pasta no Android Studio e deixe-o gerar `local.properties`. Esse arquivo é local e não vai para o Git.
2. Aguarde a sincronização do Gradle. A primeira vez exige internet.
3. Execute a configuração `app` em um emulador de testes.

### Verificar o estado do projeto

```powershell
.\gradlew.bat testDebugUnitTest assembleDebug assembleDebugAndroidTest lintDebug
```

Em Linux/macOS, use `bash ./gradlew`.

Linha de base em 15/10/2026: `:app:testDebugUnitTest` passou com **74 testes, 0 falhas, 0 erros e 0 ignorados**. Essa execução foi feita para esta documentação e não incluiu lint nem testes instrumentados. Os resultados mais recentes dos demais ficam em [revisao-publicacao.md](revisao-publicacao.md): lint com zero erros e quatro avisos de versões de dependências.

Testes instrumentados (interface, Room, migração) rodam só em emulador e criam dados de demonstração. Quando houver mais de um dispositivo, **fixe o serial**:

```powershell
$env:ANDROID_SERIAL = 'emulator-5554'
.\gradlew.bat connectedDebugAndroidTest '-Pandroid.injected.device.serial=emulator-5554' --no-configuration-cache
```

> [!CAUTION]
> Em uma rodada anterior, uma execução sem serial fixo atingiu um aparelho Samsung real e causou conflitos de instalação. A preservação dos dados daquele aparelho não foi confirmada. Nunca rode testes instrumentados sem `ANDROID_SERIAL` quando houver um aparelho pessoal conectado. Detalhes em [cadastro.md](cadastro.md).

## Onde fica cada coisa

```text
app/src/main/java/cc/rhuan/receitas/
  data/local/        Room: banco, entidades, DAOs, conversores, migrações
  data/repository/   Interfaces e implementações Room dos repositórios
  domain/model/      Modelos de domínio (PrescriptionItem, MedicationItem, PickupInterval…)
  domain/usecase/    Regras de lembrete e situação de retirada
  notifications/     ReminderScheduler, ReminderWorker, ReminderSynchronizer
  di/                Módulos Hilt
  ui/navigation/     NavRoute, grafo, barra flutuante, MainScreen
  ui/feature/        Uma pasta por funcionalidade (home, medications, prescriptions,
                     notifications, menu). A de prescriptions tem o wizard de cadastro.
  ui/components/     Componentes compartilhados (listas, detalhes, formulários, menus)
  ui/theme/          DesignSystem.kt (tokens), Theme.kt, AppPalette.kt
app/src/test/        Testes unitários
app/src/androidTest/ Testes de interface, persistência e migração
app/schemas/         Schema Room exportado (versão 4)
```

Para entender como as camadas se conectam, leia [arquitetura.md](arquitetura.md).

### "Quero mudar X, onde mexo?"

| Tarefa | Onde |
| --- | --- |
| Layout de uma tela | `ui/feature/<área>/*Screen.kt` e `ui/components` |
| Cor, tipografia, espaçamento | `ui/theme/DesignSystem.kt`. Nunca use valores locais por tela. |
| Fluxo ou etapas do cadastro | `ui/feature/prescriptions/wizard/` (estado em `PrescriptionWizardUiState.kt`) |
| Regra de validade, intervalo ou vínculo | `domain/` e `data/repository/`; documente em [modelagem.md](modelagem.md) |
| Quando e o que notificar | `domain/usecase/EvaluateRemindersUseCase.kt` e `notifications/` |
| Tabelas ou colunas | `data/local/`. **Exige migração e novo schema.** Veja abaixo. |
| Textos | `app/src/main/res/values/strings.xml` |
| Rotas e navegação | `ui/navigation/ReceitasNavHost.kt` e `NavRoute.kt` |

## Regras que não podem ser quebradas

1. **Não use fallback destrutivo no Room.** Mudança de schema precisa de migração explícita, schema exportado e teste de migração. Hoje há migrações 2→3 e 3→4 em `DatabaseMigrations.kt`.
2. **Não crie vínculo automático entre medicamento e receita para retiradas.** Participar de uma receita e escolher a receita para retiradas são relações diferentes. A única exceção aprovada é "Adicionar medicamento" a partir da lista de uma receita ativa. Veja [modelagem.md](modelagem.md#relacionamentos).
3. **Não apague dados nem histórico por conveniência.** Arquivar é distinto de excluir. Receita substituída é somente consulta.
4. **Não crie estilos por tela.** Reutilize `ui/components` e `DesignSystem.kt`. Padrão recorrente novo vira componente compartilhado.
5. **Não prefixe componentes com `OneUi`.** A referência Samsung fica em `DESIGN.md`.
6. **Preserve alterações locais que você não fez.** O repositório tem trabalho não commitado (veja a próxima seção).
7. **Valide telas com conteúdo longo, 320 × 640 dp, fonte 200% e temas claro e escuro.** É o critério de aceite visual do projeto.
8. **Texto de data é previsão.** Não escreva copy que prometa disponibilidade ou autorização.

## Estado do repositório

> [!WARNING]
> Em 15/10/2026, antes desta revisão de documentação, o working tree tinha **103 entradas não commitadas** no `git status` (arquivos modificados e novos), incluindo `README.md`, `.github/`, `.gitattributes`, componentes de interface, testes e documentação. O último commit é `c33e729`. Não há `origin` configurado e nada foi publicado. Revise `git status` e `git diff` antes de qualquer commit e agrupe as mudanças por assunto.

Publicação no GitHub, `.gitignore`, licença e workflow estão descritos no [README](../README.md#publicar-no-github). Nenhuma licença foi escolhida.

## Glossário

| Termo | Significado |
| --- | --- |
| **Receita** | Documento com emissão, validade, profissional opcional, observações e itens. Pode estar ativa, arquivada ou substituída. |
| **Item da receita** | Relação receita–medicamento, com orientação e quantidade prescritas. Preserva nome, concentração e forma como constam no documento. |
| **Medicamento** | Nome, princípio ativo, concentração e forma farmacêutica. Pode constar em várias receitas. |
| **Acompanhamento** | Configuração de retirada de um medicamento: intervalo, antecedência, local e receita opcional. Um por medicamento. Na interface aparece como "Acompanhamento de retiradas". |
| **Retirada** | Evento com data real e momento do registro. Uma por acompanhamento e data. Também chamada de compra ou reposição em textos antigos. |
| **Receitas vinculadas** | Quantidade de receitas que **contêm** o medicamento (participação). Não depende do acompanhamento. |
| **Receita para retiradas** | Receita escolhida explicitamente para sustentar o acompanhamento. É independente da participação. Aparece só quando escolhida. |
| **Substituída** | Receita que foi renovada. Fica somente para consulta. |
| **Renovação** | Nova receita que aponta para a anterior. A anterior passa a substituída. |
| **Conflito de prazos** | Receita para retiradas vence antes da próxima retirada. Gera aviso imediato. Vencimento no mesmo dia não é conflito. |
| **Wizard** | Fluxo de cadastro em etapas (`PrescriptionWizard*`). Cobre receita completa, medicamento avulso, renovação, edição e inclusão pela lista da receita. |
| **Listas agrupadas** | Padrão de interface aprovado: grupos com título externo para detalhes, cartões para registros e resumos. |
| **Cápsula** | A barra de navegação flutuante (`TextNavigationBar`). |

## Fluxos de ponta a ponta para conhecer

- **Cadastro:** Receitas → botão de criação → wizard (nome → emissão e tipo → alertas → medicamentos → retiradas → detalhes opcionais → revisão) → salvar em transação Room. Veja [cadastro.md](cadastro.md).
- **Retirada:** Início ou detalhe do medicamento → registrar data real → evento no histórico → previsão recalculada → alertas antigos encerrados. Veja [reposicao.md](reposicao.md).
- **Alerta:** WorkManager diário e verificações ao abrir o app → `ReminderSynchronizer` → histórico interno e notificação do Android. Veja [lembretes.md](lembretes.md).
- **Renovação:** detalhe da receita → Registrar nova receita → wizard preenchido → nova receita aponta para a anterior. Veja [modelagem.md](modelagem.md#renovação).

## Pendências e riscos

Antes de planejar trabalho novo, leia [pendencias.md](pendencias.md). Ele reúne divergências entre documentos e código, validações que nunca foram feitas (TalkBack, tablets, aparelho físico) e dívidas técnicas.

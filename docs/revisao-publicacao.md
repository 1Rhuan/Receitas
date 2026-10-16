# Revisão para publicação no GitHub

Revisão de 15/10/2026 do estado local do projeto, incluindo alterações que já existiam antes desta rodada. O objetivo foi preparar a reprodução do projeto e procurar defeitos nos fluxos de cadastro, acompanhamento, histórico e avisos. Os testes usam dados fictícios e o emulador Android 14; não houve testes em aparelho físico.

## Correções

| Problema | Comportamento após a correção |
| --- | --- |
| Receitas arquivadas/substituídas apareciam em eventos futuros, datas selecionadas e indicadores da agenda | A agenda apresenta somente receitas ativas; o histórico permanece acessível nas listas |
| O aviso de conflito oferecia registrar retirada como ação, mesmo quando a necessidade era revisar a receita | O aviso oferece consultar o medicamento; o registro real continua disponível nos detalhes |
| Ajuda orientava registrar novas retiradas dentro do histórico, que oferece correção e exclusão | Texto distingue o registro nos detalhes da correção no histórico |
| Texto da inclusão contextual não explicava a função da receita para retiradas | A orientação explica que a validade será comparada com a próxima retirada |
| Nome longo da receita ocupava meia coluna e era repetido integralmente no aviso | Nome usa linha vertical com largura completa; aviso mantém as datas sem repetir o nome |
| Botão do aviso dizia Ver medicamentos ao abrir um único registro | Rótulo ajustado para Ver medicamento |
| Ações do cabeçalho de notificações disputavam a mesma linha do título e reduziam a área de leitura | Título e ações ficam separados; cabeçalho e filtros rolam com os avisos, usando botões e tokens compartilhados |
| Limpar todas as notificações executava a ação imediatamente | Confirmação explica que os registros de saúde serão preservados |
| KSP antigo declarava incompatibilidade com Kotlin atual | Atualização para KSP 2.3.12 e remoção da opção que forçava o mecanismo antigo |
| Recursos em português eram tratados pelo lint como inglês | Idioma declarado e plurais ajustados, incluindo unidades com número formatado |
| Suíte agregadora incluía classes que o runner também descobria individualmente | Removido o agregador redundante; os cenários continuam em suas classes originais |
| Backup do Android usava configuração padrão para os dados de saúde | Backup desativado e arquivos excluídos explicitamente de backup/transferência |
| Acompanhamentos da tela inicial repetiam a mesma ação em botões de 34 dp e texto de 11 sp | Uma ação principal pelo componente compartilhado, com alvo mínimo de 48 dp e texto de ação distinto dos metadados |
| Data do evento não aparecia no conteúdo principal de alguns cartões | A data passou a anteceder informações de apoio, com estilo de corpo principal |
| Dias do calendário podiam ficar apertados ou sobrepostos | Quantidade de dias se adapta à largura, com abreviações mais claras e alvos de 48 dp |
| Próximos eventos usavam nomes truncados e data como controle pequeno | Nome pode quebrar linha e Ver data é um botão reconhecível |

O prazo anual de 365 dias e o aviso de conflito entre validade e retirada foram implementados na rodada anterior e receberam cenários adicionais de interface e notificação nesta revisão.

## Usabilidade e ambiguidades

A avaliação é heurística, apoiada em execução de cenários e inspeção visual; não constitui teste de compreensão com usuários finais. Os fluxos mantêm as decisões de DESIGN.md, sem substituir a navegação ou as composições aprovadas.

- Receita do medicamento representa sua participação no documento. Receita para retiradas é a referência usada para comparar os prazos. A inclusão pela lista de uma receita ativa mantém a exceção contextual já aprovada; o cadastro geral mantém escolha independente.
- Vencimento pertence à receita. Próxima retirada prevista pertence ao acompanhamento e depende da data real mais recente, sem confirmar disponibilidade nem autorização de dispensação.
- Registrar retirada informa um acontecimento real. Uma antecipação precisa de confirmação, e cancelar não registra o evento.
- Arquivar preserva histórico e encerra lembretes; excluir definitivamente apresenta consequências e confirmação. Excluir medicamento continua bloqueado enquanto houver participação em receitas.
- Revisão permite retornar ao assunto editado, sem obrigar a repetir todo o cadastro. Informações opcionais continuam identificadas e podem ser puladas sem apagar o preenchimento.

### Hierarquia de leitura

Não foi aplicado aumento indiscriminado à tipografia do aplicativo. Os estilos continuam centralizados em `DesignSystem.kt`; a revisão ajusta os papéis dos textos e os componentes que os acomodam.

| Papel | Tratamento |
| --- | --- |
| Nome de medicamento/receita no acompanhamento | Título de 18 sp, sem truncamento no cartão principal |
| Data do evento e mensagem de aviso | Corpo principal de 16 sp |
| Situação e ações principais | Rótulo de 14 sp; ação com alvo mínimo de 48 dp e altura expansível |
| Tipo de registro, horário de registro e informações de apoio | Estilos secundários de 12 a 14 sp, preservando menor ênfase |
| Rótulos e valores nos detalhes | Estilos compartilhados; organização vertical com fonte ampliada |

O calendário e os cabeçalhos adaptam a organização em vez de reduzir a fonte para caber. A inspeção encontrou a abreviação de domingo quebrando em fonte 200%; as células agora ganham largura conforme a escala e a página passa a exibir menos dias, com teste de linha única para esse caso. Na tela inicial, uma só ação de registro abre o diálogo que continua permitindo escolher a data real. Metadados não recebem a mesma ênfase de nomes, datas e decisões.

Foram calculados 12 pares de contraste dos tokens usados para texto/superfície, texto secundário, ação principal, contêiner principal, erro e situação de retirada, nos dois temas. O menor resultado foi 4,71:1. A verificação desses pares não substitui uma auditoria de todas as combinações, estados desabilitados ou superfícies legadas.

## Preparação do repositório

README descreve propósito, ambiente, execução, testes, arquitetura e limites. `.gitignore` exclui IDE, builds, bancos, arquivos de ambiente e chaves de assinatura. `.gitattributes` define finais de linha para o wrapper e as fontes. As configurações `.idea` foram retiradas somente do índice Git, mantendo os arquivos locais. Não houve reescrita de histórico.

O workflow Android usa permissão de leitura e executa testes unitários, lint e compilação dos APKs de desenvolvimento e de testes. Sua execução hospedada será verificada após o envio ao GitHub; validar o build local não comprova execução no serviço. O projeto não tinha remote configurado nesta revisão. Nenhum repositório foi criado nem conteúdo enviado.

A busca automatizada por padrões de credenciais não encontrou chaves privadas, tokens GitHub, chaves AWS ou chaves Google nos arquivos candidatos à publicação. Essa busca não equivale a uma auditoria completa de segredos ou de todo o histórico Git. Nenhuma licença de redistribuição foi escolhida em nome do autor.

Referências técnicas para as mudanças de infraestrutura: [release do KSP](https://github.com/google/ksp/releases/tag/2.3.12), [KSP2](https://github.com/google/ksp/blob/main/docs/ksp2.md) e [regras de backup do Android](https://developer.android.com/identity/data/autobackup). As referências de projeto e os resultados locais têm escopos distintos de uma auditoria regulatória ou de segurança.

## Validação e limites

Resultados em 15/10/2026, no emulador `emulator-5554`, Android 14/API 34:

- 72 testes unitários aprovados, sem falhas ou erros.
- Compilação dos APKs do aplicativo e dos testes aprovada.
- Lint aprovado: zero erros e quatro avisos de versões disponíveis de dependências/plugins.
- Rodada completa: 87 cenários instrumentados, com 82 aprovações e cinco falhas de aquisição de imagem (PixelCopy/tempo de captura).
- Repetição do diálogo de receita: um cenário aprovado após selecionar a raiz ativa da janela.
- Repetições das classes afetadas e da leitura da tela inicial: 22 cenários aprovados (4 de leitura, 3 de revisão do cadastro, 7 de Remédios e 8 de Receitas), incluindo o calendário corrigido.
- Considerando a execução completa e as repetições, todos os 87 cenários têm aprovação. Isso não equivale a uma única execução completa sem falhas de infraestrutura. Os relatórios completos e separados foram preservados em `app/build/outputs/publication-review`, fora do Git.

Os testes de uso exercitaram registro antecipado de retirada, recálculo após excluir um evento, cancelamento de exclusão, recriação da Activity, arquivamento/reativação, entrega real de aviso pelo Android e encerramento do alerta após ampliar a validade. A inspeção visual incluiu nomes longos, 320 × 640 dp, temas claro/escuro e escala de fonte de 200%. O calendário recebeu verificação de uma linha para domingo, além de seleção e paginação. Não foi realizado estudo de usabilidade com participantes.

As capturas de interface usam a raiz Compose ativa, incluindo diálogos. A aquisição da imagem permite até três tentativas somente para falhas transitórias de PixelCopy ou tempo de captura; esgotar as tentativas continua falhando o teste. Cliques e asserções não são repetidos por esse mecanismo. A rodada anterior apresentou capturas de tela do sistema nulas em alguns cenários, apesar de as verificações de conteúdo terem avançado; a captura não depende mais dessa resposta do Android. As asserções de navegação, conteúdo, datas e persistência foram mantidas.

Permanecem fora da comprovação desta rodada: compreensão por usuários finais, TalkBack completo, tablets, diferentes fabricantes, execução periódica prolongada sob restrições de bateria, restauração de backups antigos e a execução hospedada do workflow. Notificações internas permanecem disponíveis com a permissão negada; a entrega pelo Android depende das configurações do sistema. A opção experimental de source sets e avisos de APIs descontinuadas/dependências devem ser acompanhados em manutenção futura.

## Padronização posterior dos estados vazios

Por solicitação do usuário na conversa por voz, o cartão central do Início passou a ser a referência compartilhada para Receitas, Remédios e Medicamentos da receita. Ícones vetoriais variam por contexto (documento ou comprimidos). A criação aparece uma única vez dentro do cartão vazio; nas listas preenchidas continua no início rolável. **[Substituído]** Nas listas preenchidas de Receitas e Remédios a criação é o "+" contextual junto à cápsula, conforme decisão de 15/10/2026. A lista interna reaproveita RecordListScaffold e a hierarquia de títulos dos cartões de Receitas, preservando nome/concentração, orientações, quantidades e navegação.

A altura mínima disponível centraliza a orientação sem limitar o crescimento com fonte ampliada. Receitas arquivadas explicam a necessidade de reativar nos detalhes; substituídas continuam somente para consulta. Os estados sem resultados por filtro preservam suas orientações próprias.

Validação específica desta alteração: sete cenários de estado vazio aprovados em 320 × 640 dp, incluindo claro/escuro e fonte 200%; mais dois cenários aprovados para lista preenchida com nome longo e inclusão/cancelamento real pela receita. As capturas finais foram inspecionadas. Esta rodada restrita complementa a revisão anterior; a suíte completa não foi repetida após a alteração visual.

## Referências de layout, menus e painéis inferiores — fechamento de 15/10/2026

As referências enviadas pelo usuário foram adaptadas para componentes compartilhados, mantendo a fonte atual: cabeçalhos com ações, menus arredondados, orientação vazia aberta com ícone contextual, Notificações com cartões e ações consistentes, escolhas únicas por rádio e painéis que sobem da borda inferior. O seletor de data mantém seu componente próprio. Conforme AGENTS.md vigente à época, Nova receita e Novo medicamento ficam no início rolável também no vazio, sem duplicação na orientação. **[Substituído]** Em 15/10/2026 o usuário decidiu manter o "+" contextual do código, e o `AGENTS.md` foi atualizado. Isso substitui a posição desses dois botões descrita na etapa intermediária acima.

Os painéis preservam confirmar/cancelar, a confirmação adicional da retirada antecipada e as regras existentes de remoção. Conteúdo longo pode rolar. Menus e painéis preservam explicitamente a escala de fonte da tela, inclusive nos testes com escala 200% injetada na composição.

Compilação do aplicativo e dos testes e lint aprovados; 72 testes unitários aprovados. O lint permanece com zero erros e quatro avisos de versões de dependências. A rodada de interface usa o emulador Phone, Android 10/API 29, com testes de conteúdo em 320 × 640 dp, temas claro/escuro e fonte 200%. Os relatórios desta etapa ficam em app/build/outputs/publication-review; não equivalem a uma repetição da suíte completa anterior.

A infraestrutura teve interrupções: o emulador anterior perdeu serviços do Android; uma tentativa via Gradle selecionou o aparelho Samsung apesar da propriedade de serial e foi interrompida. Essa tentativa não comprova testes nem ausência de instalação no aparelho. As execuções finais usam ADB com serial explícito emulator-5554. O teste de Voltar nos menus foi corrigido para enviar a tecla do Android após a abertura, evitando que a automação encerrasse a Activity ao fundo. Os testes não repetem cliques nem ocultam falhas de interação.

Resultados específicos: 23 cenários distintos aprovados — quatro de painéis, seis de menus/Notificações, sete de estados vazios, quatro de confirmação nos detalhes/histórico, um fluxo real de inclusão/cancelamento e um de escolhas por rádio com preservação dos dados de cada medicamento. Os oito casos de painéis/confirmações foram repetidos após o ajuste final da escala dos menus. Logs finais: final-ReferenceLayoutTest.txt, final-EmptyCollectionsDesignTest.txt, verified-flows-1.txt a verified-flows-5.txt e design-final-6.txt/design-final-7.txt. Relatórios antigos com falhas foram preservados para rastreabilidade.

As capturas de janelas de menu/painel agora priorizam a tela nativa para mostrar a janela sobreposta; capturas da raiz Compose continuam nas telas normais. As verificações de posição do painel usam marcadores Android com coordenadas na tela. As imagens foram inspecionadas; esta revisão não substitui estudo com usuários nem auditoria completa de acessibilidade.

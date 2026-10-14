# Revisão de textos da interface

A revisão uniformiza os textos em português e os alinha ao comportamento implementado.

| Antes | Depois / motivo |
| --- | --- |
| Reposição | Retirada nos campos e cartões; quantidade não representa estoque. |
| Registrar renovação | Registrar nova receita; a ação abre o cadastro de um novo documento. |
| Próxima retirada | Próxima retirada prevista; a data não garante disponibilidade na farmácia. |
| Venc. 1 dia | Vence em 1 dia; sem abreviação no aviso de prazo. |
| 1 dias antes | 1 dia antes; recursos de plural para lembretes, prazos e unidades do intervalo. |
| Medicamento cadastrado ao editar | Medicamento atualizado e Salvar alterações. |
| ID inválido / ciclo encerrado | Orientação para voltar à lista ou atualizar os dados. |
| Menu com ações vazias | Itens indisponíveis identificados como “em breve”; versão obtida do pacote instalado. |

Textos estáticos das telas, rótulos de acessibilidade e textos com datas ou valores foram centralizados em `app/src/main/res/values/strings.xml`. Recursos substituídos e sem referências foram removidos. As mensagens produzidas pelo domínio e pelos estados/ViewModels continuam em português em Kotlin; esta alteração não constitui uma tradução completa do aplicativo para outros idiomas.

O resumo do cadastro também passou a calcular a próxima retirada com a unidade configurada, corrigindo a previsão de intervalos em semanas, meses e anos.

Validação: compilação do APK, testes unitários, testes instrumentados de persistência/migração/interface e lint. Foram acrescentadas regressões para a previsão mensal de 31/01/2026 para 28/02/2026, lembrete singular e confirmação de medicamento editado.

Resultados: 36 testes unitários e 21 testes instrumentados no emulador passaram. Na execução com o Samsung SM-A576B também conectado, os 17 testes de persistência/migração/contexto passaram; o teste de cadastro foi interrompido com status -1, sem detalhamento da causa, e os demais testes de interface não executaram. A validação completa nesse aparelho permanece pendente.

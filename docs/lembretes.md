# Alertas de validade, renovação e retirada

WorkManager 2.12.0 verifica as datas diariamente, sem exigir rede ou horário exato. Uma verificação adicional ocorre ao abrir o aplicativo e quando receitas ou medicamentos são alterados.

- Receitas: antecedência configurada no banco (padrão: 5 dias), dia do vencimento e receita vencida. O dia do vencimento ainda é considerado válido.
- Retiradas recorrentes: antecedência configurada (padrão: 3 dias) e data alcançada ou atrasada. A data vem da última retirada mais o intervalo, independentemente do estoque. Registros sem data ou com intervalo inválido não geram lembretes.
- Cada etapa de cada ciclo gera um aviso. O histórico de entrega é persistido separadamente do histórico interno para que apagar notificações não provoque reenvio.
- Renovação, retirada, exclusão e mudança de etapa cancelam avisos antigos na barra e marcam alertas internos obsoletos como lidos.
- Android 13 ou superior solicita autorização na primeira abertura. Se recusada, os avisos internos continuam funcionando; a autorização pode ser habilitada nas configurações do Android. A próxima abertura verifica novamente os alertas pendentes.
- Tocar no aviso abre o aplicativo. O texto fica privado na tela bloqueada, conforme as configurações do sistema.

## Verificação manual em dispositivo ou emulador

1. Autorizar notificações e cadastrar uma receita próxima do vencimento e uma retirada recorrente próxima da data.
2. Conferir os avisos internos e a barra de notificações. Reabrir o app: a mesma etapa não deve produzir novo aviso.
3. Registrar uma renovação e uma retirada: os avisos antigos devem sair da barra e as próximas datas devem ser recalculadas.
4. Testar receita com vencimento hoje e ontem; retirada hoje e atrasada; retirada com estoque alto e sem receita vinculada.
5. Negar a permissão e verificar que os alertas internos continuam disponíveis. Autorizar nas configurações e reabrir: os avisos atuais devem ser entregues.
6. Desabilitar o canal de notificações e verificar que nenhum aviso externo é enviado.
7. Validar a execução periódica com o app em segundo plano e após reiniciar o dispositivo. O Android pode adiar a execução; não há garantia de horário específico.

Referência: https://developer.android.com/jetpack/androidx/releases/work

## Sincronização com a tela de notificações

A tela observa o mesmo histórico Room alimentado pela verificação diária. Atualizar chama a mesma sincronização utilizada pelo Worker e mantém apenas uma assinatura da lista. Categorias são identificadas pelo ciclo e etapa do alerta, sem inferência por palavras no título. Eventos antigos de dose ou estoque ficam fora da lista atual.

Ler, excluir ou limpar alertas cancela os respectivos avisos do Android. Excluir persiste a supressão daquela etapa, impedindo que atualizar recrie o item. A etapa seguinte e ciclos futuros continuam elegíveis. Tocar em um aviso do Android abre a tela de notificações e o marca como lido. Descartar o aviso deslizando na barra do Android não marca o histórico interno como lido.

Registrar renovação ou retirada exige confirmação e usa a data de hoje. Retirada altera a última data registrada e preserva os demais dados do medicamento. Alertas de ciclos encerrados ficam no histórico sem ações de registro. Ver receita abre os detalhes da receita vinculada.

Validação adicional: ler/excluir/limpar na tela e conferir a barra; atualizar e reabrir para garantir que excluídos não reapareçam; tocar em dois avisos diferentes com o app aberto e fechado; verificar que ler uma etapa antiga não permite registrar novamente uma renovação ou retirada após o ciclo mudar.

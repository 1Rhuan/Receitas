# Quantidade por reposição

Receitas e medicamentos usam RecordCard: categoria/aviso, ícone/título e informações complementares, com os mesmos espaçamentos e cores.

A quantidade por reposição é opcional e aceita unidade junto ao valor (2 caixas, 60 comprimidos, 1 frasco). Pode ser informada no cadastro, etapa Retiradas, e é exibida na revisão, lista, detalhes e início.

O valor é salvo em medications.prescribedQuantity. O saldo antigo currentStock permanece separado; não é convertido em quantidade por reposição. Registrar retirada atualiza a data e não soma, decrementa ou substitui essa quantidade. É possível apagar o valor ao editar.

Os lembretes usam apenas datas de validade, renovação e retirada; não usam limites de estoque. O filtro de estoque baixo foi removido.

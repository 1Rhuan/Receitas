# Identidade visual

Referência externa: [Pillo Android](https://pillo.care/), imagem pública da lista de medicamentos consultada em 13/10/2026.

A adaptação usa fundo neutro, cartões planos claros, cantos arredondados, hierarquia de texto e destaque azul. Os componentes do cadastro servem de base para as demais telas. Não foram reutilizadas imagens, marca ou funcionalidades de dosagem do Pillo.

- Theme.kt: paleta Material 3 completa, esquemas claros e escuros, formas.
- Type.kt: Poppins com tamanhos e alturas de linha explícitos.
- AppCard: cartão compartilhado para início, listas, detalhes, menu, alertas e cadastro.
- AppPalette: aviso de prazo com contraste próprio para ambos os temas.
- WizardDesignSystem: ações, opções e campos usam as cores do tema.

Usar surface para cartões; background para telas; onSurface/onSurfaceVariant para texto; primary/onPrimary para ações; error/errorContainer para vencidos; warning/warningContainer para prazos próximos; tertiary/tertiaryContainer para confirmação. A barra inferior destaca a seção ativa com primaryContainer.

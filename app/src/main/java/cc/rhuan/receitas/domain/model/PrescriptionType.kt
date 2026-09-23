package cc.rhuan.receitas.domain.model

enum class PrescriptionType(
    val label: String,
    val defaultValidityDays: Int,
    val description: String,
    val legalNote: String,
) {
    CONTINUO(
        label = "Uso contínuo",
        defaultValidityDays = 180,
        description = "Medicamentos de uso contínuo (pressão, diabetes, etc.)",
        legalNote = "Validade de 180 dias com dispensação fracionada.",
    ),
    ANTIBIOTICO(
        label = "Antibiótico",
        defaultValidityDays = 10,
        description = "Critérios rígidos de dispensação de antibacterianos",
        legalNote = "RDC nº 471/2021: prazo máximo de validade em apenas 10 dias corridos a contar da data de prescrição.",
    ),
    CONTROLADO(
        label = "Controlado - receita azul/amarela",
        defaultValidityDays = 30,
        description = "Substâncias sob Controle Especial (Portaria 344/98)",
        legalNote = "Portaria SVS/MS nº 344/1998: validade de estritos 30 dias contados a partir da data de emissão.",
    ),
    FARMACIA_POPULAR(
        label = "Farmácia Popular",
        defaultValidityDays = 180,
        description = "Atende pacientes do Programa Farmácia Popular do Brasil",
        legalNote = "Portaria de Consolidação GM/MS nº 5/2017: validade de 180 dias com liberações fracionadas.",
    ),
    SUS_BASIC(
        label = "Farmácias Básicas Municipais (SUS)",
        defaultValidityDays = 180,
        description = "Receitas de medicamentos contínuos na rede primária do SUS",
        legalNote = "Prazos oscilam habitualmente entre 90 e 180 dias conforme portarias municipais.",
    ),
    SPECIAL_CONTROL(
        label = "Substâncias sob Controle Especial",
        defaultValidityDays = 30,
        description = "Listas A, B e C (Notificação de Receita)",
        legalNote = "Portaria SVS/MS nº 344/1998: validade de 30 dias.",
    ),
    ANTIMICROBIAL(
        label = "Antimicrobianos",
        defaultValidityDays = 10,
        description = "Dispensação de antibacterianos",
        legalNote = "RDC nº 471/2021: validade 10 dias.",
    ),
    PFPB(
        label = "Programa Farmácia Popular do Brasil",
        defaultValidityDays = 180,
        description = "Farmácia Popular",
        legalNote = "Validade de 180 dias.",
    );

    companion object {
        fun fromLabel(label: String): PrescriptionType {
            return entries.find {
                it.label.equals(label, ignoreCase = true) || it.name.equals(label, ignoreCase = true)
            } ?: SUS_BASIC
        }
    }
}

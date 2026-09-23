package com.example.virabraking

enum class TipoServico {
    JOGO_VELAS("Jogo de Velas"),
    CABOS_VELAS("Cabos de Velas"),
    PASTILHA_FREIO("Pastilha de Freio"),
    FLUIDO_FREIO("Fluido de Freio"),
    OLEO_MOTOR_E_FILTRO("Óleo de Motor e Filtro"),
    FILTRO_COMBUSTIVEL("Filtro de Combustível"),
    FILTRO_AR("Filtro de Ar"),
    FILTRO_CABINE("Filtro de Cabine"),
    OLEO_DIRECAO_HIDRAULICA("Óleo da Direção Hidráulica"),
    LIQUIDO_ARREFECIMENTO("Líquido de Arrefecimento"),
    GEOMETRIA("Geometria"),
    BALACEAMENTO("Balanceamento"),
    TAMPA_RESERVATORIO_RADIADOR("Tampa Reservatório do Radiador"),
    PALHETA_VIDRO_TRASEIRO("Palheta do Vidro Traseiro"),
    PALHETA_PARABRISA("Palheta Parabrisa"),
    ESCAPAMENTO("Escapamento"),
    MILITEC("Militec"),
    PNEUS_DIANTEIROS("Pneus Dianteiros"),
    PNEUS_TRASEIROS("Pneus Traseiros"),
    AMORTECEDORES_DIANTEIROS("Amortecedores Dianteiros"),
    AMORTECEDORES_TRASEIROS("Amortecedores Traseiros"),
    BATERIA("Bateria"),
    CORREIA_DENTADA("Correia Dentada");

    val descricao: String

    constructor(item: String) {
        this.descricao = item
    }
}
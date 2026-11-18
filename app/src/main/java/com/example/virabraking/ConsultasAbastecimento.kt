package com.example.virabraking

import android.database.Cursor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

val LOCALE_BR: Locale = Locale.forLanguageTag("pt-BR")

data class Atividade(
    val origem: String,
    val id: Int,
    val veiculoId: Int,
    val titulo: String,
    val data: String,
    val quilometragem: Int,
    val detalhe: String,
    val valor: Double
)

data class GastoVeiculo(
    val apelido: String,
    val total: Double
)

data class GastoMensal(
    val mesAno: String,
    val rotulo: String,
    val total: Double
)

data class ManutencaoAgendada(
    val servicoId: Int,
    val veiculoId: Int,
    val tipo: String,
    val data: String,
    val veiculoApelido: String
)

fun BancoHelper.buscarProximaManutencao(dataHoje: String): ManutencaoAgendada? {
    readableDatabase.rawQuery(SQL_PROXIMA_MANUTENCAO, arrayOf(dataHoje)).use { cursor ->
        if (cursor.moveToFirst()) {
            return cursor.paraManutencaoAgendada()
        }
    }

    return null
}

private fun Cursor.paraManutencaoAgendada() = ManutencaoAgendada(
    servicoId = getInt(0),
    veiculoId = getInt(1),
    tipo = getString(2) ?: "",
    data = getString(3) ?: "",
    veiculoApelido = getString(4) ?: ""
)

private fun Cursor.paraAbastecimento() = RegistroAbastecimento(
    id = getInt(0),
    veiculoId = getInt(1),
    litros = getDouble(2),
    valorLitro = getDouble(3),
    valorTotal = getDouble(4),
    quilometragem = getInt(5),
    posto = getString(6) ?: "",
    data = getString(7)
)

fun BancoHelper.listarAbastecimentos(veiculoId: Int): List<RegistroAbastecimento> {
    val lista = mutableListOf<RegistroAbastecimento>()

    readableDatabase.rawQuery(SQL_LISTAR_ABASTECIMENTOS, arrayOf(veiculoId.toString())).use { cursor ->
        while (cursor.moveToNext()) {
            lista.add(cursor.paraAbastecimento())
        }
    }

    return lista
}

fun BancoHelper.listarAtividades(
    veiculoId: Int? = null,
    limite: Int? = 50,
    servicos: Boolean = true,
    abastecimentos: Boolean = true
    ): List<Atividade> {
    val consultas = mutableListOf<String>()
    val argumentos = mutableListOf<String>()

    if (servicos) {
        consultas.add(criarConsultaServicos(veiculoId))

        veiculoId?.let {
            argumentos.add(it.toString())
        }
    }

    if (abastecimentos) {
        consultas.add(criarConsultaAbastecimentos(veiculoId))

        veiculoId?.let {
            argumentos.add(it.toString())
        }
    }

    if (consultas.isEmpty()) {
        return emptyList()
    }

    val sql = construirConsultaAtividades(consultas, limite)

    limite?.let {
        argumentos.add(it.toString())
    }

    return executarConsultaAtividades(sql, argumentos)
}

private fun criarConsultaServicos(veiculoId: Int?): String {
    val filtro = criarFiltroVeiculo(veiculoId)

    return """
        SELECT
            'servico' AS origem,
            id,
            veiculo_id,
            tipo AS titulo,
            data,
            COALESCE(quilometragem, 0) AS km,
            COALESCE(oficina, '') AS detalhe,
            COALESCE(valor, 0) AS valor
        FROM servicos
        $filtro
    """.trimIndent()
}

private fun criarFiltroVeiculo(veiculoId: Int?): String {
    return if (veiculoId != null) {
        "WHERE veiculo_id = ?"
    } else {
        ""
    }
}

private fun criarConsultaAbastecimentos(veiculoId: Int?): String {
    val filtro = criarFiltroVeiculo(veiculoId)

    return """
        SELECT
            'abastecimento' AS origem,
            id,
            veiculo_id,
            'Abastecimento' AS titulo,
            data,
            quilometragem,
            printf('%.1f L', litros) ||
                CASE
                    WHEN posto IS NULL OR posto = '' THEN ''
                    ELSE ' • ' || posto
                END AS detalhe,
            valor_total AS valor
        FROM abastecimentos
        $filtro
    """.trimIndent()
}

private fun construirConsultaAtividades(consultas: List<String>, limite: Int?): String {
    val consultaUnificada = consultas.joinToString(" UNION ALL ")

    val ordenacao = " ORDER BY data DESC, id DESC"

    val limiteSql = if (limite != null) {
        " LIMIT ?"
    } else {
        ""
    }

    return consultaUnificada + ordenacao + limiteSql
}

private fun BancoHelper.executarConsultaAtividades(sql: String, argumentos: List<String>): List<Atividade> {
    val atividades = mutableListOf<Atividade>()

    readableDatabase.rawQuery(sql, argumentos.toTypedArray()).use { cursor ->
        while (cursor.moveToNext()) {
            atividades.add(cursor.paraAtividade())
        }
    }

    return atividades
}

private fun Cursor.paraAtividade() = Atividade(
    origem = getString(0) ?: "",
    id = getInt(1),
    veiculoId = getInt(2),
    titulo = getString(3) ?: "",
    data = getString(4) ?: "",
    quilometragem = getInt(5),
    detalhe = getString(6) ?: "",
    valor = getDouble(7)
)

private fun BancoHelper.somar(sql: String, vararg argumentos: String): Double {
    readableDatabase.rawQuery(sql, argumentos).use { cursor ->
        return if (cursor.moveToFirst()) {
            cursor.getDouble(0)
        } else {
            0.0
        }
    }
}

fun BancoHelper.totalCombustivelDesde(dataInicial: String): Double {
    return somar(SQL_TOTAL_COMBUSTIVEL, dataInicial)
}

fun BancoHelper.totalServicosDesde(dataInicial: String): Double {
    return somar(SQL_TOTAL_SERVICOS, dataInicial)
}

fun BancoHelper.totalServicosPorTipoDesde(dataInicial: String): Map<String, Double> {
    val totaisPorTipo = linkedMapOf<String, Double>()

    readableDatabase.rawQuery(SQL_TOTAL_SERVICOS_POR_TIPO, arrayOf(dataInicial)).use { cursor ->
        while (cursor.moveToNext()) {
            val tipo = cursor.getString(0) ?: ""
            val total = cursor.getDouble(1)

            totaisPorTipo[tipo] = total
        }
    }

    return totaisPorTipo
}

fun BancoHelper.contarVeiculos(): Int {
    readableDatabase.rawQuery(SQL_CONTAR_VEICULOS, null).use { cursor ->
        return if (cursor.moveToFirst()) {
            cursor.getInt(0)
        } else {
            0
        }
    }
}

fun BancoHelper.consumoMedio(veiculoId: Int): Double? {
    var quilometragemInicial = 0
    var quilometragemFinal = 0
    var totalLitros = 0.0
    var quantidadeRegistros = 0

    readableDatabase.rawQuery(SQL_CONSUMO_MEDIO, arrayOf(veiculoId.toString())).use { cursor ->
        while (cursor.moveToNext()) {
            if (quantidadeRegistros == 0) {
                quilometragemInicial = cursor.getInt(0)
            } else {
                totalLitros += cursor.getDouble(1)
            }

            quilometragemFinal = cursor.getInt(0)
            quantidadeRegistros++
        }
    }

    return calcularConsumo(
        quilometragemInicial,
        quilometragemFinal,
        totalLitros,
        quantidadeRegistros
    )
}

private fun calcularConsumo(
    quilometragemInicial: Int,
    quilometragemFinal: Int,
    totalLitros: Double,
    quantidadeRegistros: Int
): Double? {
    if (quantidadeRegistros < 2 || totalLitros <= 0.0) {
        return null
    }

    return (quilometragemFinal - quilometragemInicial) / totalLitros
}

fun BancoHelper.totalGastosPorVeiculoDesde(dataInicial: String): List<GastoVeiculo> {
    val gastos = mutableListOf<GastoVeiculo>()

    readableDatabase.rawQuery(SQL_GASTOS_POR_VEICULO, arrayOf(dataInicial, dataInicial)).use { cursor ->
        while (cursor.moveToNext()) {
            gastos.add(
                GastoVeiculo(
                    apelido = cursor.getString(0) ?: "",
                    total = cursor.getDouble(1)
                )
            )
        }
    }

    return gastos
}

fun BancoHelper.tendenciaGastosUltimosMeses(quantidadeMeses: Int = 5): List<GastoMensal> {
    require(quantidadeMeses > 0) {
        "A quantidade de meses deve ser maior que zero."
    }

    val calendario = Calendar.getInstance()

    calendario.set(Calendar.DAY_OF_MONTH, 1)
    calendario.add(Calendar.MONTH, -(quantidadeMeses - 1))

    val dataCorte = calendario.formatarPrimeiroDiaDoMes()

    val valoresPorMes = buscarValoresPorMes(dataCorte)

    return criarListaGastosMensais(calendario, valoresPorMes, quantidadeMeses)
}

private fun Calendar.formatarPrimeiroDiaDoMes(): String {
    return String.format(
        Locale.US,
        "%04d-%02d-01",
        get(Calendar.YEAR),
        get(Calendar.MONTH) + 1
    )
}

private fun BancoHelper.buscarValoresPorMes(dataCorte: String): Map<String, Double> {
    val valoresPorMes = mutableMapOf<String, Double>()

    readableDatabase.rawQuery(SQL_TENDENCIA_GASTOS, arrayOf(dataCorte, dataCorte)).use { cursor ->
        while (cursor.moveToNext()) {
            val mes = cursor.getString(0) ?: continue
            val total = cursor.getDouble(1)

            valoresPorMes[mes] = total
        }
    }

    return valoresPorMes
}

private fun criarListaGastosMensais(
    calendario: Calendar,
    valoresPorMes: Map<String, Double>,
    quantidadeMeses: Int
): List<GastoMensal> {
    val resultado = mutableListOf<GastoMensal>()

    repeat(quantidadeMeses) {
        val mesAno = calendario.formatarAnoMes()
        val rotulo = calendario.formatarNomeMes()

        resultado.add(
            GastoMensal(
                mesAno = mesAno,
                rotulo = rotulo,
                total = valoresPorMes[mesAno] ?: 0.0
            )
        )

        calendario.add(Calendar.MONTH, 1)
    }

    return resultado
}

private fun Calendar.formatarAnoMes(): String {
    return String.format(
        Locale.US,
        "%04d-%02d",
        get(Calendar.YEAR),
        get(Calendar.MONTH) + 1
    )
}

private fun Calendar.formatarNomeMes(): String {
    return NOMES_MESES[get(Calendar.MONTH)]
}

fun BancoHelper.historicoConsumo(veiculoId: Int, limite: Int = 6): List<Double> {
    require(limite >= 0) {
        "O limite não pode ser negativo."
    }

    val registros = buscarRegistrosConsumo(veiculoId)

    if (registros.size < 2) {
        return emptyList()
    }

    val consumos = calcularHistoricoConsumo(registros)

    return consumos.takeLast(limite)
}

private fun BancoHelper.buscarRegistrosConsumo(veiculoId: Int): List<Pair<Int, Double>> {
    val registros = mutableListOf<Pair<Int, Double>>()

    readableDatabase.rawQuery(SQL_HISTORICO_CONSUMO, arrayOf(veiculoId.toString())).use { cursor ->
        while (cursor.moveToNext()) {
            registros.add(
                cursor.getInt(0) to cursor.getDouble(1)
            )
        }
    }

    return registros
}

private fun calcularHistoricoConsumo(registros: List<Pair<Int, Double>>): List<Double> {
    val consumos = mutableListOf<Double>()

    for (indice in 1 until registros.size) {
        val quilometragemAnterior = registros[indice - 1].first
        val (quilometragemAtual, litros) = registros[indice]

        val distancia = quilometragemAtual - quilometragemAnterior

        if (distancia > 0 && litros > 0.0) {
            consumos.add(distancia / litros)
        }
    }

    return consumos
}

private fun primeiroDia(mesesAtras: Int, ateJaneiro: Boolean = false): String {
    val calendario = Calendar.getInstance()

    calendario.set(Calendar.DAY_OF_MONTH, 1)

    if (ateJaneiro) {
        calendario.set(Calendar.MONTH, Calendar.JANUARY)
    } else {
        calendario.add(Calendar.MONTH, -mesesAtras)
    }

    return calendario.formatarPrimeiroDiaDoMes()
}

fun inicioDoMes(): String = primeiroDia(0)

fun inicioDosUltimosTresMeses(): String = primeiroDia(2)

fun inicioDoAno(): String = primeiroDia(0, ateJaneiro = true)

fun Double.emReais(): String {
    return NumberFormat.getCurrencyInstance(LOCALE_BR).format(this)
}

fun String.dataParaExibicao(): String {
    val partes = split("-")

    val formatoValido = partes.size == 3 &&
            partes[0].length == 4

    if (!formatoValido) {
        return this
    }

    val (ano, mes, dia) = partes

    return "$dia/$mes/$ano"
}

fun Atividade.paraView(inflater: LayoutInflater, pai: ViewGroup): View {
    val view = inflater.inflate(R.layout.item_atividade, pai, false)

    configurarVisualizacaoAtividade(view)

    return view
}

private fun Atividade.configurarVisualizacaoAtividade(view: View) {
    configurarTitulo(view)
    configurarDetalhes(view)
    configurarValor(view)
}

private fun Atividade.configurarTitulo(view: View) {
    view.findViewById<TextView>(R.id.tvAtividadeTitulo).text = titulo
}

private fun Atividade.configurarDetalhes(view: View) {
    val partes = mutableListOf(data.dataParaExibicao())

    if (quilometragem > 0) {
        partes.add("$quilometragem km")
    }

    if (detalhe.isNotBlank()) {
        partes.add(detalhe)
    }

    view.findViewById<TextView>(R.id.tvAtividadeDetalhe).text = partes.joinToString(" • ")
}

private fun Atividade.configurarValor(view: View) {
    view.findViewById<TextView>(R.id.tvAtividadeValor).text = valor.emReais()
}

private val NOMES_MESES = arrayOf(
    "Jan", "Fev", "Mar", "Abr",
    "Mai", "Jun", "Jul", "Ago",
    "Set", "Out", "Nov", "Dez"
)

private const val SQL_PROXIMA_MANUTENCAO = """
    SELECT s.id, s.veiculo_id, s.tipo, s.data, v.apelido
    FROM servicos s
    JOIN veiculos v ON s.veiculo_id = v.id
    WHERE s.data >= ?
    ORDER BY s.data ASC, s.id ASC
    LIMIT 1
"""

private const val SQL_LISTAR_ABASTECIMENTOS = """
    SELECT id, veiculo_id, litros, valor_litro, valor_total,
           quilometragem, posto, data
    FROM abastecimentos
    WHERE veiculo_id = ?
    ORDER BY data DESC, id DESC
"""

private const val SQL_TOTAL_COMBUSTIVEL = """
    SELECT COALESCE(SUM(valor_total), 0)
    FROM abastecimentos
    WHERE data >= ?
"""

private const val SQL_TOTAL_SERVICOS = """
    SELECT COALESCE(SUM(valor), 0)
    FROM servicos
    WHERE data >= ?
"""

private const val SQL_TOTAL_SERVICOS_POR_TIPO = """
    SELECT tipo, COALESCE(SUM(valor), 0)
    FROM servicos
    WHERE data >= ?
    GROUP BY tipo
"""

private const val SQL_CONTAR_VEICULOS = """
    SELECT COUNT(*)
    FROM veiculos
"""

private const val SQL_CONSUMO_MEDIO = """
    SELECT quilometragem, litros
    FROM abastecimentos
    WHERE veiculo_id = ?
    ORDER BY quilometragem ASC
"""

private const val SQL_GASTOS_POR_VEICULO = """
    SELECT
        v.apelido,
        COALESCE(SUM(g.valor), 0) AS total
    FROM veiculos v
    LEFT JOIN (
        SELECT veiculo_id, valor_total AS valor
        FROM abastecimentos
        WHERE data >= ?

        UNION ALL

        SELECT veiculo_id, valor AS valor
        FROM servicos
        WHERE data >= ?
    ) g ON v.id = g.veiculo_id
    GROUP BY v.id, v.apelido
    HAVING total > 0
    ORDER BY total DESC
"""

private const val SQL_TENDENCIA_GASTOS = """
    SELECT
        strftime('%Y-%m', data) AS mes,
        COALESCE(SUM(valor), 0)
    FROM (
        SELECT data, valor_total AS valor
        FROM abastecimentos
        WHERE data >= ?

        UNION ALL

        SELECT data, valor AS valor
        FROM servicos
        WHERE data >= ?
    )
    GROUP BY mes
"""

private const val SQL_HISTORICO_CONSUMO = """
    SELECT quilometragem, litros
    FROM abastecimentos
    WHERE veiculo_id = ?
    ORDER BY quilometragem ASC, id ASC
"""
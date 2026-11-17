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
    val sql = """
        SELECT s.id, s.veiculo_id, s.tipo, s.data, v.apelido
        FROM servicos s
        JOIN veiculos v ON s.veiculo_id = v.id
        WHERE s.data >= ?
        ORDER BY s.data ASC, s.id ASC
        LIMIT 1
    """.trimIndent()

    val cursor = readableDatabase.rawQuery(sql, arrayOf(dataHoje))
    var resultado: ManutencaoAgendada? = null
    if (cursor.moveToFirst()) {
        resultado = ManutencaoAgendada(
            servicoId = cursor.getInt(0),
            veiculoId = cursor.getInt(1),
            tipo = cursor.getString(2) ?: "",
            data = cursor.getString(3) ?: "",
            veiculoApelido = cursor.getString(4) ?: ""
        )
    }
    cursor.close()
    return resultado
}

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
    val cursor = readableDatabase.rawQuery(
        "SELECT id, veiculo_id, litros, valor_litro, valor_total, quilometragem, posto, data " +
                "FROM abastecimentos WHERE veiculo_id = ? ORDER BY data DESC, id DESC",
        arrayOf(veiculoId.toString())
    )
    while (cursor.moveToNext()) lista.add(cursor.paraAbastecimento())
    cursor.close()
    return lista
}

fun BancoHelper.listarAtividades(
    veiculoId: Int? = null,
    limite: Int? = 50,
    servicos: Boolean = true,
    abastecimentos: Boolean = true
    ): List<Atividade> {
    val filtro = if (veiculoId != null) " WHERE veiculo_id = ?" else ""
    val partes = mutableListOf<String>()
    val args = mutableListOf<String>()

    if (servicos) {
        partes.add(
            "SELECT 'servico' AS origem, id, veiculo_id, tipo AS titulo, data, " +
                    "COALESCE(quilometragem, 0) AS km, COALESCE(oficina, '') AS detalhe, " +
                    "COALESCE(valor, 0) AS valor FROM servicos$filtro"
        )
        if (veiculoId != null) args.add(veiculoId.toString())
    }
    if (abastecimentos) {
        partes.add(
            "SELECT 'abastecimento', id, veiculo_id, 'Abastecimento', data, quilometragem, " +
                    "printf('%.1f L', litros) || CASE WHEN posto IS NULL OR posto = '' THEN '' " +
                    "ELSE ' • ' || posto END, valor_total FROM abastecimentos$filtro"
        )
        if (veiculoId != null) args.add(veiculoId.toString())
    }
    if (partes.isEmpty()) return emptyList()

    val sql = partes.joinToString(" UNION ALL ") + " ORDER BY data DESC, id DESC" + (limite?.let { " LIMIT $it" } ?: "")
    val lista = mutableListOf<Atividade>()
    val cursor = readableDatabase.rawQuery(sql, args.toTypedArray())
    while (cursor.moveToNext()) {
        lista.add(
            Atividade(
                origem = cursor.getString(0) ?: "",
                id = cursor.getInt(1),
                veiculoId = cursor.getInt(2),
                titulo = cursor.getString(3) ?: "",
                data = cursor.getString(4) ?: "",
                quilometragem = cursor.getInt(5),
                detalhe = cursor.getString(6) ?: "",
                valor = cursor.getDouble(7)
            )
        )
    }
    cursor.close()
    return lista
}

private fun BancoHelper.somar(sql: String, vararg args: String): Double {
    val cursor = readableDatabase.rawQuery(sql, args.toList().toTypedArray())
    val total = if (cursor.moveToFirst()) cursor.getDouble(0) else 0.0
    cursor.close()
    return total
}

fun BancoHelper.totalCombustivelDesde(dataInicial: String): Double =
    somar("SELECT COALESCE(SUM(valor_total), 0) FROM abastecimentos WHERE data >= ?", dataInicial)

fun BancoHelper.totalServicosDesde(dataInicial: String): Double =
    somar("SELECT COALESCE(SUM(valor), 0) FROM servicos WHERE data >= ?", dataInicial)

fun BancoHelper.totalServicosPorTipoDesde(dataInicial: String): Map<String, Double> {
    val mapa = linkedMapOf<String, Double>()
    val cursor = readableDatabase.rawQuery(
        "SELECT tipo, COALESCE(SUM(valor), 0) FROM servicos WHERE data >= ? GROUP BY tipo",
        arrayOf(dataInicial)
    )
    while (cursor.moveToNext()) mapa[cursor.getString(0) ?: ""] = cursor.getDouble(1)
    cursor.close()
    return mapa
}

fun BancoHelper.contarVeiculos(): Int {
    val cursor = readableDatabase.rawQuery("SELECT COUNT(*) FROM veiculos", null)
    val total = if (cursor.moveToFirst()) cursor.getInt(0) else 0
    cursor.close()
    return total
}

fun BancoHelper.consumoMedio(veiculoId: Int): Double? {
    val cursor = readableDatabase.rawQuery(
        "SELECT quilometragem, litros FROM abastecimentos WHERE veiculo_id = ? ORDER BY quilometragem ASC",
        arrayOf(veiculoId.toString())
    )
    var kmInicial = 0
    var kmFinal = 0
    var litros = 0.0
    var contador = 0
    while (cursor.moveToNext()) {
        if (contador == 0) kmInicial = cursor.getInt(0) else litros += cursor.getDouble(1)
        kmFinal = cursor.getInt(0)
        contador++
    }
    cursor.close()
    return if (contador >= 2 && litros > 0) (kmFinal - kmInicial) / litros else null
}

fun BancoHelper.totalGastosPorVeiculoDesde(dataInicial: String): List<GastoVeiculo> {
    val sql = """
        SELECT v.apelido, COALESCE(SUM(g.valor), 0) AS total
        FROM veiculos v
        LEFT JOIN (
            SELECT veiculo_id, valor_total AS valor FROM abastecimentos WHERE data >= ?
            UNION ALL
            SELECT veiculo_id, valor AS valor FROM servicos WHERE data >= ?
        ) g ON v.id = g.veiculo_id
        GROUP BY v.id, v.apelido
        HAVING total > 0
        ORDER BY total DESC
    """.trimIndent()

    val lista = mutableListOf<GastoVeiculo>()
    val cursor = readableDatabase.rawQuery(sql, arrayOf(dataInicial, dataInicial))
    while (cursor.moveToNext()) {
        lista.add(GastoVeiculo(cursor.getString(0) ?: "", cursor.getDouble(1)))
    }
    cursor.close()
    return lista
}

fun BancoHelper.tendenciaGastosUltimosMeses(quantidadeMeses: Int = 5): List<GastoMensal> {
    val mesesNomes = arrayOf("Jan", "Fev", "Mar", "Abr", "Mai", "Jun", "Jul", "Ago", "Set", "Out", "Nov", "Dez")
    val resultado = mutableListOf<GastoMensal>()
    val cal = Calendar.getInstance()
    cal.set(Calendar.DAY_OF_MONTH, 1)
    cal.add(Calendar.MONTH, -(quantidadeMeses - 1))

    val dataCorte = String.format(Locale.US, "%04d-%02d-01", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)

    val sql = """
        SELECT strftime('%Y-%m', data) AS mes, COALESCE(SUM(valor), 0)
        FROM (
            SELECT data, valor_total AS valor FROM abastecimentos WHERE data >= ?
            UNION ALL
            SELECT data, valor AS valor FROM servicos WHERE data >= ?
        )
        GROUP BY mes
    """.trimIndent()

    val mapaValores = mutableMapOf<String, Double>()
    val cursor = readableDatabase.rawQuery(sql, arrayOf(dataCorte, dataCorte))
    while (cursor.moveToNext()) {
        cursor.getString(0)?.let { mes ->
            mapaValores[mes] = cursor.getDouble(1)
        }
    }
    cursor.close()

    for (i in 0 until quantidadeMeses) {
        val chave = String.format(Locale.US, "%04d-%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)
        val rotulo = mesesNomes[cal.get(Calendar.MONTH)]
        resultado.add(GastoMensal(chave, rotulo, mapaValores[chave] ?: 0.0))
        cal.add(Calendar.MONTH, 1)
    }

    return resultado
}

fun BancoHelper.historicoConsumo(veiculoId: Int, limite: Int = 6): List<Double> {
    val cursor = readableDatabase.rawQuery(
        "SELECT quilometragem, litros FROM abastecimentos WHERE veiculo_id = ? ORDER BY quilometragem ASC, id ASC",
        arrayOf(veiculoId.toString())
    )

    val registros = mutableListOf<Pair<Int, Double>>()
    while (cursor.moveToNext()) {
        registros.add(cursor.getInt(0) to cursor.getDouble(1))
    }
    cursor.close()

    if (registros.size < 2) return emptyList()

    val consumos = mutableListOf<Double>()
    for (i in 1 until registros.size) {
        val kmAnterior = registros[i - 1].first
        val (kmAtual, litros) = registros[i]
        val distancia = kmAtual - kmAnterior

        if (distancia > 0 && litros > 0.0) {
            consumos.add(distancia / litros)
        }
    }

    return consumos.takeLast(limite)
}

private fun primeiroDia(mesesAtras: Int, ateJaneiro: Boolean = false): String {
    val c = Calendar.getInstance()
    c.set(Calendar.DAY_OF_MONTH, 1)
    if (ateJaneiro) c.set(Calendar.MONTH, Calendar.JANUARY) else c.add(Calendar.MONTH, -mesesAtras)
    return String.format(Locale.US, "%04d-%02d-01", c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1)
}

fun inicioDoMes(): String = primeiroDia(0)
fun inicioDosUltimosTresMeses(): String = primeiroDia(2)
fun inicioDoAno(): String = primeiroDia(0, ateJaneiro = true)

fun Double.emReais(): String = NumberFormat.getCurrencyInstance(LOCALE_BR).format(this)

fun String.dataParaExibicao(): String {
    val p = split("-")
    return if (p.size == 3 && p[0].length == 4) "${p[2]}/${p[1]}/${p[0]}" else this
}

fun Atividade.paraView(inflater: LayoutInflater, pai: ViewGroup): View {
    val view = inflater.inflate(R.layout.item_atividade, pai, false)
    val partes = mutableListOf(data.dataParaExibicao())
    if (quilometragem > 0) partes.add("$quilometragem km")
    if (detalhe.isNotBlank()) partes.add(detalhe)

    view.findViewById<TextView>(R.id.tvAtividadeTitulo).text = titulo
    view.findViewById<TextView>(R.id.tvAtividadeDetalhe).text = partes.joinToString(" • ")
    view.findViewById<TextView>(R.id.tvAtividadeValor).text = valor.emReais()
    return view
}
package com.example.virabraking

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class BancoHelper private constructor(context: Context) :
    SQLiteOpenHelper(context, NOME_BANCO, null, VERSAO_BANCO) {
    companion object {
        private const val NOME_BANCO = "virabraking.db"
        private const val VERSAO_BANCO = 7
        private const val TABELA_VEICULOS = "veiculos"
        private const val TABELA_SERVICOS = "servicos"
        private const val TABELA_ABASTECIMENTOS = "abastecimentos"
        private const val TABELA_LEMBRETES = "lembretes"

        private const val COLUNA_ID = "id"
        private const val COLUNA_TIPO = "tipo"
        private const val COLUNA_APELIDO = "apelido"
        private const val COLUNA_MARCA = "marca"
        private const val COLUNA_MODELO = "modelo"
        private const val COLUNA_ANO = "ano"
        private const val COLUNA_PLACA = "placa"
        private const val COLUNA_QUILOMETRAGEM = "quilometragem"
        private const val COLUNA_FAVORITO = "favorito"
        private const val COLUNA_CAMINHO_FOTO = "caminho_foto"
        private const val COLUNA_CAMINHO_COMPROVANTE = "caminho_comprovante"

        private const val COLUNA_VEICULO_ID = "veiculo_id"
        private const val COLUNA_DATA = "data"
        private const val COLUNA_VALOR = "valor"
        private const val COLUNA_OFICINA = "oficina"
        private const val COLUNA_OBSERVACOES = "observacoes"

        private const val COLUNA_LITROS = "litros"
        private const val COLUNA_VALOR_LITRO = "valor_litro"
        private const val COLUNA_VALOR_TOTAL = "valor_total"
        private const val COLUNA_POSTO = "posto"

        private const val COLUNA_TITULO = "titulo"
        private const val COLUNA_DESCRICAO = "descricao"
        private const val COLUNA_HORA = "hora"

        private const val HORA_PADRAO = "12:00"

        private const val SQL_LISTAR_VEICULOS = """
            SELECT id, tipo, apelido, marca, modelo, ano, placa,
                   quilometragem, favorito, caminho_foto
            FROM veiculos
            ORDER BY favorito DESC, id DESC
        """

        private const val SQL_BUSCAR_VEICULO = """
            SELECT id, tipo, apelido, marca, modelo, ano, placa,
                   quilometragem, favorito, caminho_foto
            FROM veiculos
            WHERE id = ?
        """

        private const val SQL_BUSCAR_SERVICO = """
            SELECT id, veiculo_id, tipo, data, quilometragem,
                   valor, oficina, observacoes, caminho_comprovante
            FROM servicos
            WHERE id = ?
        """

        private const val SQL_LISTAR_LEMBRETES = """
            SELECT l.id, l.veiculo_id, l.titulo, l.descricao,
                   l.data, l.hora, v.apelido
            FROM lembretes l
            LEFT JOIN veiculos v ON l.veiculo_id = v.id
            ORDER BY l.data ASC, l.hora ASC, l.id ASC
        """

        private const val SQL_ATUALIZAR_QUILOMETRAGEM = """
            UPDATE veiculos
            SET quilometragem = MAX(COALESCE(quilometragem, 0), ?)
            WHERE id = ?
        """

        @Volatile
        private var instancia: BancoHelper? = null

        fun getInstance(context: Context): BancoHelper = instancia ?: synchronized(this) {
            instancia ?: BancoHelper(context.applicationContext).also {
                instancia = it
            }
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        criarTabelaVeiculos(db)
        criarTabelaServicos(db)
        criarTabelaAbastecimentos(db)
        criarTabelaLembretes(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            criarTabelaServicos(db)
        }

        if (oldVersion < 3) {
            criarTabelaAbastecimentos(db)
        }

        if (oldVersion < 4) {
            adicionarColunaFavorito(db)
        }

        if (oldVersion < 5) {
            adicionarColunaCaminhoFoto(db)
        }

        if (oldVersion < 6) {
            adicionarColunaCaminhoComprovante(db)
        }

        if (oldVersion < 7) {
            criarTabelaLembretes(db)
        }
    }

    private fun criarTabelaVeiculos(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE veiculos (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                tipo TEXT NOT NULL,
                apelido TEXT NOT NULL,
                marca TEXT,
                modelo TEXT,
                ano INTEGER,
                placa TEXT,
                quilometragem INTEGER,
                favorito INTEGER NOT NULL DEFAULT 0,
                caminho_foto TEXT
            )
            """.trimIndent()
        )
    }

    private fun criarTabelaServicos(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE servicos (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                veiculo_id INTEGER NOT NULL,
                tipo TEXT NOT NULL,
                data TEXT NOT NULL,
                quilometragem INTEGER,
                valor REAL,
                oficina TEXT,
                observacoes TEXT,
                caminho_comprovante TEXT,
                FOREIGN KEY (veiculo_id) REFERENCES veiculos(id)
            )
            """.trimIndent()
        )
    }

    private fun criarTabelaAbastecimentos(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE abastecimentos (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                veiculo_id INTEGER NOT NULL,
                litros REAL NOT NULL,
                valor_litro REAL NOT NULL,
                valor_total REAL NOT NULL,
                quilometragem INTEGER NOT NULL,
                posto TEXT,
                data TEXT NOT NULL,
                FOREIGN KEY (veiculo_id) REFERENCES veiculos(id)
            )
            """.trimIndent()
        )
    }

    private fun criarTabelaLembretes(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE lembretes (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                veiculo_id INTEGER,
                titulo TEXT NOT NULL,
                descricao TEXT,
                data TEXT NOT NULL,
                hora TEXT NOT NULL DEFAULT '12:00',
                FOREIGN KEY (veiculo_id) REFERENCES veiculos(id)
            )
            """.trimIndent()
        )
    }

    private fun adicionarColunaFavorito(db: SQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE veiculos ADD COLUMN favorito INTEGER NOT NULL DEFAULT 0"
        )
    }

    private fun adicionarColunaCaminhoFoto(db: SQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE veiculos ADD COLUMN caminho_foto TEXT"
        )
    }

    private fun adicionarColunaCaminhoComprovante(db: SQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE servicos ADD COLUMN caminho_comprovante TEXT"
        )
    }

    fun inserirVeiculo(veiculo: Veiculo): Long {
        val valores = ContentValues().apply {
            put(COLUNA_TIPO, veiculo.tipo)
            put(COLUNA_APELIDO, veiculo.apelido)
            put(COLUNA_MARCA, veiculo.marca)
            put(COLUNA_MODELO, veiculo.modelo)
            put(COLUNA_ANO, veiculo.ano)
            put(COLUNA_PLACA, veiculo.placa)
            put(COLUNA_QUILOMETRAGEM, veiculo.quilometragem)
            put(COLUNA_CAMINHO_FOTO, veiculo.caminhoFoto)
        }

        return writableDatabase.insert(TABELA_VEICULOS, null, valores)
    }

    fun listarVeiculos(): List<Veiculo> {
        val lista = mutableListOf<Veiculo>()

        readableDatabase.rawQuery(SQL_LISTAR_VEICULOS, null).use { cursor ->
            while (cursor.moveToNext()) {
                lista.add(cursorParaVeiculo(cursor))
            }
        }

        return lista
    }

    private fun cursorParaVeiculo(cursor: Cursor): Veiculo {
        return Veiculo(
            id = cursor.getInt(0),
            tipo = cursor.getString(1) ?: "",
            apelido = cursor.getString(2) ?: "",
            marca = cursor.getString(3) ?: "",
            modelo = cursor.getString(4) ?: "",
            ano = cursor.getInt(5),
            placa = cursor.getString(6) ?: "",
            quilometragem = cursor.getInt(7),
            favorito = cursor.getInt(8) == 1,
            caminhoFoto = cursor.getString(9)
        )
    }

    fun buscarVeiculo(id: Int): Veiculo? {
        readableDatabase.rawQuery(SQL_BUSCAR_VEICULO, arrayOf(id.toString())).use { cursor ->
            if (cursor.moveToFirst()) {
                return cursorParaVeiculo(cursor)
            }
        }

        return null
    }

    fun removerVeiculo(id: Int) {
        val db = writableDatabase

        db.beginTransaction()

        try {
            val argumentoId = arrayOf(id.toString())

            db.delete(
                TABELA_ABASTECIMENTOS,
                COLUNA_VEICULO_ID + " = ?",
                argumentoId
            )

            db.delete(
                TABELA_SERVICOS,
                COLUNA_VEICULO_ID + " = ?",
                argumentoId
            )

            db.delete(
                TABELA_LEMBRETES,
                COLUNA_VEICULO_ID + " = ?",
                argumentoId
            )

            db.delete(
                TABELA_VEICULOS,
                COLUNA_ID + " = ?",
                argumentoId
            )

            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun definirFavorito(id: Int, favorito: Boolean) {
        val valores = ContentValues().apply {
            put(COLUNA_FAVORITO, if (favorito) 1 else 0)
        }

        writableDatabase.update(
            TABELA_VEICULOS,
            valores,
            "$COLUNA_ID = ?",
            arrayOf(id.toString())
        )
    }

    fun inserirServico(servico: Servico): Long {
        val valores = ContentValues().apply {
            put(COLUNA_VEICULO_ID, servico.veiculoId)
            put(COLUNA_TIPO, servico.tipo)
            put(COLUNA_DATA, servico.data)
            put(COLUNA_QUILOMETRAGEM, servico.quilometragem)
            put(COLUNA_VALOR, servico.valor)
            put(COLUNA_OFICINA, servico.oficina)
            put(COLUNA_OBSERVACOES, servico.observacoes)
            put(COLUNA_CAMINHO_COMPROVANTE, servico.caminhoComprovante)
        }

        return writableDatabase.insert(TABELA_SERVICOS, null, valores)
    }

    fun buscarServico(id: Int): Servico? {
        readableDatabase.rawQuery(SQL_BUSCAR_SERVICO, arrayOf(id.toString())).use { cursor ->
            if (cursor.moveToFirst()) {
                return cursorParaServico(cursor)
            }
        }

        return null
    }

    private fun cursorParaServico(cursor: Cursor): Servico {
        return Servico(
            id = cursor.getInt(0),
            veiculoId = cursor.getInt(1),
            tipo = cursor.getString(2),
            data = cursor.getString(3),
            quilometragem = cursor.getInt(4),
            valor = cursor.getDouble(5),
            oficina = cursor.getString(6) ?: "",
            observacoes = cursor.getString(7) ?: "",
            caminhoComprovante = cursor.getString(8)
        )
    }

    fun removerServico(id: Int): Int {
        return writableDatabase.delete(TABELA_SERVICOS, "$COLUNA_ID = ?", arrayOf(id.toString()))
    }

    fun registrarAbastecimento(abastecimento: RegistroAbastecimento): Long {
        val db = writableDatabase

        db.beginTransaction()

        try {
            val valores = ContentValues().apply {
                put(COLUNA_VEICULO_ID, abastecimento.veiculoId)
                put(COLUNA_LITROS, abastecimento.litros)
                put(COLUNA_VALOR_LITRO, abastecimento.valorLitro)
                put(COLUNA_VALOR_TOTAL, abastecimento.valorTotal)
                put(COLUNA_QUILOMETRAGEM, abastecimento.quilometragem)
                put(COLUNA_POSTO, abastecimento.posto)
                put(COLUNA_DATA, abastecimento.data)
            }

            val id = db.insert(
                TABELA_ABASTECIMENTOS,
                null,
                valores
            )

            atualizarQuilometragemVeiculo(db, abastecimento.veiculoId, abastecimento.quilometragem)

            db.setTransactionSuccessful()

            return id
        } finally {
            db.endTransaction()
        }
    }

    private fun atualizarQuilometragemVeiculo(db: SQLiteDatabase, veiculoId: Int, quilometragem: Int) {
        db.execSQL(SQL_ATUALIZAR_QUILOMETRAGEM, arrayOf<Any>(quilometragem, veiculoId))
    }

    fun inserirLembrete(lembrete: Lembrete): Long {
        val valores = ContentValues().apply {
            put(COLUNA_VEICULO_ID, lembrete.veiculoId)
            put(COLUNA_TITULO, lembrete.titulo)
            put(COLUNA_DESCRICAO, lembrete.descricao)
            put(COLUNA_DATA, lembrete.data)
            put(COLUNA_HORA, lembrete.hora.ifBlank { HORA_PADRAO })
        }

        return writableDatabase.insert(TABELA_LEMBRETES, null, valores)
    }

    fun listarLembretes(): List<Lembrete> {
        val lista = mutableListOf<Lembrete>()

        readableDatabase.rawQuery(SQL_LISTAR_LEMBRETES, null).use { cursor ->
            while (cursor.moveToNext()) {
                lista.add(cursorParaLembrete(cursor))
            }
        }

        return lista
    }

    private fun cursorParaLembrete(cursor: Cursor): Lembrete {
        return Lembrete(
            id = cursor.getInt(0),
            veiculoId = if (cursor.isNull(1)) null else cursor.getInt(1),
            titulo = cursor.getString(2) ?: "",
            descricao = cursor.getString(3) ?: "",
            data = cursor.getString(4) ?: "",
            hora = cursor.getString(5) ?: HORA_PADRAO,
            veiculoApelido = cursor.getString(6)
        )
    }

    fun removerLembrete(id: Int): Int {
        return writableDatabase.delete(
            TABELA_LEMBRETES,
            "$COLUNA_ID = ?",
            arrayOf(id.toString())
        )
    }
}
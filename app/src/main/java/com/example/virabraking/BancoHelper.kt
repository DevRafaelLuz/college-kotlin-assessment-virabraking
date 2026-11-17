package com.example.virabraking

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class BancoHelper private constructor(context: Context) :
    SQLiteOpenHelper(context, "virabraking.db", null, 6) {

    companion object {
        @Volatile
        private var instancia: BancoHelper? = null

        fun getInstance(context: Context): BancoHelper =
            instancia ?: synchronized(this) {
                instancia ?: BancoHelper(context.applicationContext).also { instancia = it }
            }
    }
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE veiculos (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "tipo TEXT NOT NULL, " +
                    "apelido TEXT NOT NULL, " +
                    "marca TEXT, " +
                    "modelo TEXT, " +
                    "ano INTEGER, " +
                    "placa TEXT, " +
                    "quilometragem INTEGER, " +
                    "favorito INTEGER NOT NULL DEFAULT 0, " +
                    "caminho_foto TEXT)"
        )
        criarTabelaServicos(db)
        criarTabelaAbastecimentos(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            criarTabelaServicos(db)
        }
        if (oldVersion < 3) {
            criarTabelaAbastecimentos(db)
        }
        if (oldVersion < 4) {
            db.execSQL("ALTER TABLE veiculos ADD COLUMN favorito INTEGER NOT NULL DEFAULT 0")
        }
        if (oldVersion < 5) {
            db.execSQL("ALTER TABLE veiculos ADD COLUMN caminho_foto TEXT")
        }
        if (oldVersion < 6) {
            db.execSQL("ALTER TABLE servicos ADD COLUMN caminho_comprovante TEXT")
        }
    }

    private fun criarTabelaServicos(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE servicos (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "veiculo_id INTEGER NOT NULL, " +
                    "tipo TEXT NOT NULL, " +
                    "data TEXT NOT NULL, " +
                    "quilometragem INTEGER, " +
                    "valor REAL, " +
                    "oficina TEXT, " +
                    "observacoes TEXT, " +
                    "caminho_comprovante TEXT, " +
                    "FOREIGN KEY (veiculo_id) REFERENCES veiculos(id))"
        )
    }

    private fun criarTabelaAbastecimentos(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE abastecimentos (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "veiculo_id INTEGER NOT NULL, " +
                    "litros REAL NOT NULL, " +
                    "valor_litro REAL NOT NULL, " +
                    "valor_total REAL NOT NULL, " +
                    "quilometragem INTEGER NOT NULL, " +
                    "posto TEXT, " +
                    "data TEXT NOT NULL, " +
                    "FOREIGN KEY (veiculo_id) REFERENCES veiculos(id))"
        )
    }

    fun inserirVeiculo(veiculo: Veiculo): Long {
        val valores = ContentValues()
        valores.put("tipo", veiculo.tipo)
        valores.put("apelido", veiculo.apelido)
        valores.put("marca", veiculo.marca)
        valores.put("modelo", veiculo.modelo)
        valores.put("ano", veiculo.ano)
        valores.put("placa", veiculo.placa)
        valores.put("quilometragem", veiculo.quilometragem)
        valores.put("caminho_foto", veiculo.caminhoFoto)

        return writableDatabase.insert("veiculos", null, valores)
    }

    fun listarVeiculos(): List<Veiculo> {
        val lista = mutableListOf<Veiculo>()
        val cursor = readableDatabase.rawQuery(
            "SELECT id, tipo, apelido, marca, modelo, ano, placa, quilometragem, favorito, caminho_foto FROM veiculos ORDER BY favorito DESC, id DESC",
            null
        )

        while (cursor.moveToNext()) {
            lista.add(
                Veiculo(
                    id = cursor.getInt(0),
                    tipo = cursor.getString(1),
                    apelido = cursor.getString(2),
                    marca = cursor.getString(3) ?: "",
                    modelo = cursor.getString(4) ?: "",
                    ano = cursor.getInt(5),
                    placa = cursor.getString(6) ?: "",
                    quilometragem = cursor.getInt(7),
                    favorito = cursor.getInt(8) == 1,
                    caminhoFoto = cursor.getString(9)
                )
            )
        }

        cursor.close()
        return lista
    }

    fun buscarVeiculo(id: Int): Veiculo? {
        val cursor = readableDatabase.rawQuery(
            "SELECT id, tipo, apelido, marca, modelo, ano, placa, quilometragem, favorito, caminho_foto FROM veiculos WHERE id = ?",
            arrayOf(id.toString())
        )
        var veiculo: Veiculo? = null
        if (cursor.moveToFirst()) {
            veiculo = Veiculo(
                id = cursor.getInt(0),
                tipo = cursor.getString(1),
                apelido = cursor.getString(2),
                marca = cursor.getString(3) ?: "",
                modelo = cursor.getString(4) ?: "",
                ano = cursor.getInt(5),
                placa = cursor.getString(6) ?: "",
                quilometragem = cursor.getInt(7),
                favorito = cursor.getInt(8) == 1,
                caminhoFoto = cursor.getString(9)
            )
        }
        cursor.close()
        return veiculo
    }

    fun removerVeiculo(id: Int) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            db.delete("abastecimentos", "veiculo_id = ?", arrayOf(id.toString()))
            db.delete("servicos", "veiculo_id = ?", arrayOf(id.toString()))
            db.delete("veiculos", "id = ?", arrayOf(id.toString()))
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun definirFavorito(id: Int, favorito: Boolean) {
        val valores = ContentValues()
        valores.put("favorito", if (favorito) 1 else 0)
        writableDatabase.update("veiculos", valores, "id = ?", arrayOf(id.toString()))
    }

    fun inserirServico(servico: Servico): Long {
        val valores = ContentValues()
        valores.put("veiculo_id", servico.veiculoId)
        valores.put("tipo", servico.tipo)
        valores.put("data", servico.data)
        valores.put("quilometragem", servico.quilometragem)
        valores.put("valor", servico.valor)
        valores.put("oficina", servico.oficina)
        valores.put("observacoes", servico.observacoes)
        valores.put("caminho_comprovante", servico.caminhoComprovante)

        return writableDatabase.insert("servicos", null, valores)
    }

    fun registrarAbastecimento(abastecimento: RegistroAbastecimento): Long {
        val db = writableDatabase
        db.beginTransaction()
        try {
            val valores = ContentValues()
            valores.put("veiculo_id", abastecimento.veiculoId)
            valores.put("litros", abastecimento.litros)
            valores.put("valor_litro", abastecimento.valorLitro)
            valores.put("valor_total", abastecimento.valorTotal)
            valores.put("quilometragem", abastecimento.quilometragem)
            valores.put("posto", abastecimento.posto)
            valores.put("data", abastecimento.data)

            val id = db.insert("abastecimentos", null, valores)
            db.execSQL(
                "UPDATE veiculos SET quilometragem = MAX(COALESCE(quilometragem, 0), ?) WHERE id = ?",
                arrayOf<Any>(abastecimento.quilometragem, abastecimento.veiculoId)
            )
            db.setTransactionSuccessful()
            return id
        } finally {
            db.endTransaction()
        }
    }

    fun buscarServico(id: Int): Servico? {
        val cursor = readableDatabase.rawQuery(
            "SELECT id, veiculo_id, tipo, data, quilometragem, valor, oficina, observacoes, caminho_comprovante FROM servicos WHERE id = ?",
            arrayOf(id.toString())
        )
        var servico: Servico? = null
        if (cursor.moveToFirst()) {
            servico = Servico(
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
        cursor.close()
        return servico
    }

    fun removerServico(id: Int): Int {
        return writableDatabase.delete("servicos", "id = ?", arrayOf(id.toString()))
    }
}
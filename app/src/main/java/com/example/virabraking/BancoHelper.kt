package com.example.virabraking

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class BancoHelper(context: Context) : SQLiteOpenHelper(context, "virabraking.db", null, 3) {
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
                    "quilometragem INTEGER)"
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

        return writableDatabase.insert("veiculos", null, valores)
    }

    fun listarVeiculos(): List<Veiculo> {
        val lista = mutableListOf<Veiculo>()
        val cursor = readableDatabase.rawQuery(
            "SELECT id, tipo, apelido, marca, modelo, ano, placa, quilometragem FROM veiculos ORDER BY id DESC",
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
                    quilometragem = cursor.getInt(7)
                )
            )
        }

        cursor.close()
        return lista
    }

    fun buscarVeiculo(id: Int): Veiculo? {
        val cursor = readableDatabase.rawQuery(
            "SELECT id, tipo, apelido, marca, modelo, ano, placa, quilometragem FROM veiculos WHERE id = ?",
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
                quilometragem = cursor.getInt(7)
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

    fun inserirServico(servico: Servico): Long {
        val valores = ContentValues()
        valores.put("veiculo_id", servico.veiculoId)
        valores.put("tipo", servico.tipo)
        valores.put("data", servico.data)
        valores.put("quilometragem", servico.quilometragem)
        valores.put("valor", servico.valor)
        valores.put("oficina", servico.oficina)
        valores.put("observacoes", servico.observacoes)

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
}
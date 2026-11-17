package com.example.virabraking

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class RegistrarServico : AppCompatActivity() {

    private lateinit var spTipoServico: Spinner
    private lateinit var spVeiculo: Spinner
    private lateinit var etData: EditText
    private lateinit var etQuilometragem: EditText
    private lateinit var etValor: EditText
    private lateinit var etOficina: EditText
    private lateinit var etObservacoes: EditText

    private var veiculos = listOf<Veiculo>()
    private var caminhoComprovanteSelecionado: String? = null

    private val selecionarComprovanteLauncher =
        registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                caminhoComprovanteSelecionado = copiarImagemParaArmazenamentoInterno(uri)
                if (caminhoComprovanteSelecionado != null) {
                    findViewById<LinearLayout>(R.id.llComprovante).visibility = View.GONE
                    findViewById<ImageView>(R.id.ivComprovante).apply {
                        visibility = View.VISIBLE
                        setImageURI(android.net.Uri.fromFile(java.io.File(caminhoComprovanteSelecionado!!)))
                    }
                    exibirMensagem("Comprovante anexado")
                } else {
                    exibirMensagem("Erro ao anexar comprovante")
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registrar_servico)

        inicializarViews()
        configurarNavegacao()
        carregarVeiculos()
        configurarTiposServico()
        configurarSeletorData()
        configurarBotoes()
    }

    private fun inicializarViews() {
        spTipoServico = findViewById(R.id.sTipoServico)
        spVeiculo = findViewById(R.id.spVeiculo)
        etData = findViewById(R.id.etData)
        etQuilometragem = findViewById(R.id.etQuilometragem)
        etValor = findViewById(R.id.etValor)
        etOficina = findViewById(R.id.etOficina)
        etObservacoes = findViewById(R.id.etObservacoes)
    }

    private fun configurarNavegacao() {
        configurarDestino(R.id.llInicio, Inicio::class.java)
        configurarDestino(R.id.llVeiculos, Veiculos::class.java)
        configurarDestino(R.id.llAlertas, Alertas::class.java)
        configurarDestino(R.id.llRelatorios, Relatorios::class.java)
    }

    private fun configurarDestino(idView: Int, destino: Class<out AppCompatActivity>) {
        findViewById<LinearLayout>(idView).setOnClickListener {
            startActivity(Intent(this, destino))
            overridePendingTransition(0, 0);
        }
    }

    private fun carregarVeiculos() {
        veiculos = BancoHelper.getInstance(this).listarVeiculos()

        val adapter = criarAdapter(veiculos.map { it.apelido })
        spVeiculo.adapter = adapter

        selecionarVeiculoInicial()
        configurarAtualizacaoQuilometragem()
    }

    private fun criarAdapter(itens: List<String>): ArrayAdapter<String> {
        return ArrayAdapter(this, R.layout.item_spinner_tipo_servico, itens).apply {
            setDropDownViewResource(R.layout.item_spinner_dropdown)
        }
    }

    private fun selecionarVeiculoInicial() {
        val idInicial = intent.getIntExtra(
            Constantes.EXTRA_VEICULO_ID,
            Constantes.ID_INVALIDO
        )

        val posicaoInicial = veiculos.indexOfFirst {
            it.id == idInicial
        }

        if (posicaoInicial >= 0) {
            spVeiculo.setSelection(posicaoInicial)
        }
    }

    private fun configurarAtualizacaoQuilometragem() {
        if (veiculos.isEmpty()) return

        spVeiculo.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                atualizarQuilometragemVeiculoSelecionado(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        atualizarQuilometragemVeiculoSelecionado(spVeiculo.selectedItemPosition)
    }

    private fun atualizarQuilometragemVeiculoSelecionado(posicao: Int) {
        val veiculoSelecionado = veiculos.getOrNull(posicao) ?: return

        etQuilometragem.setText("${veiculoSelecionado.quilometragem}")
        etQuilometragem.error = null
    }

    private fun configurarTiposServico() {
        val opcoes = listOf("Selecione") + TipoServico.entries.map { it.descricao }

        spTipoServico.adapter = criarAdapter(opcoes)
    }

    private fun configurarSeletorData() {
        etData.setOnClickListener {
            exibirSeletorData()
        }
    }

    private fun exibirSeletorData() {
        val calendario = Calendar.getInstance()

        DatePickerDialog(
            this,
            { _, ano, mes, dia ->
                etData.setText(
                    String.format(
                        Locale.getDefault(),
                        "%02d/%02d/%04d",
                        dia,
                        mes + 1,
                        ano
                    )
                )
            },
            calendario.get(Calendar.YEAR),
            calendario.get(Calendar.MONTH),
            calendario.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun abrirAbastecimento() {
        val intent = Intent(this, Abastecimento::class.java)

        veiculos.getOrNull(spVeiculo.selectedItemPosition)?.let {
            intent.putExtra(Constantes.EXTRA_VEICULO_ID, it.id)
        }

        startActivity(intent)
        overridePendingTransition(0, 0);
    }

    private fun salvarServico() {
        if (veiculos.isEmpty()) {
            exibirMensagem("Cadastre um veículo primeiro")
            return
        }

        val tipoServico = obterTipoServicoSelecionado()
        if (tipoServico == null) {
            exibirMensagem("Selecione o tipo de serviço")
            return
        }

        val data = converterDataParaFormatoBanco(etData.text.toString())
        if (data == null) {
            exibirMensagem("Selecione uma data válida para o serviço")
            return
        }

        val servico = criarServico(tipoServico, data)

        val resultado = BancoHelper.getInstance(this)
            .inserirServico(servico)

        if (resultado == -1L) {
            exibirMensagem("Erro ao salvar o serviço", Toast.LENGTH_LONG)
            return
        }

        exibirMensagem("Serviço salvo")
        finish()
    }

    private fun obterTipoServicoSelecionado(): TipoServico? {
        val posicao = spTipoServico.selectedItemPosition

        if (posicao <= 0) {
            return null
        }

        return TipoServico.entries.getOrNull(posicao - 1)
    }

    private fun converterDataParaFormatoBanco(dataTexto: String): String? {
        val formatoEntrada = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply {
            isLenient = false
        }

        return try {
            val data = formatoEntrada.parse(dataTexto) ?: return null

            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(data)
        } catch (e: ParseException) {
            null
        }
    }

    private fun criarServico(tipoServico: TipoServico, data: String): Servico {
        val veiculoSelecionado = veiculos[spVeiculo.selectedItemPosition]
        val kmDigitada = etQuilometragem.text.toString().toIntOrNull()
        val kmFinal = kmDigitada ?: veiculoSelecionado.quilometragem

        return Servico(
            veiculoId = veiculoSelecionado.id,
            tipo = tipoServico.descricao,
            data = data,
            quilometragem = kmFinal,
            valor = etValor.text.toString().replace(",", ".").toDoubleOrNull() ?: 0.0,
            oficina = etOficina.text.toString().trim(),
            observacoes = etObservacoes.text.toString().trim(),
            caminhoComprovante = caminhoComprovanteSelecionado
        )
    }

    private fun exibirMensagem(mensagem: String, duracao: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(this, mensagem, duracao).show()
    }

    private fun configurarBotoes() {
        findViewById<Button>(R.id.btnSalvarManutencao).setOnClickListener {
            salvarServico()
        }

        findViewById<Button>(R.id.btnAbastecer).setOnClickListener {
            abrirAbastecimento()
        }

        findViewById<LinearLayout>(R.id.llComprovante).setOnClickListener {
            selecionarComprovanteLauncher.launch("image/*")
        }

        findViewById<ImageView>(R.id.ivComprovante).setOnClickListener {
            selecionarComprovanteLauncher.launch("image/*")
        }
    }

    private fun copiarImagemParaArmazenamentoInterno(uri: android.net.Uri): String? {
        return try {
            val pasta = java.io.File(filesDir, "comprovantes").apply { if (!exists()) mkdirs() }
            val arquivoDestino = java.io.File(pasta, "comp_${System.currentTimeMillis()}.jpg")
            contentResolver.openInputStream(uri)?.use { input ->
                arquivoDestino.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            arquivoDestino.absolutePath
        } catch (e: Exception) {
            null
        }
    }
}
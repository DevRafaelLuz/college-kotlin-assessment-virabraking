package com.example.virabraking

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class Alertas : AppCompatActivity() {

    private lateinit var llListaLembretes: LinearLayout
    private lateinit var tvSemLembretes: TextView

    private val solicitarPermissaoNotificacao = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alertas)

        inicializarViews()
        configurarNavegacao()
        verificarPermissaoNotificacao()
    }

    override fun onResume() {
        super.onResume()
        LembreteManager.reagendarTodosLembretesFuturos(this)
        carregarLembretes()
    }

    private fun inicializarViews() {
        llListaLembretes = findViewById(R.id.llListaLembretes)
        tvSemLembretes = findViewById(R.id.tvSemLembretes)

        findViewById<ImageButton>(R.id.iBtnAdicionarLembrete).setOnClickListener {
            exibirDialogNovoLembrete()
        }
    }

    private fun configurarNavegacao() {
        configurarDestino(R.id.llInicio, Inicio::class.java)
        configurarDestino(R.id.llVeiculos, Veiculos::class.java)
        configurarDestino(R.id.llAdicionar, RegistrarServico::class.java)
        configurarDestino(R.id.llRelatorios, Relatorios::class.java)
    }

    private fun configurarDestino(idView: Int, destino: Class<out AppCompatActivity>) {
        findViewById<LinearLayout>(idView).setOnClickListener {
            startActivity(Intent(this, destino))
            overridePendingTransition(0, 0)
        }
    }

    private fun verificarPermissaoNotificacao() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                solicitarPermissaoNotificacao.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun carregarLembretes() {
        val banco = BancoHelper.getInstance(this)
        val lembretes = banco.listarLembretes()

        llListaLembretes.removeAllViews()

        if (lembretes.isEmpty()) {
            tvSemLembretes.visibility = View.VISIBLE
            return
        }

        tvSemLembretes.visibility = View.GONE

        lembretes.forEach { lembrete ->
            val itemView = layoutInflater.inflate(R.layout.item_lembrete, llListaLembretes, false)

            val tvTitulo = itemView.findViewById<TextView>(R.id.tvLembreteTitulo)
            val tvDataHora = itemView.findViewById<TextView>(R.id.tvLembreteDataHora)
            val tvDescricao = itemView.findViewById<TextView>(R.id.tvLembreteDescricao)
            val btnExcluir = itemView.findViewById<ImageButton>(R.id.iBtnExcluirLembrete)

            val complementoVeiculo = if (!lembrete.veiculoApelido.isNullOrBlank()) {
                " • ${lembrete.veiculoApelido}"
            } else {
                ""
            }

            tvTitulo.text = "${lembrete.titulo}$complementoVeiculo"
            tvDataHora.text = "${lembrete.data.dataParaExibicao()} às ${lembrete.hora}"

            if (lembrete.descricao.isNotBlank()) {
                tvDescricao.text = lembrete.descricao
                tvDescricao.visibility = View.VISIBLE
            } else {
                tvDescricao.visibility = View.GONE
            }

            btnExcluir.setOnClickListener {
                confirmarExclusaoLembrete(lembrete)
            }

            llListaLembretes.addView(itemView)
        }
    }

    private fun confirmarExclusaoLembrete(lembrete: Lembrete) {
        AlertDialog.Builder(this)
            .setTitle("Remover Lembrete")
            .setMessage("Deseja remover o lembrete \"${lembrete.titulo}\"?")
            .setPositiveButton("Remover") { _, _ ->
                BancoHelper.getInstance(this).removerLembrete(lembrete.id)
                LembreteManager.cancelarNotificacao(this, lembrete.id)
                carregarLembretes()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun exibirDialogNovoLembrete() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_criar_lembrete, null)

        val etTitulo = dialogView.findViewById<EditText>(R.id.etTituloLembrete)
        val etDescricao = dialogView.findViewById<EditText>(R.id.etDescricaoLembrete)
        val spVeiculo = dialogView.findViewById<Spinner>(R.id.spVeiculoLembrete)
        val etData = dialogView.findViewById<EditText>(R.id.etDataLembrete)
        val etHora = dialogView.findViewById<EditText>(R.id.etHoraLembrete)

        val banco = BancoHelper.getInstance(this)
        val veiculos = banco.listarVeiculos()

        val opcoesVeiculos = listOf("Nenhum veículo vinculado") + veiculos.map { it.apelido }
        val adapter = ArrayAdapter(this, R.layout.item_spinner_tipo_servico, opcoesVeiculos).apply {
            setDropDownViewResource(R.layout.item_spinner_dropdown)
        }
        spVeiculo.adapter = adapter

        val calSelecionado = Calendar.getInstance()
        val formatoDataExibicao = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val formatoDataBanco = SimpleDateFormat("yyyy-MM-dd", Locale.US)

        etData.setText(formatoDataExibicao.format(calSelecionado.time))
        var horaSelecionada = "12:00"
        etHora.setText(horaSelecionada)

        etData.setOnClickListener {
            DatePickerDialog(
                this,
                { _, ano, mes, dia ->
                    calSelecionado.set(ano, mes, dia)
                    etData.setText(formatoDataExibicao.format(calSelecionado.time))
                },
                calSelecionado.get(Calendar.YEAR),
                calSelecionado.get(Calendar.MONTH),
                calSelecionado.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        etHora.setOnClickListener {
            TimePickerDialog(
                this,
                { _, hora, minuto ->
                    horaSelecionada = String.format(Locale.US, "%02d:%02d", hora, minuto)
                    etHora.setText(horaSelecionada)
                },
                12,
                0,
                true
            ).show()
        }

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Salvar") { _, _ ->
                val titulo = etTitulo.text.toString().trim()
                if (titulo.isBlank()) {
                    Toast.makeText(this, "Informe o título do lembrete", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val veiculoId = if (spVeiculo.selectedItemPosition > 0) {
                    veiculos.getOrNull(spVeiculo.selectedItemPosition - 1)?.id
                } else {
                    null
                }

                val dataBanco = formatoDataBanco.format(calSelecionado.time)
                val descricao = etDescricao.text.toString().trim()

                val lembrete = Lembrete(
                    veiculoId = veiculoId,
                    titulo = titulo,
                    descricao = descricao,
                    data = dataBanco,
                    hora = horaSelecionada
                )

                val idSalvo = banco.inserirLembrete(lembrete)

                if (idSalvo != -1L) {
                    val msgNotif = descricao.ifBlank { "Lembrete: $titulo" }
                    LembreteManager.agendarNotificacao(
                        context = this,
                        id = idSalvo.toInt(),
                        titulo = titulo,
                        mensagem = msgNotif,
                        dataStr = dataBanco,
                        horaStr = horaSelecionada
                    )
                    Toast.makeText(this, "Lembrete criado com sucesso!", Toast.LENGTH_SHORT).show()
                    carregarLembretes()
                } else {
                    Toast.makeText(this, "Erro ao criar lembrete", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}

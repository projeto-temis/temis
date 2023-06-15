<template>
  <div>
    <table class="table table-striped">
      <thead>
        <tr>
          <th>Participante</th>
          <th>Situação</th>
          <th>Just.</th>
          <th style="text-align:center;">Deferir</th>
          <th style="text-align:center;">Indeferir</th>
          <th style="text-align:center;">Aprovar</th>
          <th style="text-align:center;">Reprovar</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="e in linhas" :key="e.id">
          <td>{{ e.nome }}</td>
          <td>{{ e.situacao }}</td>
          <td>{{ e.justificativa }}</td>
          <td style="text-align:center;">
            <atividade-acao :action="e.acoes" slug="deferir" />
          </td>
          <td style="text-align:center;">
            <atividade-acao :action="e.acoes" slug="indeferir" />
          </td>
          <td style="text-align:center;">
            <atividade-acao :action="e.acoes" slug="aprovar" />
          </td>
          <td style="text-align:center;">
            <atividade-acao :action="e.acoes" slug="reprovar" />
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>
<script>
import AtividadeAcao from "./AtividadeAcao"

export default {
  props: {
    event: { type: Array },
  },
  computed: {
    linhas: function () {
      const l = [];
      const map = {}
      const mapTodos = {}

      // Cria uma linha para cada participante
      this.event.forEach((evt) => {
        if (
          evt.evento
          && !evt.idCanceladora
          && !evt.idDesabilitadora
        ) {
          if (evt.tipo === 'Inscrição de Participante') {
            const linha = {
              id: evt.id,
              nome: evt.descr,
              acoes: [],
              relacionados: []
            }
            if (evt.action) linha.acoes = [...linha.acoes, ...evt.action.filter(i => i.slug !== 'excluir' && i.slug !== 'cancelar')]
            map[evt.id] = linha
            l.push(linha);
          }
          mapTodos[evt.id] = evt
        }
      });

      // Copia as ações de todos os eventos relacionados
      this.event.forEach((evt) => {
        if (
          evt.evento
          && !evt.idCanceladora
          && !evt.idDesabilitadora
          && evt.idReferente
        ) {
          let linha = map[evt.idReferente]
          // relacionados indiretamente
          if (!linha && mapTodos[evt.idReferente].idReferente)
            linha = map[mapTodos[evt.idReferente].idReferente]
          if (evt.action) linha.acoes = [...linha.acoes, ...evt.action.filter(i => i.slug !== 'excluir' && i.slug !== 'cancelar')]
          linha.relacionados = [...linha.relacionados, evt]
        }
      });

      l.forEach(linha => {
        if (linha.relacionados.some(e => e.evento.kind === 'CaeEventoDeAtividadeAprovacao')) linha.situacao = 'Aprovado'
        else if (linha.relacionados.some(e => e.evento.kind === 'CaeEventoDeAtividadeReprovacao')) {
          linha.situacao = 'Reprovado'
          linha.justificativa = linha.relacionados.filter(e => e.evento.kind === 'CaeEventoDeAtividadeReprovacao')[0].evento.texto
        }
        else if (linha.relacionados.some(e => e.evento.kind === 'CaeEventoDeAtividadeIndeferimento')) {
          linha.situacao = 'Indeferido'
          linha.justificativa = linha.relacionados.filter(e => e.evento.kind === 'CaeEventoDeAtividadeIndeferimento')[0].evento.texto
        }
        else if (linha.relacionados.some(e => e.evento.kind === 'CaeEventoDeAtividadeDeferimento')) linha.situacao = 'Deferido'
        else linha.situacao = 'A deferir'
      })
      return l;
    },
  },
  methods: {
    actMiniTitle: function (name, active, explanation) {
      return name + ": " + explanation;
    },
  },
  components: {
    "atividade-acao": AtividadeAcao,
  },
}
</script>

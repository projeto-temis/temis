<template>
  <div>
    <fieldset class="juia juia-title">
      <div class="row align-items-center">
        <div class="col col-12">
          <big class="juia">Atividades</big>
        </div>
      </div>
    </fieldset>
    <div class="row">
      <div class="col col-sm-12" v-if="errormsg">
        <p class="alert alert-danger"><strong>Erro!</strong> {{ errormsg }}</p>
      </div>
    </div>

    <div class="row mt-3" v-if="carregando &amp;&amp; primeiraCarga">
      <div class="col col-12">
        <p class="alert alert-warning">
          <strong>Aguarde,</strong> carregando...
        </p>
      </div>
    </div>

    <div class="row mt-3" v-if="!carregando &amp;&amp; filtrados.length == 0">
      <div class="col col-12">
        <p class="alert alert-warning">
          <strong>Atenção!</strong> Lista vazia.
        </p>
      </div>
    </div>

    <div class="row" v-if="filtrados.length > 0">
      <div class="col-sm-12">
        <table class="table table-sm table-borderless">
          <thead>
            <tr class="table-head">
              <th style="text-align: center">Data</th>
              <th>Tema</th>
              <th>Tipo</th>
              <th>Comissão</th>
              <th>Período</th>
              <th>Local</th>
              <th></th>
              <th v-show="filtradosTemAlgumErro"></th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="f in filtrados" :key="'grupo-row' + f.codigo">
              <td style="text-align: center">
                <input type="checkbox" v-model="f.checked" :disabled="f.disabled" />
              </td>
              <td class="d-none d-md-block" :title="f.datahora">
                {{ f.tempoRelativo }}
              </td>
              <td>
                <router-link :to="{ name: f.tipo + 'Show', params: { key: f.codigo } }">{{ f.sigla }}</router-link><span
                  class="d-inline d-md-none"> - {{ f.descr }}</span>
              </td>
              <td class="d-none d-md-block">{{ f.descr }}</td>
              <td>{{ f.origem }}</td>
              <td class="d-none d-md-block" style="padding: 0;">
                <painel-tags :list="f.list" />
              </td>
              <td v-show="filtradosTemAlgumErro" style="color: red">
                {{ f.errormsg }}
              </td>
            </tr>
            <tr v-if="f.grupoEspacar" class="table-group" :key="'grupo-space' + f.codigo">
              <th colspan="6" class="pb-2 pb-0 pl-0"></th>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
    <p class="alert alert-success" v-if="acessos &amp;&amp; acessos.length >= 1">
      Último acesso em {{ acessos[1].datahora }} no endereço
      {{ acessos[1].ip }}.
    </p>
  </div>
</template>

<script>
import UtilsBL from "../utils/utils";
import PainelTags from "../components/PainelTags";

export default {
  components: { PainelTags },

  mounted() {
    this.errormsg = undefined;
    console.log(this.$route);

    setTimeout(() => {
      this.carregarMesa();
      //   if (this.$route.params.exibirAcessoAnterior) this.carregarAcessos()
    });
  },

  data() {
    return {
      mesa: undefined,
      filtro: undefined,
      lista: [],
      todos: {},
      carregando: false,
      primeiraCarga: true,
      acessos: [],
      errormsg: undefined,
    };
  },

  computed: {
    filtrados: function () {
      var a = this.lista;
      var grupo;
      var odd = false;
      a = UtilsBL.filtrarPorSubstring(
        a,
        this.filtro,
        "grupoNome,tempoRelativo,sigla,codigo,descr,origem,situacao,errormsg,list.nome".split(
          ","
        )
      );
      a = a.filter(function (item) {
        return item.grupo !== "NENHUM";
      });
      for (var i = 0; i < a.length; i++) {
        a[i].grupoExibir = a[i].grupo !== grupo;
        grupo = a[i].grupo;
        if (a[i].grupoExibir) odd = false;
        if (a[i].grupoExibir && i > 0) a[i - 1].grupoEspacar = true;
        odd = !odd;
        a[i].odd = odd;
      }
      return a;
    },

    filtradosTemAlgumErro: function () {
      if (!this.filtrados || this.filtrados.length === 0) return false;
      for (var i = 0; i < this.filtrados.length; i++) {
        if (this.filtrados[i].errormsg) return true;
      }
      return false;
    },

    filtradosEAnotaveis: function () {
      return this.filtrados.filter((item) => item.podeAnotar);
    },

    filtradosEAssinaveis: function () {
      return this.filtrados.filter((item) => item.podeAssinar);
    },

    filtradosETramitaveis: function () {
      return this.filtrados.filter((item) => item.podeTramitar);
    },

    filtradosEMarcadosEAnotaveis: function () {
      return this.filtradosEAnotaveis.filter(function (item) {
        return item.checked;
      });
    },

    filtradosEMarcadosEAssinaveis: function () {
      return this.filtradosEAssinaveis.filter(function (item) {
        return item.checked;
      });
    },

    filtradosEMarcadosETramitaveis: function () {
      return this.filtradosETramitaveis.filter(function (item) {
        return item.checked;
      });
    },
  },

  methods: {
    carregarMesa: function () {
      this.carregando = true;
      var erros = {};
      if (this.lista && this.lista.length > 0) {
        for (var i = 0; i < this.lista.length; i++) {
          erros[this.lista[i].codigo] = this.lista[i].errormsg;
        }
      }
      this.$http.get("painel", { block: true }).then(
        (response) => {
          this.carregando = false;
          this.lista.length = 0;
          var list = response.data.list;
          for (var i = 0; i < list.length; i++) {
            list[i].errormsg = erros[list[i].codigo];
            this.lista.push(this.fixItem(list[i]));
          }
          this.primeiraCarga = false;
        },
        (error) => {
          this.carregando = false;
          UtilsBL.errormsg(error, this);
        }
      );
    },

    // carregarAcessos: function() {
    //   this.acessos.length = 0
    //   this.$http.get('acessos').then(
    //     response => {
    //       var list = response.data.list
    //       for (var i = 0; i < list.length; i++) {
    //         list[i].datahora = UtilsBL.formatJSDDMMYYYY_AS_HHMM(list[i].datahora)
    //         this.acessos.push(list[i])
    //       }
    //     },
    //     error => {
    //       UtilsBL.errormsg(error, this)
    //     }
    //   )
    // },

    fixItem: function (item) {
      UtilsBL.applyDefauts(item, {
        checked: false,
        disabled: false,
        grupo: undefined,
        grupoNome: undefined,
        grupoExibir: undefined,
        grupoEspacar: undefined,
        datahora: undefined,
        datahoraFormatada: undefined,
        sigla: undefined,
        codigo: undefined,
        descr: undefined,
        origem: undefined,
        situacao: undefined,
        errormsg: undefined,
        odd: undefined,
      });
      if (item.datahora !== undefined) {
        item.datahoraFormatada = UtilsBL.formatJSDDMMYYYYHHMM(item.datahora);
      }
      return item;
    },

    // assinarComSenhaEmLote: function() {
    //   var a = this.filtradosEMarcadosEAssinaveis
    //   // Bus.$emit('iniciarAssinaturaComSenha', a, this.carregarMesa)
    //   Bus.$emit('assinarComSenha', a, undefined, undefined, this.carregarMesa)
    // },

    // anotarEmLote: function() {
    //   var a = this.filtradosEMarcadosEAnotaveis
    //   Bus.$emit('iniciarAnotacao', a)
    // },

    // tramitarEmLote: function() {
    //   var a = this.filtradosEMarcadosETramitaveis
    //   Bus.$emit('iniciarTramite', a, this.carregarMesa)
    // },

    //     marcarTodos: function(grupo) {
    //       var docs = this.filtrados
    //       for (var i = 0; i < docs.length; i++) {
    //         var doc = docs[i]
    //         if (doc.grupo === grupo) doc.checked = this.todos[grupo]
    //       }
    //     },

    //     mostrarDocumento: function(item, disposition) {
    //       var form = document.createElement('form')
    //       form.action =
    //         this.$parent.test.properties['siga-le.assijus.endpoint'] +
    //         '/api/v1/view' +
    //         (disposition === 'attachment' ? '?disposition=attachment' : '')
    //       form.method = 'POST'
    //       form.target = '_blank'
    //       form.style.display = 'none'

    //       var cpf = document.createElement('input')
    //       cpf.type = 'text'
    //       cpf.name = 'cpf'
    //       cpf.value = this.$parent.jwt.cpf

    //       var system = document.createElement('input')
    //       system.type = 'text'
    //       system.name = 'system'
    //       system.value = item.docsystem

    //       var docid = document.createElement('input')
    //       docid.type = 'text'
    //       docid.name = 'id'
    //       docid.value = item.docid

    //       var docsecret = document.createElement('input')
    //       docsecret.type = 'text'
    //       docsecret.name = 'secret'
    //       docsecret.value = item.docsecret

    //       var submit = document.createElement('input')
    //       submit.type = 'submit'
    //       submit.id = 'submitView'

    //       form.appendChild(cpf)
    //       form.appendChild(system)
    //       form.appendChild(docid)
    //       form.appendChild(docsecret)
    //       form.appendChild(submit)
    //       document.body.appendChild(form)

    //       /* global $ */
    //       $('#submitView').click()

    //       document.body.removeChild(form)
    //     },

    //     criarAssinavel: function(item) {
    //       return {
    //         id: item.docid,
    //         system: item.docsystem,
    //         code: item.codigo,
    //         descr: item.docdescr,
    //         kind: item.dockind,
    //         origin: 'Balcão Virtual'
    //       }
    //     },

    //     assinarDocumento: function(item) {
    //       this.chamarAssijus([this.criarAssinavel(item)])
    //     },

    //     assinarDocumentos: function() {
    //       var list = []
    //       for (var i = 0; i < this.filtradosEMarcadosEAssinaveis.length; i++) {
    //         list.push(this.criarAssinavel(this.filtradosEMarcadosEAssinaveis[i]))
    //       }
    //       if (list.length > 0) this.chamarAssijus(list)
    //     },

    //     chamarAssijus: function(list) {
    //       var json = JSON.stringify({ list: list })
    //       this.$http
    //         .post(
    //           this.$parent.test.properties['siga-le.assijus.endpoint'] +
    //             '/api/v1/store',
    //           { payload: json },
    //           { block: true }
    //         )
    //         .then(
    //           response => {
    //             var callback = window.location.href + ''
    //             console.log(callback)
    //             window.location.href =
    //               this.$parent.test.properties['siga-le.assijus.endpoint'] +
    //               '/?endpointautostart=true&endpointlistkey=' +
    //               response.data.key +
    //               '&endpointcallback=' +
    //               encodeURI(callback).replace('#', '__hashsign__')
    //           },
    //           error => UtilsBL.errormsg(error, this)
    //         )
    //     },

    //     editar: function() {
    //       this.$refs.etiqueta.show()
    //     },

    //     exibirProcessosMultiplos: function() {
    //       this.$refs.processosMultiplos.show()
    //     },

    //     acrescentarProcessosNaLista: function(arr) {
    //       if (!arr || arr.length === 0) return
    //       this.pasta = 'inbox'
    //       for (var i = 0; i < arr.length; i++) {
    //         if (arr[i] === '') continue
    //         var p = this.fixProcesso({
    //           numero: arr[i],
    //           inbox: true
    //         })
    //         this.processos.push(p)
    //       }
    //       this.validarEmLoteSilenciosamente()
    //     }
  },
};
</script>

<style scoped>
.destaque {
  color: red;
}

.td-middle {
  vertical-align: middle;
}

table .table-group th {
  border-top: 0;
}

table .table-head th {
  border-top: 0;
}

.odd {
  background-color: rgba(0, 0, 0, 0.05);
}

.xrp-label-container {
  margin-top: 4px;
  margin-bottom: 0px;
}

.xrp-label {
  font-size: 13px;
  margin-bottom: 4px;
  margin-right: 8px;
  line-height: 1.1;
  padding-left: 7px;
  padding-right: 7px;
  border-radius: 0px;
  border: 1px solid #ccc;
}
</style>

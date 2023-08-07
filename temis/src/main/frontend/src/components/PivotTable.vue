<template>
  <div>
    <vue-pivottable-ui v-model="config" :data="pivotData" :locale="locale" :locales="locales" :renderers="renderers"
      :rendererName="rendererName" :aggregatorName="aggregatorName" :aggregators="aggregators">
      <!-- Slot ColGroup -->
      <template v-slot:colGroup>
        <colgroup>
          <col :width="300">
          <col>
        </colgroup>
      </template>
      <!-- Slot Output -->
      <!-- <template v-slot:output>
                  <div v-if="loading">
                    loading...
                  </div>
                </template> -->
      <div v-if="loading" slot="output">
        loading...
      </div>

      <!-- Scoped Slot PvtAttr -->
      <template v-slot:pvtAttr="{ name }">
        {{ name }}
      </template>

      <!-- Scoped Slot Output -->
      <!-- <template v-if="!loading" v-slot:output="{ pivotData }">
                  <div v-if="!viewTable">
                    <button @click="viewTable = !viewTable">
                      View Table
                    </button>
                    <button @click="otherAction(pivotData)">
                      Other action
                    </button>
                  </div>
                  <template v-else>
                    <table-renderer
                      v-if="pivotData.props.rendererName === 'Table'"
                      :data="pivotData.props.data"
                      :props="pivotData.props"
                    >
                    </table-renderer>
                    <heatmap-renderer
                      v-if="pivotData.props.rendererName === 'Table Heatmap'"
                      :data="pivotData.props.data"
                      :props="pivotData.props"
                    >
                    </heatmap-renderer>
                  </template>
                </template> -->

    </vue-pivottable-ui>
  </div>
</template>
<script>
import { VuePivottableUi, PivotUtilities, Renderer } from 'vue-pivottable'
import PlotlyRenderer from '@vue-pivottable/plotly-renderer'
import ScrollRenderer from '@vue-pivottable/scroll-renderer'
import './pivottable-bootstrap-style.css'
import { scaleLinear } from 'd3-scale'

export default {
  name: "pivot-table",
  components: {
    VuePivottableUi
    // TableRenderer,
    // HeatmapRenderer
    // VuePivottable
  },
  props: {
    pivotData: { type: Array },
  },
  data() {
    return {
      viewTable: false,
      // fix issue #27
      valueFilter: {
        Meal: {
          Dinner: true
        }
      },
      config: {},
      filteredData: [],
      // Apenas para ficar com o exemplo, mas não são usados.
      asyncFields: ['Unused 1'],
      attributes: ['Unused 1', 'unix', 'date', 'symbol', 'open', 'high', 'low', 'close', 'Volume BTC', 'Volume USD'],
      rows: ['date'],
      cols: ['symbol'],
      vals: ['Volume BTC'],
      disabledFromDragDrop: [], // ['Payer Gender'],
      hiddenFromDragDrop: [],
      sortonlyFromDragDrop: [], // ['Party Size'],
      pivotColumns: ['Meal', 'Payer Smoker', 'Day of Week', 'Payer Gender', 'Party Size'],
      loading: false,
      aggregatorName: 'Soma',
      rendererName: 'Tabela',
      locale: 'pt'
    }
  },
  computed: {
    tableOptions() {
      return {
        clickCallback: function (e, value, filters, pivotData) {
          const values = []
          pivotData.forEachMatchingRecord(filters,
            function (record) {
              values.push(Object.values(record))
            }
          )
          alert(values.join('\n'))
        }
      }
    },
    sorters() {
      return {
        'Day of Week': PivotUtilities.sortAs(['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday'])
      }
    },
    locales() {
      return {
        pt: {
          aggregators: this.aggregators,
          localeStrings: {
            renderError: "Ocorreu um erro ao renderizar os resultados da Tabela Dinâmica.",
            computeError: "Ocorreu um erro ao calcular os resultados da Tabela Dinâmica.",
            uiRenderError: "Ocorreu um erro ao renderizar a interface da Tabela Dinâmica.",
            selectAll: "Selecionar Tudo",
            selectNone: "Selecionar Nada",
            tooMany: "(muitos para listar)",
            filterResults: "Filtrar valores",
            totals: "Totais",
            vs: "vs",
            by: "por",
            cancel: "Cancelar",
            only: "apenas"
          }
        }
      }
    },
    aggregators() {
      const usFmt = PivotUtilities.numberFormat()
      const usFmtInt = PivotUtilities.numberFormat({ digitsAfterDecimal: 0 })
      const usFmtPct = PivotUtilities.numberFormat({
        digitsAfterDecimal: 1,
        scaler: 100,
        suffix: '%'
      })

      return ((tpl) => ({
        Contagem: tpl.count(usFmtInt),
        'Contagem de Valores Únicos': tpl.countUnique(usFmtInt),
        'Listagem de Valores Únicos': tpl.listUnique(', '),
        'Soma': tpl.sum(usFmt),
        'Soma de Inteiros': tpl.sum(usFmtInt),
        'Média': tpl.average(usFmt),
        'Mediana': tpl.median(usFmt),
        'Variância da Amostra': tpl.var(1, usFmt),
        'Desvio Padrão da Amostra': tpl.stdev(1, usFmt),
        'Mínimo': tpl.min(usFmt),
        'Máximo': tpl.max(usFmt),
        'Primeiro': tpl.first(usFmt),
        'Último': tpl.last(usFmt),
        'Somatório sobre Somatório': tpl.sumOverSum(usFmt),
        'Soma como Fração do Total': tpl.fractionOf(tpl.sum(), 'total', usFmtPct),
        'Soma como Fração das Linhas': tpl.fractionOf(tpl.sum(), 'row', usFmtPct),
        'Soma como Fração das Colunas': tpl.fractionOf(tpl.sum(), 'col', usFmtPct),
        'Contagem como Fração do Total': tpl.fractionOf(tpl.count(), 'total', usFmtPct),
        'Contagem como Fração das Linhas': tpl.fractionOf(tpl.count(), 'row', usFmtPct),
        'Contagem como Fração das Colunas': tpl.fractionOf(tpl.count(), 'col', usFmtPct)
      })
      )(PivotUtilities.aggregatorTemplates)
    },
    renderers() {
      const TableRenderer = Renderer.TableRenderer
      return (() => ({
        Tabela: TableRenderer.Table,
        // 'Scroll Table': ScrollRenderer.Table,
        'Tabela Heatmap': ScrollRenderer['Table Heatmap'],
        'Tabela de Calor': TableRenderer['Table Heatmap'],
        'Tabela de Calor em Colunas': TableRenderer['Table Col Heatmap'],
        'Tabela de Calor em Linhas': TableRenderer['Table Row Heatmap'],
        'Variáveis Separadas por Tab': TableRenderer['Export Table TSV'],
        'Gráfico de Colunas Agrupadas': PlotlyRenderer['Grouped Column Chart'],
        'Gráfico de Colunas Empilhadas': PlotlyRenderer['Stacked Column Chart'],
        'Gráfico de Barras Agrupadas': PlotlyRenderer['Grouped Bar Chart'],
        'Gráfico de Barras Empilhadas': PlotlyRenderer['Stacked Bar Chart'],
        'Gráfico de Linhas': PlotlyRenderer['Line Chart'],
        'Gráfico de Pontos': PlotlyRenderer['Dot Chart'],
        'Gráfico de Áreas': PlotlyRenderer['Area Chart'],
        'Gráfico de Dispersão': PlotlyRenderer['Scatter Chart'],
        'Gráfico de Pizza Múltipla': PlotlyRenderer['Multiple Pie Chart']
      })
      )()
    },
    methods: {
      colorScaleGenerator(values) {
        const scale = scaleLinear()
          .domain([0, Math.max.apply(null, values)])
          .range(['#fff', '#77f'])
        return x => {
          return { 'background-color': scale(x) }
        }
      },
      noFilterbox() {
        alert('no data')
      },
      otherAction(pivotData) {
        alert(`All Total Count: ${pivotData.allTotal.count}`)
      }
    },
    watch: {
      config: {
        //        handler(value, oldValue) {
        handler(value) {
          delete value.data
        },
        deep: true,
        immediate: false
      }
    }
  },

};
</script>

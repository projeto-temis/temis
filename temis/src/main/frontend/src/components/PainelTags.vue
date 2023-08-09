<template>
  <div class="xrp-label-container">
    <!-- class="list-unstyled blog-tags" -->
    <span v-for="m in filteredTags" :title="m.titulo" :key="m.id"><button class="btn btn-default btn-sm xrp-label">
        <i :class="'fa fa-' + m.icone"></i> {{ m.nome
        }}<span v-if="m.pessoa &amp;&amp; !m.daPessoa">
          - {{ m.pessoa }}</span><span v-if="m.unidade &amp;&amp; (!m.daLotacao || (!m.daPessoa && !m.deOutraPessoa))">
          / {{ m.unidade }}</span>
      </button></span>
  </div>
</template>
<script>
export default {
  props: {
    list: { type: Array },
  },
  computed: {
    filteredTags: function () {
      if (!this.list) return [];
      return this.list.filter(e => true || e.ativoAgora && (e.visibilidade == 'GERAL' || (e.visibilidade == 'MARCADOS' && (e.daPessoa || e.daUnidade))));
    },
  }
};
</script>

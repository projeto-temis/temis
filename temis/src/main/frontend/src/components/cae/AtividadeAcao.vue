<template>
  <ul style="margin-bottom: 0px;" class="list-unstyled blog-tags">
    <li :class="'blog-tags-enabled-' + act.active" v-for="act in filtered" :key="act.slug"
      style="margin-bottom: 0px;padding-bottom:0px;padding-top:0px;"
      @click.prevent="act.active ? $parent.$parent.$eval(act.click) : null"><i :class="act.icon"
        :title="actMiniTitle(act.name, act.active, act.explanation)"></i></li>
  </ul>
</template>
<script>

export default {
  props: {
    action: { type: Array },
    slug: { type: String }
  },
  computed: {
    filtered: function () {
      return this.action.filter(i => i.slug === this.slug)
    },
  },
  methods: {
    actMiniTitle: function (name, active, explanation) {
      return name + ": " + explanation;
    },
  }
};
</script>

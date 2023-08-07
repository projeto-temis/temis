<template>
    <div>
        <fieldset class="juia juia-title">
            <div class="row align-items-center">
                <div class="col col-auto"><big class="juia">{{ model.singular }}</big></div>
            </div>
        </fieldset>
        <div class="row">
            <div class="col-6" ref="action"></div>
            <div class="col-6"></div>
        </div>
        <div ref="action" :is="comp" @keydown.enter.native.prevent="actEnter"></div>

        <div v-if="table" v-html="table" class="mt-3"></div>

        <pivot-table v-if="array" :pivotData="array" class="mt-3" />
    </div>
</template>
<script>
import Utils from '../juia/mixins/utils.js'
import PivotTable from '../components/PivotTable.vue'

export default {
    name: "Report",
    components: {
        PivotTable
    },
    mounted() {
        this.load();
        this.$on('validation-changed', (invalid) => {
            this.actInvalid = invalid
        });
    },
    data() {
        return {
            data: {
                change: []
            },
            tag: [],
            action: [],
            event: [],
            act: undefined,
            actTitle: undefined,
            actEventId: undefined,
            actTemplate: undefined,
            actInvalid: undefined,
            actShow: false,
            audit: false,
            table: undefined,
            array: undefined
        }
    },
    computed: {
        model() {
            return this.$store.state.model.find(i => i.locator === this.$route.params.locator)
        },
        comp() {
            return {
                template: this.actTemplate,
                name: 'ActionForm',
                data() {
                    return { act: this.$parent.act }
                },
                methods: {
                    proxify: function (v) {
                        if (v === undefined)
                            v = this.act;
                        for (var k in v) {
                            if (Object.prototype.hasOwnProperty.call(v, k)) {
                                var val = v[k]; delete v[k]; this.$set(v, k, val);
                            }
                        }
                    }
                }
            }
        }
    },
    methods: {
        load() {
            this.$http.get(`http://localhost:8080/temis/app/relatorio/${this.$route.params.locator}/form`).then(
                response => {
                    if (response.data.html) {
                        const html = response.data.html.replace('v-if="data"', '')
                        console.log(html)
                        this.$set(this, 'act', response.data.act);
                        this.actTemplate = '<validation-observer ref="observer" v-slot="obs">' + html + '<watcher ref="watcher" :value="obs.invalid"/>' +
                            '<div class="modal-footer pl-0 pr-0 pb-0">' +
                            '<b-button name="csv" :disabled="obs.invalid" variant="secondary" @click.prevent="$parent.actEnter(\'CSV\')">Baixar CSV</b-button>' +
                            '<b-button name="pdf" :disabled="obs.invalid" variant="secondary" @click.prevent="$parent.actEnter(\'PDF\')">Baixar PDF</b-button>' +
                            '<b-button name="pivot" :disabled="obs.invalid" variant="primary" @click.prevent="$parent.actEnter(\'JSON_ARRAY\')">Tabela Dinâmica</b-button>' +
                            '<b-button name="ok" :disabled="obs.invalid" variant="dark" @click.prevent="$parent.actEnter(\'HTML\')">Exibir</b-button>' +
                            '</div></validation-observer>';
                        this.actTitle = name;
                    } else {
                        this.actRun(name, undefined, {});
                    }
                },
                response => {
                    console.log(response)
                });
        },
        actOK: function (formato) {
            if (this.actInvalid)
                return;
            this.actRun(this.actTitle, this.actEventId, this.$refs.action.act, formato);
        },
        actEnter: function (formato) {
            console.log('enter')
            if (this.actInvalid)
                return;
            this.actOK(formato);
        },
        actRun: function (name, eventId, act, formato) {
            this.array = undefined;
            this.table = undefined;
            this.$set(this, 'act', act);
            const fd = Utils.formdata({ act: this.act })
            const url = `http://localhost:8080/temis/app/relatorio/${this.$route.params.locator}/exec?formato=${formato}&${fd}`
            if (formato === 'PDF' || formato === 'CSV') {
                var win = window.open(url, '_blank');
                win.focus();
                return
            } else if (formato === 'HTML') {
                this.$http.get(url).then(
                    response => {
                        this.table = response.data;
                    },
                    response => {
                        this.$bvModal.msgBoxOk(response.data.errormsg)
                    });
            } else if (formato === 'JSON_ARRAY') {
                this.$http.get(url).then(
                    response => {
                        this.array = response.data;
                    },
                    response => {
                        this.$bvModal.msgBoxOk(response.data.errormsg)
                    });
            }
        },

    }
}
</script>
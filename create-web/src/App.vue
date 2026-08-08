<script setup lang="ts">
import PolicyIssuanceReversalView from './views/PolicyIssuanceReversalView.vue'
import UnderwritingBatchView from './views/UnderwritingBatchView.vue'
import InitialPremiumMatchView from './views/InitialPremiumMatchView.vue'
import ApplicationEntryView from './views/ApplicationEntryView.vue'
import CustomerCreateView from './views/CustomerCreateView.vue'
import { ref } from 'vue'
const page = ref<'customer' | 'entry' | 'payment' | 'batch' | 'reversal'>('customer')
</script>

<template>
  <main class="app-shell"><section class="workspace"><header class="topbar"><p class="eyebrow">新契約作業系統</p><h1>保單新契約作業</h1></header><div class="work-layout"><nav class="side-menu" aria-label="新契約功能">
    <button :class="{active:page==='customer'}" @click="page='customer'">客戶資料建立</button><button :class="{active:page==='entry'}" @click="page='entry'">保單登打</button><button :class="{active:page==='payment'}" @click="page='payment'">繳費配對</button><button :class="{active:page==='batch'}" @click="page='batch'">批次核保</button><button :class="{active:page==='reversal'}" @click="page='reversal'">承保撤回</button>
  </nav><section class="work-content"><CustomerCreateView v-if="page==='customer'"/><ApplicationEntryView v-else-if="page==='entry'"/><InitialPremiumMatchView v-else-if="page === 'payment'" /><UnderwritingBatchView v-else-if="page === 'batch'" /><PolicyIssuanceReversalView v-else /></section></div></section></main>
</template>

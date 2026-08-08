<script setup lang="ts">
import PolicyIssuanceReversalView from './views/PolicyIssuanceReversalView.vue'
import UnderwritingBatchView from './views/UnderwritingBatchView.vue'
import InitialPremiumMatchView from './views/InitialPremiumMatchView.vue'
import { ref } from 'vue'
const page = ref<'payment' | 'batch' | 'reversal'>('payment')
</script>

<template>
  <nav><button @click="page='payment'">繳費配對</button><button @click="page='batch'">批次核保</button><button @click="page='reversal'">承保撤回</button></nav>
  <InitialPremiumMatchView v-if="page === 'payment'" />
  <UnderwritingBatchView v-else-if="page === 'batch'" />
  <PolicyIssuanceReversalView v-else />
</template>

<style scoped>
nav{max-width:64rem;margin:1rem auto 0;display:flex;gap:.5rem}button{padding:.65rem 1rem;border:0;border-radius:.4rem;background:#263b63;color:white}
</style>

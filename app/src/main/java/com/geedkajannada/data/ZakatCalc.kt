package com.geedkajannada.data

data class ZakatResult(val net: Double, val due: Double, val eligible: Boolean)

/** Xisaabiye Zakat: tilmaame kaliya, ma aha fatwa. Nisaabka iyo heerka isticmaaluhu ayaa geliya. */
object ZakatCalc {
    fun compute(cash: Double, gold: Double, debts: Double, nisab: Double, ratePercent: Double): ZakatResult {
        val net = (cash + gold - debts).coerceAtLeast(0.0)
        val eligible = net > 0 && net >= nisab
        val due = if (eligible) net * ratePercent / 100.0 else 0.0
        return ZakatResult(net, due, eligible)
    }

    /** Akhri tiro aan taban ahayn; madhan = 0; khalad = null. */
    fun parseAmount(text: String): Double? {
        val t = text.trim().replace(',', '.')
        if (t.isEmpty()) return 0.0
        val v = t.toDoubleOrNull() ?: return null
        return if (v.isFinite() && v >= 0) v else null
    }
}

package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.TrackerRepository
import com.example.data.TrackerSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TrackerViewModel(private val repository: TrackerRepository) : ViewModel() {
    // Input states
    private val _lydInBank = MutableStateFlow("")
    val lydInBank = _lydInBank.asStateFlow()

    private val _lydInCash = MutableStateFlow("")
    val lydInCash = _lydInCash.asStateFlow()

    private val _usdInCash = MutableStateFlow("")
    val usdInCash = _usdInCash.asStateFlow()

    private val _cashRate = MutableStateFlow("")
    val cashRate = _cashRate.asStateFlow()

    private val _bankBalanceRate = MutableStateFlow("")
    val bankBalanceRate = _bankBalanceRate.asStateFlow()

    // Error states
    private val _lydInBankError = MutableStateFlow<String?>(null)
    val lydInBankError = _lydInBankError.asStateFlow()

    private val _lydInCashError = MutableStateFlow<String?>(null)
    val lydInCashError = _lydInCashError.asStateFlow()

    private val _usdInCashError = MutableStateFlow<String?>(null)
    val usdInCashError = _usdInCashError.asStateFlow()

    private val _cashRateError = MutableStateFlow<String?>(null)
    val cashRateError = _cashRateError.asStateFlow()

    private val _bankBalanceRateError = MutableStateFlow<String?>(null)
    val bankBalanceRateError = _bankBalanceRateError.asStateFlow()

    // Warning state
    private val _rateWarning = MutableStateFlow<String?>(null)
    val rateWarning = _rateWarning.asStateFlow()

    // Computed values (Double)
    private val _bankLydRealValue = MutableStateFlow(0.0)
    val bankLydRealValue = _bankLydRealValue.asStateFlow()

    private val _hiddenLoss = MutableStateFlow(0.0)
    val hiddenLoss = _hiddenLoss.asStateFlow()

    private val _totalLyd = MutableStateFlow(0.0)
    val totalLyd = _totalLyd.asStateFlow()

    private val _totalUsd = MutableStateFlow(0.0)
    val totalUsd = _totalUsd.asStateFlow()

    init {
        viewModelScope.launch {
            val settings = repository.getSettingsDirect()
            if (settings != null) {
                _lydInBank.value = if (settings.lydInBank == 0.0) "" else formatInputVal(settings.lydInBank)
                _lydInCash.value = if (settings.lydInCash == 0.0) "" else formatInputVal(settings.lydInCash)
                _usdInCash.value = if (settings.usdInCash == 0.0) "" else formatInputVal(settings.usdInCash)
                _cashRate.value = if (settings.cashRate == 0.0) "" else formatInputVal(settings.cashRate)
                _bankBalanceRate.value = if (settings.bankBalanceRate == 0.0) "" else formatInputVal(settings.bankBalanceRate)
            } else {
                // Sane default rates for Libya (e.g. cash rate 6.5, bank rate 8.0)
                _lydInBank.value = "10000"
                _lydInCash.value = "5000"
                _usdInCash.value = "500"
                _cashRate.value = "6.5"
                _bankBalanceRate.value = "8.0"
            }
            calculate()
        }
    }

    private fun formatInputVal(d: Double): String {
        return if (d % 1.0 == 0.0) d.toInt().toString() else d.toString()
    }

    private fun parseDouble(text: String, onError: (String?) -> Unit): Double? {
        if (text.isBlank()) {
            onError(null)
            return 0.0
        }
        val cleanText = text.replace(",", "").trim()
        val parsed = cleanText.toDoubleOrNull()
        if (parsed == null) {
            onError("Invalid number format")
            return null
        }
        if (parsed < 0) {
            onError("Value must be non-negative")
            return null
        }
        onError(null)
        return parsed
    }

    fun calculate() {
        val lydBankVal = parseDouble(_lydInBank.value) { _lydInBankError.value = it }
        val lydCashVal = parseDouble(_lydInCash.value) { _lydInCashError.value = it }
        val usdCashVal = parseDouble(_usdInCash.value) { _usdInCashError.value = it }
        val cashRateVal = parseDouble(_cashRate.value) { _cashRateError.value = it }
        val bankBalanceRateVal = parseDouble(_bankBalanceRate.value) { _bankBalanceRateError.value = it }

        val b = lydBankVal ?: 0.0
        val c = lydCashVal ?: 0.0
        val u = usdCashVal ?: 0.0
        val rCash = cashRateVal ?: 0.0
        val rBank = bankBalanceRateVal ?: 0.0

        // Warning: atypical if bank-balance rate is less than cash rate (meaning bank money is valued more than cash)
        if (rBank > 0 && rCash > 0 && rBank < rCash) {
            _rateWarning.value = "Bank rate looks better than cash rate — double check your numbers"
        } else {
            _rateWarning.value = null
        }

        // Calculations:
        // Bank LYD Real Value = (LYD in Bank ÷ Bank-Balance Rate) × Cash Rate
        val bankReal = if (rBank == 0.0) 0.0 else (b / rBank) * rCash
        _bankLydRealValue.value = bankReal

        // Hidden Loss = LYD in Bank − Bank LYD Real Value
        _hiddenLoss.value = b - bankReal

        // Total (LYD) = LYD in Cash + Bank LYD Real Value + (USD in Cash × Cash Rate)
        _totalLyd.value = c + bankReal + (u * rCash)

        // Total (USD) = USD in Cash + (LYD in Cash ÷ Cash Rate) + (LYD in Bank ÷ Bank-Balance Rate)
        val usdTotal = u + (if (rCash == 0.0) 0.0 else c / rCash) + (if (rBank == 0.0) 0.0 else b / rBank)
        _totalUsd.value = usdTotal
    }

    private fun saveToDb() {
        viewModelScope.launch {
            val lydBankVal = _lydInBank.value.replace(",", "").toDoubleOrNull() ?: 0.0
            val lydCashVal = _lydInCash.value.replace(",", "").toDoubleOrNull() ?: 0.0
            val usdCashVal = _usdInCash.value.replace(",", "").toDoubleOrNull() ?: 0.0
            val cashRateVal = _cashRate.value.replace(",", "").toDoubleOrNull() ?: 0.0
            val bankBalanceRateVal = _bankBalanceRate.value.replace(",", "").toDoubleOrNull() ?: 0.0

            repository.saveSettings(
                TrackerSettings(
                    lydInBank = lydBankVal,
                    lydInCash = lydCashVal,
                    usdInCash = usdCashVal,
                    cashRate = cashRateVal,
                    bankBalanceRate = bankBalanceRateVal
                )
            )
        }
    }

    fun updateLydInBank(value: String) {
        _lydInBank.value = value
        calculate()
        saveToDb()
    }

    fun updateLydInCash(value: String) {
        _lydInCash.value = value
        calculate()
        saveToDb()
    }

    fun updateUsdInCash(value: String) {
        _usdInCash.value = value
        calculate()
        saveToDb()
    }

    fun updateCashRate(value: String) {
        _cashRate.value = value
        calculate()
        saveToDb()
    }

    fun updateBankBalanceRate(value: String) {
        _bankBalanceRate.value = value
        calculate()
        saveToDb()
    }
}

class TrackerViewModelFactory(private val repository: TrackerRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TrackerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TrackerViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

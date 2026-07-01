package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import com.example.data.AppDatabase
import com.example.data.TrackerRepository
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.TrackerViewModel
import com.example.ui.TrackerViewModelFactory
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(applicationContext)
        val repository = TrackerRepository(database.trackerDao())
        val viewModel = ViewModelProvider(
            this,
            TrackerViewModelFactory(repository)
        )[TrackerViewModel::class.java]

        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = MaterialTheme.colorScheme.background,
                    topBar = {
                        // Custom polished header following the design HTML
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.background)
                                .statusBarsPadding()
                                .padding(top = 16.dp, start = 24.dp, end = 24.dp, bottom = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Real Value Tracker",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 22.sp,
                                        letterSpacing = (-0.5).sp,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "ASSET LIQUIDITY INDEX • LYD",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            shape = RoundedCornerShape(20.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccountBalanceWallet,
                                        contentDescription = "Wallet Icon",
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    RealValueTrackerScreen(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun RealValueTrackerScreen(
    viewModel: TrackerViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    // Observe text fields from ViewModel
    val lydInBank by viewModel.lydInBank.collectAsState()
    val lydInCash by viewModel.lydInCash.collectAsState()
    val usdInCash by viewModel.usdInCash.collectAsState()
    val cashRate by viewModel.cashRate.collectAsState()
    val bankBalanceRate by viewModel.bankBalanceRate.collectAsState()

    // Observe error states
    val lydInBankError by viewModel.lydInBankError.collectAsState()
    val lydInCashError by viewModel.lydInCashError.collectAsState()
    val usdInCashError by viewModel.usdInCashError.collectAsState()
    val cashRateError by viewModel.cashRateError.collectAsState()
    val bankBalanceRateError by viewModel.bankBalanceRateError.collectAsState()

    // Observe warnings and results
    val rateWarning by viewModel.rateWarning.collectAsState()
    val bankRealVal by viewModel.bankLydRealValue.collectAsState()
    val hiddenLossVal by viewModel.hiddenLoss.collectAsState()
    val totalLydVal by viewModel.totalLyd.collectAsState()
    val totalUsdVal by viewModel.totalUsd.collectAsState()

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // --- Side-by-Side Dynamic Total Cards with Highlight Scaling & Bold Typography ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // LYD Total Card (Primary Blue Card)
            Card(
                modifier = Modifier
                    .weight(1f)
                    .testTag("lyd_total_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    // Decorative payments background symbol
                    Icon(
                        imageVector = Icons.Default.Payments,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.12f),
                        modifier = Modifier
                            .size(64.dp)
                            .align(Alignment.TopEnd)
                            .offset(x = 10.dp, y = (-10).dp)
                    )

                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "TOTAL (LYD)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = formatAmount(totalLydVal),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Real spendable value",
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // USD Total Card (Light Blue Card)
            Card(
                modifier = Modifier
                    .weight(1f)
                    .testTag("usd_total_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "TOTAL (USD)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$" + formatAmount(totalUsdVal),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Market parity value",
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // --- Hidden Loss Banner ---
        AnimatedVisibility(
            visible = true,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.TrendingDown,
                        contentDescription = "Hidden Loss",
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(20.dp).padding(top = 2.dp)
                    )
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "HIDDEN LIQUIDITY LOSS",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp,
                                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.9f)
                            )
                            Text(
                                text = "- ${formatAmount(hiddenLossVal)} LYD",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "The gap between your bank paper value and real market purchasing power.",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f),
                            lineHeight = 13.sp
                        )
                    }
                }
            }
        }

        // --- Rate Warning Message ---
        rateWarning?.let { warningText ->
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = Color(0xFFFFF9C4), // Light soft yellow warning
                            shape = RoundedCornerShape(12.dp)
                        )
                        .border(1.dp, Color(0xFFFBC02D), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Warning",
                        tint = Color(0xFFE65100),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = warningText,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF5D4037),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // --- Grid Inputs Area ---
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Row 1: LYD in Bank & LYD in Cash
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    PolishedInputField(
                        value = lydInBank,
                        onValueChange = { viewModel.updateLydInBank(it) },
                        label = "LYD in Bank",
                        explanation = "Money sitting in bank, subject to withdrawal quotas.",
                        errorText = lydInBankError,
                        tag = "lyd_in_bank_input"
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    PolishedInputField(
                        value = lydInCash,
                        onValueChange = { viewModel.updateLydInCash(it) },
                        label = "LYD in Cash",
                        explanation = "Physical cash on hand — fully liquid.",
                        errorText = lydInCashError,
                        tag = "lyd_in_cash_input"
                    )
                }
            }

            // Row 2: USD in Cash (Full Width with Badges)
            PolishedInputField(
                value = usdInCash,
                onValueChange = { viewModel.updateUsdInCash(it) },
                label = "USD in Cash",
                explanation = "Physical dollars on hand.",
                errorText = usdInCashError,
                isStableBadge = true,
                tag = "usd_in_cash_input"
            )

            // Row 3: Cash Rate & Bank Rate (Dashed Borders style helper)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    PolishedInputField(
                        value = cashRate,
                        onValueChange = { viewModel.updateCashRate(it) },
                        label = "Cash Rate",
                        explanation = "Black market: LYD per 1 USD.",
                        errorText = cashRateError,
                        isDashedBorder = true,
                        tag = "cash_rate_input"
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    PolishedInputField(
                        value = bankBalanceRate,
                        onValueChange = { viewModel.updateBankBalanceRate(it) },
                        label = "Bank Rate",
                        explanation = "Real bank-to-USD conversion.",
                        errorText = bankBalanceRateError,
                        isDashedBorder = true,
                        tag = "bank_balance_rate_input"
                    )
                }
            }
        }

        // --- True Value Outputs Breakdown ---
        Text(
            text = "LIQUIDITY ANALYSIS",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
            modifier = Modifier.padding(top = 8.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Cash LYD Output
                PolishedOutputRow(
                    label = "Cash LYD",
                    valueText = formatAmount(parseDoubleSafe(lydInCash)) + " LYD",
                    explanation = "Counted at full face value — already liquid.",
                    tag = "output_cash_lyd"
                )

                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                // Bank LYD Real Value Output
                PolishedOutputRow(
                    label = "Bank LYD (real value)",
                    valueText = formatAmount(bankRealVal) + " LYD",
                    explanation = "Adjusted using the bank-balance rate vs cash rate spread.",
                    valueColor = MaterialTheme.colorScheme.primary,
                    tag = "output_bank_lyd_real"
                )

                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                // USD Cash Output
                PolishedOutputRow(
                    label = "USD Cash",
                    valueText = "$" + formatAmount(parseDoubleSafe(usdInCash)),
                    explanation = "Counted at full face value — already liquid.",
                    tag = "output_usd_cash"
                )

                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                // Hidden Loss on Bank LYD Output
                PolishedOutputRow(
                    label = "Hidden Loss on Bank LYD",
                    valueText = formatAmount(hiddenLossVal) + " LYD",
                    explanation = "The gap between what bank LYD looks like vs what it's really worth today.",
                    valueColor = if (hiddenLossVal > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                    tag = "output_hidden_loss"
                )

                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                // Total (LYD)
                PolishedOutputRow(
                    label = "Total (LYD)",
                    valueText = formatAmount(totalLydVal) + " LYD",
                    explanation = "All assets valued in real, spendable LYD.",
                    valueColor = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.ExtraBold,
                    tag = "output_total_lyd"
                )

                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                // Total (USD)
                PolishedOutputRow(
                    label = "Total (USD)",
                    valueText = "$" + formatAmount(totalUsdVal),
                    explanation = "Same total, expressed in dollars.",
                    valueColor = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.ExtraBold,
                    tag = "output_total_usd"
                )
            }
        }

        // --- Actions Section ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Fully rounded high-polish Primary Copy Results Button
            Button(
                onClick = {
                    val reportText = """
                        --- REAL VALUE TRACKER REPORT ---
                        Date/Time: 2026-07-01
                        
                        [Assets Breakdown]
                        - Cash LYD: ${formatAmount(parseDoubleSafe(lydInCash))} LYD
                        - Bank LYD (Real Value): ${formatAmount(bankRealVal)} LYD
                        - USD Cash: $${formatAmount(parseDoubleSafe(usdInCash))}
                        - Hidden Bank Loss: ${formatAmount(hiddenLossVal)} LYD
                        
                        [Market Rates applied]
                        - Cash USD/LYD: ${formatAmount(parseDoubleSafe(cashRate))}
                        - Bank-Balance USD/LYD: ${formatAmount(parseDoubleSafe(bankBalanceRate))}
                        
                        [Calculated Totals]
                        - TOTAL ASSETS IN REAL LYD: ${formatAmount(totalLydVal)} LYD
                        - TOTAL ASSETS IN USD: $${formatAmount(totalUsdVal)}
                        ---------------------------------
                        Accounting for withdrawal limits and bank balance discount spreads.
                    """.trimIndent()

                    clipboardManager.setText(AnnotatedString(reportText))
                    Toast.makeText(context, "Results copied to clipboard!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .testTag("copy_report_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(28.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy Icon",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Copy Results",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            // Recalculate/Sync status button
            Button(
                onClick = {
                    viewModel.calculate()
                    Toast.makeText(context, "Calculations recalculated!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .size(56.dp)
                    .testTag("recalculate_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                shape = RoundedCornerShape(28.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = "Recalculate Icon",
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // --- Muted Footer Tagline ---
        Text(
            text = "Values persist locally. Last updated: Real-time",
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
fun PolishedInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    explanation: String,
    errorText: String?,
    isDashedBorder: Boolean = false,
    isStableBadge: Boolean = false,
    tag: String
) {
    // Elegant container card following Design HTML
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(tag),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (errorText != null) {
                MaterialTheme.colorScheme.error
            } else if (isDashedBorder) {
                MaterialTheme.colorScheme.outline.copy(alpha = 0.6f) // Represent dashed as lighter/secondary border
            } else {
                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Label & Badge Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label.uppercase(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )

                if (isStableBadge) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "STABLE",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Text Input Row (Borderless slick transparent layout)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("${tag}_text_field"),
                    textStyle = TextStyle(
                        color = if (errorText != null) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
                )

                if (value.isNotEmpty()) {
                    IconButton(
                        onClick = { onValueChange("") },
                        modifier = Modifier
                            .size(24.dp)
                            .testTag("${tag}_clear")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear Input",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Explanation / Error beneath
            if (errorText != null) {
                Text(
                    text = errorText,
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Text(
                    text = explanation,
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    lineHeight = 11.sp
                )
            }
        }
    }
}

@Composable
fun PolishedOutputRow(
    label: String,
    valueText: String,
    explanation: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    fontWeight: FontWeight = FontWeight.Normal,
    tag: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(tag)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = valueText,
                fontSize = 15.sp,
                fontWeight = if (fontWeight == FontWeight.ExtraBold) FontWeight.Black else FontWeight.Bold,
                color = valueColor
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = explanation,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            lineHeight = 12.sp
        )
    }
}

// Formatting Helper Functions
fun formatAmount(value: Double): String {
    return String.format(Locale.US, "%,.2f", value)
}

fun parseDoubleSafe(text: String): Double {
    return text.replace(",", "").toDoubleOrNull() ?: 0.0
}

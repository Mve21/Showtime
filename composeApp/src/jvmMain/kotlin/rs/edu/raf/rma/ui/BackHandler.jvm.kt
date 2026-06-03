package rs.edu.raf.rma.ui

import androidx.compose.runtime.Composable

@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) {
    // Desktop nema sistem back dugme — no-op
}

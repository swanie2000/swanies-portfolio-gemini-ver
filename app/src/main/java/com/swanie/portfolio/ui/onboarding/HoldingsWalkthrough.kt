package com.swanie.portfolio.ui.onboarding

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.swanie.portfolio.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.max
import kotlin.math.roundToInt

private fun coerceInRange(value: Float, min: Float, max: Float): Float =
    if (max < min) value else value.coerceIn(min, max)

enum class WalkthroughAnchor {
    ADD_BUTTON,
    ASSET_CARD,
    AMOUNT_INPUT,
    PICKER_PROVIDER_BUTTON,
    PICKER_DROPDOWN,
    PICKER_SEARCH_BOX,
    METAL_ARCHITECT_METAL_HEADER,
    METAL_ARCHITECT_METAL_GRID,
    METAL_ARCHITECT_SHAPE_HEADER,
    METAL_ARCHITECT_SHAPE_GRID,
    METAL_ARCHITECT_UNIT_HEADER,
    METAL_ARCHITECT_UNIT_GRID,
    METAL_ARCHITECT_CONTINUE,
    METAL_ARCHITECT_LIVE_WEIGHT,
    METAL_ARCHITECT_LIVE_QUANTITY_NAME,
    METAL_ARCHITECT_LIVE_PREMIUM_MODE,
    METAL_ARCHITECT_LIVE_ICON_NEXT,
    METAL_ARCHITECT_ICON_ADD,
    SETTINGS_TAB,
    FEEDBACK_ROW,
}

private enum class WalkthroughPath {
    CRYPTO,
    METAL,
}

enum class HoldingsWalkthroughStep {
    INACTIVE,
    HOLDINGS_TAP_ADD,
    PICKER_SELECT_PROVIDER,
    PICKER_CHOOSE_PROVIDER,
    PICKER_TYPE_SEARCH,
    PICKER_SCROLL_RESULTS,
    METAL_ARCHITECT_SELECT_METAL,
    METAL_ARCHITECT_SELECT_SHAPE,
    METAL_ARCHITECT_SELECT_UNIT,
    METAL_ARCHITECT_TAP_NEXT,
    METAL_ARCHITECT_LIVE_WEIGHT,
    METAL_ARCHITECT_LIVE_QUANTITY_NAME,
    METAL_ARCHITECT_LIVE_PREMIUM_MODE,
    METAL_ARCHITECT_LIVE_TAP_NEXT,
    METAL_ARCHITECT_ICON_PICK,
    AMOUNT_ENTER_SAVE,
    HOLDINGS_TAP_CARD,
    HOLDINGS_EDIT,
    HOLDINGS_DELETE,
    OPEN_SETTINGS,
    SETTINGS_FEEDBACK,
    COMPLETE,
}

private val TourYellow = Color(0xFFFFD700)
private val TourYellowShadow = Color(0x66000000)

private enum class CalloutPlacement {
    SCREEN_BOTTOM,
    SCREEN_TOP,
    ANCHOR_BELOW,
    ANCHOR_ABOVE,
    /** Pill + down-arrow in one row, positioned just above the anchor (no arrow stacked under pill). */
    ANCHOR_ABOVE_INLINE,
    /** Pill + up-arrow in one row, positioned just below the anchor. */
    ANCHOR_BELOW_INLINE,
    ANCHOR_LEFT,
}

private enum class WalkthroughArrowDirection {
    Up,
    Down,
    Left,
    Right,
}

private data class WalkthroughPulse(
    val scale: Float,
    val glowAlpha: Float,
)

@Composable
private fun rememberWalkthroughPulse(): WalkthroughPulse {
    val transition = rememberInfiniteTransition(label = "walkthroughPulse")
    val scale by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.07f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "walkthroughPulseScale",
    )
    val glowAlpha by transition.animateFloat(
        initialValue = 0.75f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "walkthroughPulseGlow",
    )
    return WalkthroughPulse(scale = scale, glowAlpha = glowAlpha)
}

private enum class HintHorizontalAlign {
    Start,
    Center,
    End,
}

private data class HintStep(
    val label: String,
    val placement: CalloutPlacement,
    val horizontalAlign: HintHorizontalAlign = HintHorizontalAlign.Center,
    val arrowAlign: HintHorizontalAlign = HintHorizontalAlign.Center,
    val arrowHorizontalFraction: Float? = null,
    val anchor: WalkthroughAnchor? = null,
    val highlightAnchor: WalkthroughAnchor? = null,
    val showNext: Boolean = false,
    val nextLabelRes: Int = R.string.walkthrough_next,
    val styledFinishButton: Boolean = false,
    val maxLabelLines: Int = 1,
    val maxLabelWidth: Dp = 240.dp,
    val maxCalloutWidth: Dp = 340.dp,
    /** Vertical stack: arrow on top, pill below (used on metal live-card page). */
    val arrowAbovePill: Boolean = false,
)

private fun arrowDirectionFor(placement: CalloutPlacement): WalkthroughArrowDirection =
    when (placement) {
        CalloutPlacement.ANCHOR_LEFT -> WalkthroughArrowDirection.Right
        CalloutPlacement.ANCHOR_BELOW,
        CalloutPlacement.ANCHOR_BELOW_INLINE,
        CalloutPlacement.SCREEN_BOTTOM -> WalkthroughArrowDirection.Up
        CalloutPlacement.ANCHOR_ABOVE,
        CalloutPlacement.ANCHOR_ABOVE_INLINE,
        CalloutPlacement.SCREEN_TOP -> WalkthroughArrowDirection.Down
    }

private fun horizontalOffsetX(
    align: HintHorizontalAlign,
    screenW: Float,
    width: Int,
    pad: Float,
): Int = when (align) {
    HintHorizontalAlign.Start -> pad
    HintHorizontalAlign.Center -> (screenW - width) / 2f
    HintHorizontalAlign.End -> screenW - width - pad
}.roundToInt().coerceAtLeast(0)

private fun hintHorizontalAlignment(align: HintHorizontalAlign): Alignment.Horizontal =
    when (align) {
        HintHorizontalAlign.Start -> Alignment.Start
        HintHorizontalAlign.Center -> Alignment.CenterHorizontally
        HintHorizontalAlign.End -> Alignment.End
    }

class HoldingsWalkthroughController {
    private val _step = MutableStateFlow(HoldingsWalkthroughStep.INACTIVE)
    val step: StateFlow<HoldingsWalkthroughStep> = _step.asStateFlow()

    private val _anchors = MutableStateFlow<Map<WalkthroughAnchor, Rect>>(emptyMap())
    val anchors: StateFlow<Map<WalkthroughAnchor, Rect>> = _anchors.asStateFlow()

    private val _selectedAssetSymbol = MutableStateFlow<String?>(null)
    val selectedAssetSymbol: StateFlow<String?> = _selectedAssetSymbol.asStateFlow()

    private val _highlightCoinId = MutableStateFlow<String?>(null)
    val highlightCoinId: StateFlow<String?> = _highlightCoinId.asStateFlow()

    private val _chromeHidden = MutableStateFlow(false)
    val chromeHidden: StateFlow<Boolean> = _chromeHidden.asStateFlow()

    private val _overlaySuppressed = MutableStateFlow(false)
    val overlaySuppressed: StateFlow<Boolean> = _overlaySuppressed.asStateFlow()

    private val _path = MutableStateFlow(WalkthroughPath.CRYPTO)

    fun setOverlaySuppressed(suppressed: Boolean) {
        _overlaySuppressed.value = suppressed
    }

    fun updateAnchor(anchor: WalkthroughAnchor, bounds: Rect?) {
        val next = _anchors.value.toMutableMap()
        if (bounds == null) {
            next.remove(anchor)
        } else {
            next[anchor] = bounds
        }
        _anchors.value = next
    }

    fun startTour() {
        if (_step.value == HoldingsWalkthroughStep.INACTIVE ||
            _step.value == HoldingsWalkthroughStep.COMPLETE
        ) {
            _chromeHidden.value = false
            _path.value = WalkthroughPath.CRYPTO
            _step.value = HoldingsWalkthroughStep.HOLDINGS_TAP_ADD
        }
    }

    fun skip() {
        _step.value = HoldingsWalkthroughStep.COMPLETE
    }

    fun onNextFromMessageOnly() {
        when (_step.value) {
            HoldingsWalkthroughStep.HOLDINGS_TAP_CARD ->
                _step.value = HoldingsWalkthroughStep.COMPLETE
            else -> Unit
        }
    }

    fun onAddButtonClicked() {
        if (_step.value == HoldingsWalkthroughStep.HOLDINGS_TAP_ADD) {
            _step.value = HoldingsWalkthroughStep.PICKER_SELECT_PROVIDER
        }
    }

    fun onPickerDropdownOpened() {
        if (_step.value == HoldingsWalkthroughStep.PICKER_SELECT_PROVIDER) {
            _step.value = HoldingsWalkthroughStep.PICKER_CHOOSE_PROVIDER
        }
    }

    fun onPickerProviderSelected() {
        if (_step.value == HoldingsWalkthroughStep.PICKER_CHOOSE_PROVIDER) {
            _path.value = WalkthroughPath.CRYPTO
            _step.value = HoldingsWalkthroughStep.PICKER_TYPE_SEARCH
        }
    }

    fun onMetalProviderSelected() {
        when (_step.value) {
            HoldingsWalkthroughStep.PICKER_SELECT_PROVIDER,
            HoldingsWalkthroughStep.PICKER_CHOOSE_PROVIDER,
            HoldingsWalkthroughStep.PICKER_TYPE_SEARCH,
            HoldingsWalkthroughStep.PICKER_SCROLL_RESULTS -> {
                _path.value = WalkthroughPath.METAL
                _selectedAssetSymbol.value = "METAL"
                _step.value = HoldingsWalkthroughStep.METAL_ARCHITECT_SELECT_METAL
            }
            else -> Unit
        }
    }

    fun onMetalArchitectMetalSelected() {
        if (_step.value == HoldingsWalkthroughStep.METAL_ARCHITECT_SELECT_METAL) {
            _step.value = HoldingsWalkthroughStep.METAL_ARCHITECT_SELECT_SHAPE
        }
    }

    fun onMetalArchitectShapeSelected() {
        if (_step.value == HoldingsWalkthroughStep.METAL_ARCHITECT_SELECT_SHAPE) {
            _step.value = HoldingsWalkthroughStep.METAL_ARCHITECT_SELECT_UNIT
        }
    }

    fun onMetalArchitectUnitSelected() {
        if (_step.value == HoldingsWalkthroughStep.METAL_ARCHITECT_SELECT_UNIT) {
            _step.value = HoldingsWalkthroughStep.METAL_ARCHITECT_TAP_NEXT
        }
    }

    fun onMetalArchitectBlueprintContinued() {
        if (_path.value == WalkthroughPath.METAL &&
            _step.value in metalArchitectBlueprintSteps
        ) {
            _chromeHidden.value = false
            _step.value = HoldingsWalkthroughStep.METAL_ARCHITECT_LIVE_WEIGHT
        }
    }

    fun onMetalArchitectLiveEditorSaved(field: String?) {
        when (_step.value) {
            HoldingsWalkthroughStep.METAL_ARCHITECT_LIVE_WEIGHT ->
                if (field == "WEIGHT") {
                    _step.value = HoldingsWalkthroughStep.METAL_ARCHITECT_LIVE_QUANTITY_NAME
                }
            HoldingsWalkthroughStep.METAL_ARCHITECT_LIVE_QUANTITY_NAME ->
                if (field == "QUANTITY" || field == "NAME") {
                    _step.value = HoldingsWalkthroughStep.METAL_ARCHITECT_LIVE_PREMIUM_MODE
                }
            HoldingsWalkthroughStep.METAL_ARCHITECT_LIVE_PREMIUM_MODE ->
                if (field == "PREMIUM") {
                    _step.value = HoldingsWalkthroughStep.METAL_ARCHITECT_LIVE_TAP_NEXT
                }
            else -> Unit
        }
    }

    fun onMetalArchitectLiveCardContinued() {
        if (_step.value == HoldingsWalkthroughStep.METAL_ARCHITECT_LIVE_TAP_NEXT) {
            _chromeHidden.value = false
            _step.value = HoldingsWalkthroughStep.METAL_ARCHITECT_ICON_PICK
        }
    }

    fun onMetalArchitectIconPickBack() {
        if (_step.value == HoldingsWalkthroughStep.METAL_ARCHITECT_ICON_PICK) {
            _chromeHidden.value = false
            _step.value = HoldingsWalkthroughStep.METAL_ARCHITECT_LIVE_TAP_NEXT
        }
    }

    fun onMetalArchitectIconSaving() {
        if (_step.value == HoldingsWalkthroughStep.METAL_ARCHITECT_ICON_PICK) {
            _chromeHidden.value = true
        }
    }

    fun onMetalArchitectCancelled() {
        when (_step.value) {
            in metalArchitectBlueprintSteps,
            in metalArchitectLiveCardSteps,
            HoldingsWalkthroughStep.METAL_ARCHITECT_ICON_PICK -> {
                _chromeHidden.value = false
                _overlaySuppressed.value = false
                _path.value = WalkthroughPath.CRYPTO
                _step.value = HoldingsWalkthroughStep.PICKER_CHOOSE_PROVIDER
            }
            else -> Unit
        }
    }

    fun onPickerSearchResultsReady() {
        if (_step.value == HoldingsWalkthroughStep.PICKER_TYPE_SEARCH) {
            _step.value = HoldingsWalkthroughStep.PICKER_SCROLL_RESULTS
        }
    }

    fun onAssetSelected(coinId: String, symbol: String) {
        _highlightCoinId.value = coinId
        _selectedAssetSymbol.value = symbol
        when (_step.value) {
            HoldingsWalkthroughStep.PICKER_SELECT_PROVIDER,
            HoldingsWalkthroughStep.PICKER_CHOOSE_PROVIDER,
            HoldingsWalkthroughStep.PICKER_TYPE_SEARCH,
            HoldingsWalkthroughStep.PICKER_SCROLL_RESULTS -> {
                _step.value = HoldingsWalkthroughStep.AMOUNT_ENTER_SAVE
            }
            else -> Unit
        }
    }

    fun onAmountEntrySubmitted() {
        if (_step.value == HoldingsWalkthroughStep.AMOUNT_ENTER_SAVE) {
            _chromeHidden.value = true
        }
    }

    fun onMetalArchitectSaving() {
        if (_step.value in metalArchitectLiveCardSteps ||
            _step.value == HoldingsWalkthroughStep.METAL_ARCHITECT_ICON_PICK
        ) {
            _chromeHidden.value = true
        }
    }

    fun onAmountSaved(savedCoinId: String? = null) {
        savedCoinId?.let { _highlightCoinId.value = it }
        when (_step.value) {
            HoldingsWalkthroughStep.AMOUNT_ENTER_SAVE,
            in metalArchitectBlueprintSteps,
            in metalArchitectLiveCardSteps,
            HoldingsWalkthroughStep.METAL_ARCHITECT_ICON_PICK -> {
                _chromeHidden.value = false
                _overlaySuppressed.value = false
                _path.value = WalkthroughPath.CRYPTO
                _step.value = HoldingsWalkthroughStep.HOLDINGS_TAP_CARD
            }
            else -> Unit
        }
    }

    fun onHoldingsCardInteraction(expandedAssetId: String?, editingAssetId: String?) {
        // Card gestures are explained in one summary step; user taps End when ready.
    }

    fun onSettingsOpened() {
        if (_step.value == HoldingsWalkthroughStep.OPEN_SETTINGS) {
            _step.value = HoldingsWalkthroughStep.SETTINGS_FEEDBACK
        }
    }

    fun onFeedbackOpened() {
        if (_step.value == HoldingsWalkthroughStep.SETTINGS_FEEDBACK) {
            _step.value = HoldingsWalkthroughStep.COMPLETE
        }
    }

    fun acknowledgeComplete() {
        _step.value = HoldingsWalkthroughStep.INACTIVE
        _anchors.value = emptyMap()
        _selectedAssetSymbol.value = null
        _highlightCoinId.value = null
        _chromeHidden.value = false
        _overlaySuppressed.value = false
        _path.value = WalkthroughPath.CRYPTO
    }

    fun isActive(): Boolean {
        val s = _step.value
        return s != HoldingsWalkthroughStep.INACTIVE && s != HoldingsWalkthroughStep.COMPLETE
    }

    fun highlightAnchor(): WalkthroughAnchor? = when (_step.value) {
        HoldingsWalkthroughStep.HOLDINGS_TAP_ADD -> WalkthroughAnchor.ADD_BUTTON
        HoldingsWalkthroughStep.HOLDINGS_TAP_CARD,
        HoldingsWalkthroughStep.HOLDINGS_EDIT,
        HoldingsWalkthroughStep.HOLDINGS_DELETE -> WalkthroughAnchor.ASSET_CARD
        HoldingsWalkthroughStep.OPEN_SETTINGS -> WalkthroughAnchor.SETTINGS_TAB
        HoldingsWalkthroughStep.SETTINGS_FEEDBACK -> WalkthroughAnchor.FEEDBACK_ROW
        else -> null
    }

    fun shouldDeferKeyboardFocus(): Boolean = when (_step.value) {
        HoldingsWalkthroughStep.PICKER_SELECT_PROVIDER,
        HoldingsWalkthroughStep.PICKER_CHOOSE_PROVIDER,
        HoldingsWalkthroughStep.PICKER_TYPE_SEARCH,
        HoldingsWalkthroughStep.AMOUNT_ENTER_SAVE,
        in metalArchitectBlueprintSteps -> true
        else -> false
    }
}

internal val metalArchitectBlueprintSteps = setOf(
    HoldingsWalkthroughStep.METAL_ARCHITECT_SELECT_METAL,
    HoldingsWalkthroughStep.METAL_ARCHITECT_SELECT_SHAPE,
    HoldingsWalkthroughStep.METAL_ARCHITECT_SELECT_UNIT,
    HoldingsWalkthroughStep.METAL_ARCHITECT_TAP_NEXT,
)

internal val metalArchitectLiveCardSteps = setOf(
    HoldingsWalkthroughStep.METAL_ARCHITECT_LIVE_WEIGHT,
    HoldingsWalkthroughStep.METAL_ARCHITECT_LIVE_QUANTITY_NAME,
    HoldingsWalkthroughStep.METAL_ARCHITECT_LIVE_PREMIUM_MODE,
    HoldingsWalkthroughStep.METAL_ARCHITECT_LIVE_TAP_NEXT,
)

internal val metalArchitectIconPickSteps = setOf(
    HoldingsWalkthroughStep.METAL_ARCHITECT_ICON_PICK,
)

@Composable
fun Modifier.walkthroughAnchor(
    anchor: WalkthroughAnchor,
    controller: HoldingsWalkthroughController,
    enabled: Boolean = true,
): Modifier {
    if (!enabled) return this
    return onGloballyPositioned { coordinates ->
        controller.updateAnchor(anchor, coordinates.boundsInWindow())
    }
}

private fun Rect.relativeTo(origin: Offset): Rect = Rect(
    left - origin.x,
    top - origin.y,
    right - origin.x,
    bottom - origin.y,
)

@Composable
fun HoldingsWalkthroughOverlay(
    controller: HoldingsWalkthroughController,
    onCompleted: () -> Unit,
    onSkipTour: (dontShowAgain: Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val step by controller.step.collectAsState()
    val anchors by controller.anchors.collectAsState()
    val selectedSymbol by controller.selectedAssetSymbol.collectAsState()
    val chromeHidden by controller.chromeHidden.collectAsState()
    val overlaySuppressed by controller.overlaySuppressed.collectAsState()

    LaunchedEffect(step) {
        if (step == HoldingsWalkthroughStep.COMPLETE) {
            onCompleted()
        }
    }

    if (!controller.isActive() || chromeHidden || overlaySuppressed) {
        return
    }

    val hint = hintForStep(step, selectedSymbol)
    val anchorRect = hint.anchor?.let { anchors[it] }
    val highlightRect = (hint.highlightAnchor ?: hint.anchor)?.let { anchors[it] }
    var overlayOrigin by remember { mutableStateOf(Offset.Zero) }
    val overlayAnchor = anchorRect?.relativeTo(overlayOrigin)
    val overlayHighlight = highlightRect?.relativeTo(overlayOrigin)

    Box(
        modifier = modifier
            .fillMaxSize()
            .zIndex(200f)
            .onGloballyPositioned { overlayOrigin = it.boundsInWindow().topLeft },
    ) {
        WalkthroughPointerHint(
            label = hint.label,
            placement = hint.placement,
            horizontalAlign = hint.horizontalAlign,
            arrowAlign = hint.arrowAlign,
            arrowHorizontalFraction = hint.arrowHorizontalFraction,
            anchor = overlayAnchor,
            showNext = hint.showNext,
            nextLabelRes = hint.nextLabelRes,
            styledFinishButton = hint.styledFinishButton,
            maxLabelLines = hint.maxLabelLines,
            maxLabelWidth = hint.maxLabelWidth,
            maxCalloutWidth = hint.maxCalloutWidth,
            arrowAbovePill = hint.arrowAbovePill,
            onSkip = onSkipTour,
            onNext = { controller.onNextFromMessageOnly() },
        )
        overlayHighlight?.let { rect ->
            WalkthroughTargetHighlight(rect = rect)
        }
    }
}

@Composable
private fun hintForStep(
    step: HoldingsWalkthroughStep,
    selectedSymbol: String?,
): HintStep {
    val symbolFallback = stringResource(R.string.walkthrough_amount_symbol_fallback)
    val symbol = selectedSymbol?.takeIf { it.isNotBlank() } ?: symbolFallback
    return when (step) {
        HoldingsWalkthroughStep.HOLDINGS_TAP_ADD -> HintStep(
            label = stringResource(R.string.walkthrough_hint_add_asset),
            placement = CalloutPlacement.ANCHOR_BELOW,
            anchor = WalkthroughAnchor.ADD_BUTTON,
        )
        HoldingsWalkthroughStep.PICKER_SELECT_PROVIDER -> HintStep(
            label = stringResource(R.string.walkthrough_hint_select_provider),
            placement = CalloutPlacement.SCREEN_TOP,
            horizontalAlign = HintHorizontalAlign.End,
            arrowHorizontalFraction = 0.7f,
            anchor = WalkthroughAnchor.PICKER_PROVIDER_BUTTON,
        )
        HoldingsWalkthroughStep.PICKER_CHOOSE_PROVIDER -> HintStep(
            label = stringResource(R.string.walkthrough_hint_choose_provider),
            placement = CalloutPlacement.ANCHOR_BELOW,
            horizontalAlign = HintHorizontalAlign.End,
            arrowAlign = HintHorizontalAlign.End,
            anchor = WalkthroughAnchor.PICKER_DROPDOWN,
        )
        HoldingsWalkthroughStep.PICKER_TYPE_SEARCH -> HintStep(
            label = stringResource(R.string.walkthrough_hint_type_search),
            placement = CalloutPlacement.SCREEN_TOP,
            horizontalAlign = HintHorizontalAlign.Start,
            arrowAlign = HintHorizontalAlign.Start,
            anchor = WalkthroughAnchor.PICKER_SEARCH_BOX,
        )
        HoldingsWalkthroughStep.PICKER_SCROLL_RESULTS -> HintStep(
            label = stringResource(R.string.walkthrough_hint_scroll_results),
            placement = CalloutPlacement.SCREEN_TOP,
            horizontalAlign = HintHorizontalAlign.Center,
            arrowAlign = HintHorizontalAlign.Center,
            maxLabelLines = 2,
            maxLabelWidth = 300.dp,
            maxCalloutWidth = 360.dp,
        )
        HoldingsWalkthroughStep.METAL_ARCHITECT_SELECT_METAL -> HintStep(
            label = stringResource(R.string.walkthrough_hint_metal_select_metal),
            placement = CalloutPlacement.ANCHOR_ABOVE_INLINE,
            horizontalAlign = HintHorizontalAlign.Center,
            arrowAlign = HintHorizontalAlign.Center,
            anchor = WalkthroughAnchor.METAL_ARCHITECT_METAL_GRID,
        )
        HoldingsWalkthroughStep.METAL_ARCHITECT_SELECT_SHAPE -> HintStep(
            label = stringResource(R.string.walkthrough_hint_metal_select_shape),
            placement = CalloutPlacement.ANCHOR_ABOVE_INLINE,
            horizontalAlign = HintHorizontalAlign.Center,
            arrowAlign = HintHorizontalAlign.Center,
            anchor = WalkthroughAnchor.METAL_ARCHITECT_SHAPE_GRID,
        )
        HoldingsWalkthroughStep.METAL_ARCHITECT_SELECT_UNIT -> HintStep(
            label = stringResource(R.string.walkthrough_hint_metal_select_unit),
            placement = CalloutPlacement.ANCHOR_ABOVE_INLINE,
            horizontalAlign = HintHorizontalAlign.Center,
            arrowAlign = HintHorizontalAlign.Center,
            anchor = WalkthroughAnchor.METAL_ARCHITECT_UNIT_GRID,
        )
        HoldingsWalkthroughStep.METAL_ARCHITECT_TAP_NEXT -> HintStep(
            label = stringResource(R.string.walkthrough_hint_metal_tap_next),
            placement = CalloutPlacement.ANCHOR_ABOVE_INLINE,
            horizontalAlign = HintHorizontalAlign.Center,
            arrowAlign = HintHorizontalAlign.Center,
            anchor = WalkthroughAnchor.METAL_ARCHITECT_CONTINUE,
        )
        HoldingsWalkthroughStep.METAL_ARCHITECT_LIVE_WEIGHT -> HintStep(
            label = stringResource(R.string.walkthrough_hint_metal_live_weight),
            placement = CalloutPlacement.ANCHOR_BELOW,
            horizontalAlign = HintHorizontalAlign.Center,
            arrowAlign = HintHorizontalAlign.Center,
            anchor = WalkthroughAnchor.METAL_ARCHITECT_LIVE_WEIGHT,
            arrowAbovePill = true,
            maxLabelLines = 6,
            maxLabelWidth = 320.dp,
            maxCalloutWidth = 380.dp,
        )
        HoldingsWalkthroughStep.METAL_ARCHITECT_LIVE_QUANTITY_NAME -> HintStep(
            label = stringResource(R.string.walkthrough_hint_metal_live_quantity),
            placement = CalloutPlacement.ANCHOR_BELOW,
            horizontalAlign = HintHorizontalAlign.Center,
            arrowAlign = HintHorizontalAlign.Center,
            anchor = WalkthroughAnchor.METAL_ARCHITECT_LIVE_QUANTITY_NAME,
            arrowAbovePill = true,
            maxLabelLines = 4,
            maxLabelWidth = 320.dp,
            maxCalloutWidth = 380.dp,
        )
        HoldingsWalkthroughStep.METAL_ARCHITECT_LIVE_PREMIUM_MODE -> HintStep(
            label = stringResource(R.string.walkthrough_hint_metal_live_premium_mode),
            placement = CalloutPlacement.ANCHOR_BELOW,
            horizontalAlign = HintHorizontalAlign.Center,
            arrowAlign = HintHorizontalAlign.Center,
            anchor = WalkthroughAnchor.METAL_ARCHITECT_LIVE_PREMIUM_MODE,
            arrowAbovePill = true,
            maxLabelLines = 10,
            maxLabelWidth = 320.dp,
            maxCalloutWidth = 380.dp,
        )
        HoldingsWalkthroughStep.METAL_ARCHITECT_LIVE_TAP_NEXT -> HintStep(
            label = stringResource(R.string.walkthrough_hint_metal_live_tap_next),
            placement = CalloutPlacement.ANCHOR_ABOVE,
            horizontalAlign = HintHorizontalAlign.Center,
            arrowAlign = HintHorizontalAlign.Center,
            anchor = WalkthroughAnchor.METAL_ARCHITECT_LIVE_ICON_NEXT,
            arrowAbovePill = true,
            maxLabelLines = 3,
            maxLabelWidth = 320.dp,
            maxCalloutWidth = 380.dp,
        )
        HoldingsWalkthroughStep.METAL_ARCHITECT_ICON_PICK -> HintStep(
            label = stringResource(R.string.walkthrough_hint_metal_icon_pick),
            placement = CalloutPlacement.ANCHOR_ABOVE,
            horizontalAlign = HintHorizontalAlign.Center,
            arrowAlign = HintHorizontalAlign.Center,
            anchor = WalkthroughAnchor.METAL_ARCHITECT_ICON_ADD,
            arrowAbovePill = true,
            maxLabelLines = 5,
            maxLabelWidth = 320.dp,
            maxCalloutWidth = 380.dp,
        )
        HoldingsWalkthroughStep.AMOUNT_ENTER_SAVE -> HintStep(
            label = stringResource(R.string.walkthrough_hint_amount, symbol),
            placement = CalloutPlacement.ANCHOR_ABOVE,
            horizontalAlign = HintHorizontalAlign.Start,
            arrowAlign = HintHorizontalAlign.Start,
            anchor = WalkthroughAnchor.AMOUNT_INPUT,
            maxLabelLines = 2,
            maxLabelWidth = 300.dp,
            maxCalloutWidth = 360.dp,
        )
        HoldingsWalkthroughStep.HOLDINGS_TAP_CARD -> HintStep(
            label = stringResource(R.string.walkthrough_hint_card_gestures),
            placement = CalloutPlacement.ANCHOR_BELOW,
            anchor = WalkthroughAnchor.ASSET_CARD,
            showNext = true,
            nextLabelRes = R.string.walkthrough_end,
            styledFinishButton = true,
            maxLabelLines = 2,
            maxLabelWidth = 260.dp,
            maxCalloutWidth = 300.dp,
        )
        HoldingsWalkthroughStep.HOLDINGS_EDIT,
        HoldingsWalkthroughStep.HOLDINGS_DELETE -> HintStep(
            label = stringResource(R.string.walkthrough_hint_card_gestures),
            placement = CalloutPlacement.ANCHOR_BELOW,
            anchor = WalkthroughAnchor.ASSET_CARD,
            showNext = true,
            nextLabelRes = R.string.walkthrough_end,
            styledFinishButton = true,
            maxLabelLines = 2,
            maxLabelWidth = 260.dp,
            maxCalloutWidth = 300.dp,
        )
        HoldingsWalkthroughStep.OPEN_SETTINGS -> HintStep(
            label = stringResource(R.string.walkthrough_hint_settings),
            placement = CalloutPlacement.ANCHOR_ABOVE,
            anchor = WalkthroughAnchor.SETTINGS_TAB,
        )
        HoldingsWalkthroughStep.SETTINGS_FEEDBACK -> HintStep(
            label = stringResource(R.string.walkthrough_hint_feedback),
            placement = CalloutPlacement.ANCHOR_ABOVE,
            anchor = WalkthroughAnchor.FEEDBACK_ROW,
        )
        else -> HintStep("", CalloutPlacement.SCREEN_BOTTOM)
    }
}

@Composable
fun TakeTourInviteButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pulse = rememberWalkthroughPulse()

    Button(
        onClick = onClick,
        modifier = modifier
            .height(48.dp)
            .wrapContentWidth()
            .graphicsLayer {
                scaleX = pulse.scale
                scaleY = pulse.scale
            },
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(
            containerColor = TourYellow.copy(alpha = pulse.glowAlpha),
            contentColor = Color.Black,
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
        contentPadding = PaddingValues(horizontal = 28.dp),
    ) {
        Text(
            text = stringResource(R.string.walkthrough_take_tour),
            fontWeight = FontWeight.Black,
            fontSize = 15.sp,
        )
    }
}

@Composable
private fun WalkthroughPointerHint(
    label: String,
    placement: CalloutPlacement,
    horizontalAlign: HintHorizontalAlign,
    arrowAlign: HintHorizontalAlign,
    arrowHorizontalFraction: Float?,
    anchor: Rect?,
    showNext: Boolean,
    nextLabelRes: Int,
    styledFinishButton: Boolean,
    maxLabelLines: Int,
    maxLabelWidth: Dp,
    maxCalloutWidth: Dp,
    arrowAbovePill: Boolean,
    onSkip: (dontShowAgain: Boolean) -> Unit,
    onNext: () -> Unit,
) {
    val arrowDirection = arrowDirectionFor(placement)
    val pillFirst = (placement == CalloutPlacement.ANCHOR_ABOVE && !arrowAbovePill) ||
        placement == CalloutPlacement.SCREEN_TOP
    val arrowFirstInColumn = placement == CalloutPlacement.ANCHOR_BELOW ||
        (placement == CalloutPlacement.ANCHOR_ABOVE && arrowAbovePill)
    val inlineArrowBesidePill = placement == CalloutPlacement.ANCHOR_ABOVE_INLINE ||
        placement == CalloutPlacement.ANCHOR_BELOW_INLINE
    val columnArrowAlign = hintHorizontalAlignment(arrowAlign)
    val density = LocalDensity.current
    var calloutSize by remember { mutableStateOf(IntSize.Zero) }
    var pillWidthPx by remember { mutableIntStateOf(0) }
    val arrowWidthPx = with(density) { 36.dp.toPx() }
    val horizontalPad = with(density) { 16.dp.toPx() }
    val verticalPad = with(density) { 12.dp.toPx() }
    val gap = with(density) { 8.dp.toPx() }
    val arrowAlongAxis = with(density) { 40.dp.toPx() }

    val screenBox = remember { mutableStateOf(IntSize.Zero) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { screenBox.value = it.size },
    ) {
        val screenW = screenBox.value.width.toFloat()
        val screenH = screenBox.value.height.toFloat()
        val minTopY = with(density) { 72.dp.toPx() }
        val minHeaderY = with(density) { 56.dp.toPx() }
        val maxBottomPad = with(density) { 24.dp.toPx() }
        val maxY = if (screenH > 0f) {
            max(minTopY, screenH - calloutSize.height - verticalPad)
        } else {
            minTopY
        }
        val maxX = if (screenW > 0f) {
            max(horizontalPad, screenW - calloutSize.width - horizontalPad)
        } else {
            horizontalPad
        }

        val offset = when (placement) {
            CalloutPlacement.SCREEN_BOTTOM -> {
                IntOffset(
                    x = horizontalOffsetX(horizontalAlign, screenW, calloutSize.width, horizontalPad),
                    y = if (screenH > 0f) {
                        (screenH - calloutSize.height - verticalPad - maxBottomPad).roundToInt()
                    } else {
                        0
                    },
                )
            }
            CalloutPlacement.SCREEN_TOP -> {
                IntOffset(
                    x = horizontalOffsetX(horizontalAlign, screenW, calloutSize.width, horizontalPad),
                    y = minTopY.roundToInt(),
                )
            }
            CalloutPlacement.ANCHOR_BELOW -> if (anchor != null) {
                val x = when (horizontalAlign) {
                    HintHorizontalAlign.Center -> horizontalOffsetX(
                        HintHorizontalAlign.Center,
                        screenW,
                        calloutSize.width,
                        horizontalPad,
                    ).toFloat()
                    HintHorizontalAlign.Start -> coerceInRange(anchor.left, horizontalPad, maxX)
                    HintHorizontalAlign.End -> coerceInRange(
                        anchor.right - calloutSize.width,
                        horizontalPad,
                        maxX,
                    )
                }
                IntOffset(
                    x = x.roundToInt(),
                    y = (anchor.bottom + gap).roundToInt(),
                )
            } else {
                IntOffset(horizontalPad.roundToInt(), (screenH * 0.2f).roundToInt())
            }
            CalloutPlacement.ANCHOR_ABOVE -> if (anchor != null) {
                val x = when (horizontalAlign) {
                    HintHorizontalAlign.Center -> horizontalOffsetX(
                        HintHorizontalAlign.Center,
                        screenW,
                        calloutSize.width,
                        horizontalPad,
                    ).toFloat()
                    HintHorizontalAlign.Start -> coerceInRange(anchor.left, horizontalPad, maxX)
                    HintHorizontalAlign.End -> coerceInRange(
                        anchor.right - calloutSize.width,
                        horizontalPad,
                        maxX,
                    )
                }
                val aboveY = if (arrowAbovePill) {
                    anchor.top - gap - calloutSize.height
                } else {
                    anchor.top - gap - arrowAlongAxis - calloutSize.height
                }
                IntOffset(
                    x = x.roundToInt(),
                    y = coerceInRange(
                        aboveY,
                        minHeaderY,
                        maxY,
                    ).roundToInt(),
                )
            } else {
                IntOffset(horizontalPad.roundToInt(), (screenH * 0.65f).roundToInt())
            }
            CalloutPlacement.ANCHOR_ABOVE_INLINE -> if (anchor != null) {
                val anchorX = when (horizontalAlign) {
                    HintHorizontalAlign.Start -> anchor.left
                    HintHorizontalAlign.End -> anchor.right - calloutSize.width
                    HintHorizontalAlign.Center -> anchor.center.x - calloutSize.width / 2f
                }
                IntOffset(
                    x = coerceInRange(anchorX, horizontalPad, maxX).roundToInt(),
                    y = coerceInRange(
                        anchor.top - gap - calloutSize.height,
                        minHeaderY,
                        maxY,
                    ).roundToInt(),
                )
            } else {
                IntOffset(horizontalPad.roundToInt(), (screenH * 0.65f).roundToInt())
            }
            CalloutPlacement.ANCHOR_BELOW_INLINE -> if (anchor != null) {
                val anchorX = when (horizontalAlign) {
                    HintHorizontalAlign.Start -> anchor.left
                    HintHorizontalAlign.End -> anchor.right - calloutSize.width
                    HintHorizontalAlign.Center -> anchor.center.x - calloutSize.width / 2f
                }
                IntOffset(
                    x = coerceInRange(anchorX, horizontalPad, maxX).roundToInt(),
                    y = coerceInRange(
                        anchor.bottom + gap,
                        minHeaderY,
                        maxY,
                    ).roundToInt(),
                )
            } else {
                IntOffset(horizontalPad.roundToInt(), (screenH * 0.35f).roundToInt())
            }
            CalloutPlacement.ANCHOR_LEFT -> if (anchor != null) {
                val desiredX = anchor.left - gap - calloutSize.width
                val desiredY = anchor.center.y - calloutSize.height / 2f
                IntOffset(
                    x = coerceInRange(desiredX, horizontalPad, maxX).roundToInt(),
                    y = coerceInRange(desiredY, minTopY, maxY).roundToInt(),
                )
            } else {
                IntOffset(horizontalPad.roundToInt(), with(density) { 96.dp.toPx() }.roundToInt())
            }
        }

        val anchorCenterX = anchor?.center?.x
        val tipOffsetX = if (arrowHorizontalFraction == null && arrowAlign != HintHorizontalAlign.Start) {
            anchorCenterX?.let { anchorX ->
                with(density) { (anchorX - offset.x - calloutSize.width / 2f).toDp() }
            }
        } else {
            null
        }
        val fractionArrowOffsetX = arrowHorizontalFraction?.let { fraction ->
            if (pillWidthPx > 0) {
                with(density) {
                    (pillWidthPx * fraction - arrowWidthPx / 2f)
                        .coerceIn(0f, (pillWidthPx - arrowWidthPx).coerceAtLeast(0f))
                        .toDp()
                }
            } else {
                null
            }
        }

        Box(
            modifier = Modifier
                .offset { offset }
                .wrapContentSize(unbounded = true)
                .onGloballyPositioned { calloutSize = it.size }
                .widthIn(max = maxCalloutWidth),
        ) {
            if (placement == CalloutPlacement.ANCHOR_LEFT || inlineArrowBesidePill) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    WalkthroughGlossyArrow(direction = arrowDirection)
                    WalkthroughHintPill(
                        label = label,
                        showNext = showNext,
                        nextLabelRes = nextLabelRes,
                        styledFinishButton = styledFinishButton,
                        maxLabelLines = maxLabelLines,
                        maxLabelWidth = maxLabelWidth,
                        onSkip = onSkip,
                        onNext = onNext,
                        modifier = if (inlineArrowBesidePill) {
                            Modifier.onGloballyPositioned { pillWidthPx = it.size.width }
                        } else {
                            Modifier
                        },
                    )
                }
            } else {
                Column(
                    modifier = Modifier.wrapContentWidth(),
                    horizontalAlignment = columnArrowAlign,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    if (arrowFirstInColumn) {
                        WalkthroughFractionalArrow(
                            arrowDirection = arrowDirection,
                            pillWidthPx = pillWidthPx,
                            arrowHorizontalFraction = arrowHorizontalFraction,
                            tipOffsetX = tipOffsetX,
                            fractionArrowOffsetX = fractionArrowOffsetX,
                            columnArrowAlign = columnArrowAlign,
                        )
                        WalkthroughHintPill(
                            label = label,
                            showNext = showNext,
                            nextLabelRes = nextLabelRes,
                            styledFinishButton = styledFinishButton,
                            maxLabelLines = maxLabelLines,
                            maxLabelWidth = maxLabelWidth,
                            onSkip = onSkip,
                            onNext = onNext,
                            modifier = Modifier.onGloballyPositioned { pillWidthPx = it.size.width },
                        )
                    } else {
                        WalkthroughHintPill(
                            label = label,
                            showNext = showNext,
                            nextLabelRes = nextLabelRes,
                            styledFinishButton = styledFinishButton,
                            maxLabelLines = maxLabelLines,
                            maxLabelWidth = maxLabelWidth,
                            onSkip = onSkip,
                            onNext = onNext,
                            modifier = Modifier.onGloballyPositioned { pillWidthPx = it.size.width },
                        )
                        WalkthroughFractionalArrow(
                            arrowDirection = arrowDirection,
                            pillWidthPx = pillWidthPx,
                            arrowHorizontalFraction = arrowHorizontalFraction,
                            tipOffsetX = tipOffsetX,
                            fractionArrowOffsetX = fractionArrowOffsetX,
                            columnArrowAlign = columnArrowAlign,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WalkthroughFractionalArrow(
    arrowDirection: WalkthroughArrowDirection,
    pillWidthPx: Int,
    arrowHorizontalFraction: Float?,
    tipOffsetX: Dp?,
    fractionArrowOffsetX: Dp?,
    columnArrowAlign: Alignment.Horizontal,
) {
    val density = LocalDensity.current
    if (arrowHorizontalFraction != null) {
        Box(
            modifier = if (pillWidthPx > 0) {
                Modifier.width(with(density) { pillWidthPx.toDp() })
            } else {
                Modifier.wrapContentWidth()
            },
            contentAlignment = Alignment.CenterStart,
        ) {
            WalkthroughGlossyArrow(
                direction = arrowDirection,
                horizontalBias = fractionArrowOffsetX,
            )
        }
    } else {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = when (columnArrowAlign) {
                Alignment.Start -> Alignment.CenterStart
                Alignment.CenterHorizontally -> Alignment.Center
                else -> Alignment.CenterEnd
            },
        ) {
            WalkthroughGlossyArrow(
                direction = arrowDirection,
                horizontalBias = tipOffsetX,
            )
        }
    }
}

@Composable
private fun WalkthroughHintPill(
    label: String,
    showNext: Boolean,
    nextLabelRes: Int,
    styledFinishButton: Boolean,
    maxLabelLines: Int,
    maxLabelWidth: Dp,
    onSkip: (dontShowAgain: Boolean) -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showExitDialog by remember { mutableStateOf(false) }
    var dontShowAgain by remember { mutableStateOf(false) }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = {
                showExitDialog = false
                dontShowAgain = false
            },
            title = {
                Text(
                    text = stringResource(R.string.walkthrough_exit_title),
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        checked = dontShowAgain,
                        onCheckedChange = { dontShowAgain = it },
                    )
                    Text(
                        text = stringResource(R.string.walkthrough_exit_dont_show_again),
                        fontWeight = FontWeight.Medium,
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val hideTour = dontShowAgain
                        showExitDialog = false
                        dontShowAgain = false
                        onSkip(hideTour)
                    },
                ) {
                    Text(
                        text = stringResource(R.string.walkthrough_exit_yes),
                        color = Color.Red,
                        fontWeight = FontWeight.Bold,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showExitDialog = false
                    dontShowAgain = false
                }) {
                    Text(
                        text = stringResource(R.string.walkthrough_exit_no),
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            },
        )
    }

    Box(
        modifier = modifier.padding(top = 12.dp, end = 12.dp),
    ) {
        Surface(
            shape = if (maxLabelLines > 1) RoundedCornerShape(18.dp) else RoundedCornerShape(50),
            color = TourYellow,
            shadowElevation = 6.dp,
            tonalElevation = 2.dp,
        ) {
            Column(
                modifier = Modifier.padding(
                    start = 14.dp,
                    end = 18.dp,
                    top = if (maxLabelLines > 1) 8.dp else 7.dp,
                    bottom = if (maxLabelLines > 1) 8.dp else 7.dp,
                ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = label,
                    color = Color.Black,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = maxLabelLines,
                    overflow = TextOverflow.Clip,
                    lineHeight = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.widthIn(max = maxLabelWidth),
                )
                if (showNext) {
                    if (styledFinishButton) {
                        Surface(
                            onClick = onNext,
                            shape = RoundedCornerShape(50),
                            color = Color.Black,
                            shadowElevation = 2.dp,
                        ) {
                            Text(
                                text = stringResource(nextLabelRes),
                                color = TourYellow,
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 22.dp, vertical = 6.dp),
                            )
                        }
                    } else {
                        TextButton(
                            onClick = onNext,
                            contentPadding = PaddingValues(horizontal = 6.dp),
                        ) {
                            Text(
                                text = stringResource(nextLabelRes),
                                color = Color.Black,
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp,
                            )
                        }
                    }
                }
            }
        }
        Surface(
            onClick = { showExitDialog = true },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 4.dp, y = (-2).dp)
                .size(22.dp)
                .zIndex(1f),
            shape = CircleShape,
            color = Color(0xFFE53935),
            shadowElevation = 4.dp,
            border = BorderStroke(2.dp, Color.White),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "×",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    lineHeight = 15.sp,
                )
            }
        }
    }
}

@Composable
private fun WalkthroughGlossyArrow(
    direction: WalkthroughArrowDirection,
    modifier: Modifier = Modifier,
    horizontalBias: Dp? = null,
) {
    val pulse = rememberWalkthroughPulse()
    val isHorizontal = direction == WalkthroughArrowDirection.Left ||
        direction == WalkthroughArrowDirection.Right
    val arrowWidth = if (isHorizontal) 52.dp else 36.dp
    val arrowHeight = if (isHorizontal) 36.dp else 44.dp
    val rotation = when (direction) {
        WalkthroughArrowDirection.Right -> 0f
        WalkthroughArrowDirection.Down -> 90f
        WalkthroughArrowDirection.Left -> 180f
        WalkthroughArrowDirection.Up -> 270f
    }

    Box(
        modifier = modifier
            .then(
                if (horizontalBias != null && !isHorizontal) {
                    Modifier.offset(x = horizontalBias)
                } else {
                    Modifier
                },
            )
            .size(width = arrowWidth, height = arrowHeight)
            .graphicsLayer {
                scaleX = pulse.scale
                scaleY = pulse.scale
            },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val arrowPath = buildGlossyArrowPath(size.width, size.height)
            val pivotX = size.width / 2f
            val pivotY = size.height / 2f
            rotate(rotation, Offset(pivotX, pivotY)) {
                translate(left = 2f, top = 3f) {
                    drawPath(path = arrowPath, color = TourYellowShadow, style = Fill, alpha = 0.5f)
                }
                drawPath(path = arrowPath, color = Color.White, style = Stroke(width = 3.5f))
                drawPath(
                    path = arrowPath,
                    color = TourYellow.copy(alpha = pulse.glowAlpha),
                    style = Fill,
                )
                clipPath(arrowPath) {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.38f),
                        radius = size.height * 0.34f,
                        center = Offset(size.width * 0.34f, size.height * 0.27f),
                    )
                }
            }
        }
    }
}

private fun buildGlossyArrowPath(width: Float, height: Float): Path {
    val shaftInset = width * 0.08f
    val shaftTop = height * 0.30f
    val shaftBottom = height * 0.70f
    val shaftEnd = width * 0.56f
    val neckTop = height * 0.14f
    val neckBottom = height * 0.86f
    val tipX = width * 0.93f
    val midY = height * 0.5f
    val tailRadius = height * 0.20f

    return Path().apply {
        moveTo(shaftInset + tailRadius, shaftTop)
        lineTo(shaftEnd, shaftTop)
        lineTo(shaftEnd, neckTop)
        lineTo(tipX, midY)
        lineTo(shaftEnd, neckBottom)
        lineTo(shaftEnd, shaftBottom)
        lineTo(shaftInset + tailRadius, shaftBottom)
        quadraticTo(shaftInset, midY, shaftInset + tailRadius, shaftTop)
        close()
    }
}

@Composable
private fun WalkthroughTargetHighlight(rect: Rect) {
    val density = LocalDensity.current
    val pad = with(density) { 4.dp.toPx() }
    Canvas(
        modifier = Modifier
            .offset {
                IntOffset(
                    (rect.left - pad).roundToInt(),
                    (rect.top - pad).roundToInt(),
                )
            }
            .size(
                width = with(density) { (rect.width + pad * 2).toDp() },
                height = with(density) { (rect.height + pad * 2).toDp() },
            ),
    ) {
        drawRoundRect(
            color = TourYellow.copy(alpha = 0.35f),
            cornerRadius = CornerRadius(12f, 12f),
        )
        drawRoundRect(
            color = TourYellow,
            cornerRadius = CornerRadius(12f, 12f),
            style = Stroke(width = 2.5f),
        )
    }
}

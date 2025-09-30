package moritz.lindner.masterarbeit.ui.components.epaview.components.toolbar.animation

//import moritz.lindner.masterarbeit.ui.components.epaview.viewmodel.EpaViewModel

//@Composable
//fun TimelineSliderWholeLogUi(
//    extendedPrefixAutomaton: ExtendedPrefixAutomaton<Long>,
//    viewModel: EpaViewModel,
//    dispatcher: CoroutineDispatcher,
//    onClose: () -> Unit,
//) {
//    val animationService = AnimationService<Long>()
//
//    var isLoading by remember(extendedPrefixAutomaton) { mutableStateOf(true) }
//    var eventLogAnimation by remember(extendedPrefixAutomaton) { mutableStateOf<EventLogAnimation<Long>?>(null) }
//    var sliderValue by remember(extendedPrefixAutomaton) { mutableStateOf(0f) }
//    var isPlaying by remember(extendedPrefixAutomaton) { mutableStateOf(false) }
//    val speed = 17L // 60fps
//    var stepSize by remember(extendedPrefixAutomaton) { mutableStateOf(1L) }
//    var multiplier by remember(extendedPrefixAutomaton) { mutableStateOf(1.0f) }
//
//    LaunchedEffect(extendedPrefixAutomaton) {
//        isLoading = true
//        isPlaying = false
//        viewModel.updateAnimation(AnimationState.Empty)
//        withContext(dispatcher) {
//            eventLogAnimation = animationService.createFullLogAnimation(
//                extendedPrefixAutomaton,
//                10L,
//                Long::plus,
//            )
//        }
//        viewModel.updateAnimation(AnimationState.Empty)
//        sliderValue = eventLogAnimation!!.getFirst().first.toFloat()
//        isLoading = false
//    }
//
//    LaunchedEffect(isPlaying, extendedPrefixAutomaton) {
//        viewModel.updateAnimation(AnimationState.Empty)
//        if (isPlaying && eventLogAnimation != null) {
//            val first = eventLogAnimation!!.getFirst().first
//            val last = eventLogAnimation!!.getLast().first
//
//            val playbackSpeed = speed
//            stepSize =
//                findStepSize(
//                    start = first,
//                    end = last,
//                )
//
//            var timestamp = sliderValue.toLong()
//
//            while (timestamp <= last) {
//                val dynamicStepSize = (stepSize * multiplier).toLong()
//
//                logger.info { "running animation $timestamp" }
//                sliderValue = timestamp.toFloat()
//
//                val state = eventLogAnimation!!.getActiveStatesAt(timestamp)
//                viewModel.updateAnimation(
//                    AnimationState(
//                        time = timestamp,
//                        currentTimeStates = state.toSet(),
//                    ),
//                )
//
//                yield()
//                delay(playbackSpeed)
//                timestamp += dynamicStepSize
//            }
//            isPlaying = false
//        }
//    }
//
//    if (isLoading) {
//        CircularProgressIndicator()
//    } else if (eventLogAnimation != null) {
//        AnimationControlsUI(
//            isPlaying = isPlaying,
//            stepSize = stepSize,
//            multiplier = multiplier,
//            sliderValue = sliderValue,
//            animation = eventLogAnimation,
//            viewModel = viewModel,
//            onClose = onClose,
//            onSliderChange = {
//                sliderValue = it
//            },
//            onPlayToggle = {
//                isPlaying = it
//            },
//            onForward = {
//                multiplier += 0.25f
//            },
//            onBackward = {
//                multiplier -= 0.25f
//            },
//        )
//    }
//}

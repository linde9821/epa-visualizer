package moritz.lindner.masterarbeit.ui.components.epaview.components.toolbar.animation

//import moritz.lindner.masterarbeit.ui.components.epaview.viewmodel.EpaViewModel

//@Composable
//fun SingleCaseAnimationUI(
//    epa: ExtendedPrefixAutomaton<Long>,
//    backgroundDispatcher: ExecutorCoroutineDispatcher,
//    viewModel: EpaViewModel,
//    onClose: () -> Unit,
//) {
//    val epaService = EpaService<Long>()
//
//    var isLoading by remember { mutableStateOf(true) }
//    var showDialog by remember { mutableStateOf(false) }
//    var selectedCase by remember { mutableStateOf<String?>(null) }
//    var eventsByCase by remember { mutableStateOf(emptyMap<String, List<Event<Long>>>()) }
//
//    LaunchedEffect(epa) {
//        isLoading = true
//        withContext(backgroundDispatcher) {
//            selectedCase = null
//            eventsByCase = epaService.getEventsByCase(epa)
//        }
//        isLoading = false
//    }
//
//    Column(modifier = Modifier.padding(16.dp)) {
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp),
//            horizontalArrangement = Arrangement.spacedBy(16.dp),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            DefaultButton(
//                onClick = {
//                    showDialog = true
//                    selectedCase = null
//                    viewModel.updateAnimation(AnimationState.Empty)
//                }
//            ) {
//                Text("Select Case")
//            }
//
//            if (selectedCase != null) {
//                Text(
//                    text = "Case: $selectedCase",
//                    style = JewelTheme.defaultTextStyle,
//                    modifier = Modifier.weight(1f) // Takes available space
//                )
//            }
//
//            DefaultButton(
//                onClick = {
//                    viewModel.updateAnimation(AnimationState.Empty)
//                    onClose()
//                }
//            ) {
//                Text("Close")
//            }
//        }
//        Spacer(Modifier.height(8.dp))
//
//        if (selectedCase != null) {
//            TimelineSliderSingleCaseUi(epa, backgroundDispatcher, viewModel, selectedCase!!)
//        }
//
//        if (showDialog) {
//            DialogWindow(
//                onCloseRequest = { showDialog = false },
//                title = "Single Case Animation",
//                visible = showDialog,
//            ) {
//                if (!isLoading) {
//                    Box(
//                        Modifier.padding(16.dp),
//                    ) {
//                        Row {
//                            LazyColumn {
//                                items(eventsByCase.keys.toList()) { case ->
//                                    val isSelected = case == selectedCase
//                                    Text(
//                                        text = case,
//                                        modifier =
//                                            Modifier
//                                                .padding(8.dp)
//                                                .background(if (isSelected) Color.LightGray else Color.Transparent)
//                                                .clickable {
//                                                    selectedCase = case
//                                                    showDialog = false
//                                                }
//                                                .padding(8.dp),
//                                    )
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }
//}

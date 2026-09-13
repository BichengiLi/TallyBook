package com.example.tallybook.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tallybook.ui.theme.AnimePink

@Composable
fun ExpandableFAB(
    isRewardClaimed: Boolean,
    onRewardClick: () -> Unit,
    onAddClick: () -> Unit,
    onRewardLongPress: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    val animDuration = 250

    val rotation by animateFloatAsState(
        targetValue = if (expanded) -90f else 0f,
        animationSpec = tween(animDuration),
        label = "fab_rotation"
    )

    Box(modifier = modifier) {
        // 展开时全屏透明遮罩，点任意区域收回
        if (expanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        expanded = false
                    }
            )
        }

        // 面板 + 主按钮，定位在右下角
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 侧伸面板
            AnimatedVisibility(
                visible = expanded,
                enter = slideInHorizontally(
                    animationSpec = tween(animDuration),
                    initialOffsetX = { it }
                ) + fadeIn(animationSpec = tween(animDuration)),
                exit = slideOutHorizontally(
                    animationSpec = tween(animDuration),
                    targetOffsetX = { it }
                ) + fadeOut(animationSpec = tween(animDuration))
            ) {
                Card(
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // "忍" 按钮
                        val rewardEnabled = !isRewardClaimed
                        FloatingActionButton(
                            onClick = {
                                if (rewardEnabled) {
                                    onRewardClick()
                                    expanded = false
                                } else {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .then(
                                    if (rewardEnabled && onRewardLongPress != null) {
                                        Modifier.pointerInput(onRewardLongPress) {
                                            detectTapGestures(
                                                onLongPress = { onRewardLongPress() }
                                            )
                                        }
                                    } else {
                                        Modifier
                                    }
                                ),
                            containerColor = if (rewardEnabled) AnimePink
                                else Color.Gray.copy(alpha = 0.4f),
                            contentColor = if (rewardEnabled) Color.White
                                else Color.White.copy(alpha = 0.5f),
                            shape = CircleShape
                        ) {
                            Text(
                                text = "忍",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // "+" 按钮
                        FloatingActionButton(
                            onClick = {
                                onAddClick()
                                expanded = false
                            },
                            modifier = Modifier.size(48.dp),
                            containerColor = AnimePink,
                            contentColor = Color.White,
                            shape = CircleShape
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "添加记录"
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 主按钮 ✕
            FloatingActionButton(
                onClick = { expanded = !expanded },
                containerColor = AnimePink,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .size(56.dp)
                    .graphicsLayer {
                        rotationZ = rotation
                    }
            ) {
                Text(
                    text = "✕",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

package com.crashinvaders.laughemout.game.engine.components;

import com.esotericsoftware.spine.AnimationState
import com.esotericsoftware.spine.Skeleton
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

class SkeletonContainer(
    val skeleton: Skeleton,
    val animState: AnimationState
) : Component<SkeletonContainer> {
    override fun type() = SkeletonContainer
    companion object : ComponentType<SkeletonContainer>()
}
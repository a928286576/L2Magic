package dev.xkmc.l2magic.content.transport.tile.base;

import net.minecraft.core.BlockPos;

public interface ILinkableNode {

	void link(BlockPos clickedPos);

	int getMaxDistanceSqr();

}
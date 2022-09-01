package dev.xkmc.l2magic.init.registrate;

import dev.xkmc.l2library.block.BlockProxy;
import dev.xkmc.l2library.block.DelegateBlock;
import dev.xkmc.l2library.block.DelegateBlockProperties;
import dev.xkmc.l2library.repack.registrate.providers.DataGenContext;
import dev.xkmc.l2library.repack.registrate.providers.RegistrateBlockstateProvider;
import dev.xkmc.l2library.repack.registrate.util.entry.BlockEntityEntry;
import dev.xkmc.l2library.repack.registrate.util.entry.BlockEntry;
import dev.xkmc.l2magic.content.magic.block.RitualCore;
import dev.xkmc.l2magic.content.magic.block.RitualRenderer;
import dev.xkmc.l2magic.content.magic.block.RitualSide;
import dev.xkmc.l2magic.content.transport.tile.base.SidedBlockEntity;
import dev.xkmc.l2magic.content.transport.tile.block.ItemTransferBlock;
import dev.xkmc.l2magic.content.transport.tile.block.NodeSetFilter;
import dev.xkmc.l2magic.content.transport.tile.client.ItemNodeRenderer;
import dev.xkmc.l2magic.content.transport.tile.item.*;
import dev.xkmc.l2magic.init.L2Magic;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.client.model.generators.ConfiguredModel;

/**
 * handles blocks and block entities
 */
public class LMBlocks {

	static {
		L2Magic.REGISTRATE.creativeModeTab(() -> LMItems.TAB_MAIN);
	}

	public static final BlockEntry<DelegateBlock> B_RITUAL_CORE, B_RITUAL_SIDE;
	public static final BlockEntry<Block> ENCHANT_GOLD_BLOCK, MAGICIUM_BLOCK;

	public static final BlockEntry<DelegateBlock> B_SIDED, B_ITEM_SIMPLE, B_ITEM_ORDERED, B_ITEM_SYNCED, B_ITEM_DISTRIBUTE, B_ITEM_RETRIEVE;

	public static final BlockEntityEntry<RitualCore.TE> TE_RITUAL_CORE;
	public static final BlockEntityEntry<RitualSide.TE> TE_RITUAL_SIDE;

	public static final BlockEntityEntry<SimpleItemNodeBlockEntity> TE_ITEM_SIMPLE;
	public static final BlockEntityEntry<OrderedItemNodeBlockEntity> TE_ITEM_ORDERED;
	public static final BlockEntityEntry<SyncedItemNodeBlockEntity> TE_ITEM_SYNCED;
	public static final BlockEntityEntry<DistributeItemNodeBlockEntity> TE_ITEM_DISTRIBUTE;
	public static final BlockEntityEntry<RetrieverItemNodeBlockEntity> TE_ITEM_RETRIEVE;
	public static final BlockEntityEntry<SidedBlockEntity> TE_SIDED;

	static {
		{
			DelegateBlockProperties PEDESTAL = DelegateBlockProperties.copy(Blocks.STONE).make(e -> e
					.noOcclusion().lightLevel(bs -> bs.getValue(BlockStateProperties.LIT) ? 15 : 7)
					.isRedstoneConductor((a, b, c) -> false));
			B_RITUAL_CORE = L2Magic.REGISTRATE.block("ritual_core",
							(p) -> DelegateBlock.newBaseBlock(PEDESTAL, RitualCore.ACTIVATE, RitualCore.CLICK,
									BlockProxy.TRIGGER, RitualCore.TILE_ENTITY_SUPPLIER_BUILDER))
					.blockstate((ctx, pvd) -> pvd.simpleBlock(ctx.getEntry(), pvd.models().getExistingFile(
							new ResourceLocation(L2Magic.MODID, "block/ritual_core"))))
					.tag(BlockTags.MINEABLE_WITH_PICKAXE).defaultLoot().defaultLang().simpleItem().register();
			B_RITUAL_SIDE = L2Magic.REGISTRATE.block("ritual_side",
							(p) -> DelegateBlock.newBaseBlock(PEDESTAL, RitualCore.CLICK, RitualSide.TILE_ENTITY_SUPPLIER_BUILDER))
					.blockstate((ctx, pvd) -> pvd.simpleBlock(ctx.getEntry(), pvd.models().getExistingFile(
							new ResourceLocation(L2Magic.MODID, "block/ritual_side"))))
					.tag(BlockTags.MINEABLE_WITH_PICKAXE).defaultLoot().defaultLang().simpleItem().register();
			TE_RITUAL_CORE = L2Magic.REGISTRATE.blockEntity("ritual_core", RitualCore.TE::new)
					.validBlock(B_RITUAL_CORE).renderer(() -> RitualRenderer::new).register();
			TE_RITUAL_SIDE = L2Magic.REGISTRATE.blockEntity("ritual_side", RitualSide.TE::new)
					.validBlock(B_RITUAL_SIDE).renderer(() -> RitualRenderer::new).register();
		}
		{
			ENCHANT_GOLD_BLOCK = L2Magic.REGISTRATE.block("enchant_gold_block", p ->
							new Block(BlockBehaviour.Properties.copy(Blocks.GOLD_BLOCK)))
					.tag(BlockTags.MINEABLE_WITH_PICKAXE)
					.defaultBlockstate().defaultLoot().defaultLang().simpleItem().register();
			MAGICIUM_BLOCK = L2Magic.REGISTRATE.block("magicium_block",
							p -> new Block(Block.Properties.copy(Blocks.IRON_BLOCK)))
					.tag(BlockTags.MINEABLE_WITH_PICKAXE)
					.defaultBlockstate().defaultLoot().defaultLang().simpleItem().register();
		}
		{

			DelegateBlockProperties NOLIT = DelegateBlockProperties.copy(Blocks.STONE).make(e -> e
					.noOcclusion().lightLevel(bs -> 7)
					.isRedstoneConductor((a, b, c) -> false));

			DelegateBlockProperties LIT = DelegateBlockProperties.copy(Blocks.STONE).make(e -> e
					.noOcclusion().lightLevel(bs -> bs.getValue(BlockStateProperties.LIT) ? 15 : 7)
					.isRedstoneConductor((a, b, c) -> false));

			B_ITEM_SIMPLE = L2Magic.REGISTRATE.block("node_item_simple",
							(p) -> DelegateBlock.newBaseBlock(LIT, NodeSetFilter.INSTANCE, ItemTransferBlock.SIMPLE))
					.blockstate(LMBlocks::genNodeModel).tag(BlockTags.MINEABLE_WITH_PICKAXE)
					.defaultLoot().defaultLang().simpleItem().register();

			B_ITEM_ORDERED = L2Magic.REGISTRATE.block("node_item_ordered",
							(p) -> DelegateBlock.newBaseBlock(LIT, NodeSetFilter.INSTANCE, ItemTransferBlock.ORDERED))
					.blockstate(LMBlocks::genNodeModel).tag(BlockTags.MINEABLE_WITH_PICKAXE)
					.defaultLoot().defaultLang().simpleItem().register();

			B_ITEM_SYNCED = L2Magic.REGISTRATE.block("node_item_synced",
							(p) -> DelegateBlock.newBaseBlock(LIT, NodeSetFilter.INSTANCE, ItemTransferBlock.SYNCED))
					.blockstate(LMBlocks::genNodeModel).tag(BlockTags.MINEABLE_WITH_PICKAXE)
					.defaultLoot().defaultLang().simpleItem().register();

			B_ITEM_DISTRIBUTE = L2Magic.REGISTRATE.block("node_item_distribute",
							(p) -> DelegateBlock.newBaseBlock(LIT, NodeSetFilter.INSTANCE, ItemTransferBlock.DISTRIBUTE))
					.blockstate(LMBlocks::genNodeModel).tag(BlockTags.MINEABLE_WITH_PICKAXE)
					.defaultLoot().defaultLang().simpleItem().register();

			B_ITEM_RETRIEVE = L2Magic.REGISTRATE.block("node_item_retrieve",
							(p) -> DelegateBlock.newBaseBlock(LIT, NodeSetFilter.INSTANCE, BlockProxy.ALL_DIRECTION, ItemTransferBlock.RETRIEVE))
					.blockstate(LMBlocks::genFacingModel).tag(BlockTags.MINEABLE_WITH_PICKAXE)
					.defaultLoot().defaultLang().simpleItem().register();


			B_SIDED = L2Magic.REGISTRATE.block("node_sided",
							(p) -> DelegateBlock.newBaseBlock(NOLIT, BlockProxy.ALL_DIRECTION, ItemTransferBlock.SIDED))
					.blockstate(LMBlocks::genFacingModel).tag(BlockTags.MINEABLE_WITH_PICKAXE)
					.defaultLoot().defaultLang().simpleItem().register();

			TE_ITEM_SIMPLE = L2Magic.REGISTRATE.blockEntity("node_item_simple", SimpleItemNodeBlockEntity::new)
					.validBlock(B_ITEM_SIMPLE).renderer(() -> ItemNodeRenderer::new).register();
			TE_ITEM_ORDERED = L2Magic.REGISTRATE.blockEntity("node_item_ordered", OrderedItemNodeBlockEntity::new)
					.validBlock(B_ITEM_ORDERED).renderer(() -> ItemNodeRenderer::new).register();
			TE_ITEM_SYNCED = L2Magic.REGISTRATE.blockEntity("node_item_synced", SyncedItemNodeBlockEntity::new)
					.validBlock(B_ITEM_SYNCED).renderer(() -> ItemNodeRenderer::new).register();
			TE_ITEM_DISTRIBUTE = L2Magic.REGISTRATE.blockEntity("node_item_distribute", DistributeItemNodeBlockEntity::new)
					.validBlock(B_ITEM_DISTRIBUTE).renderer(() -> ItemNodeRenderer::new).register();
			TE_ITEM_RETRIEVE = L2Magic.REGISTRATE.blockEntity("node_item_retrieve", RetrieverItemNodeBlockEntity::new)
					.validBlock(B_ITEM_RETRIEVE).renderer(() -> ItemNodeRenderer::new).register();
			TE_SIDED = L2Magic.REGISTRATE.blockEntity("node_sided", SidedBlockEntity::new)
					.validBlock(B_SIDED).register();
		}
	}

	private static void genNodeModel(DataGenContext<Block, DelegateBlock> ctx, RegistrateBlockstateProvider pvd) {
		pvd.getVariantBuilder(ctx.getEntry()).forAllStates(bs -> {
			boolean lit = bs.getValue(BlockStateProperties.LIT);
			String model = ctx.getName() + (lit ? "_lit" : "");
			String name = ctx.getName().replace('_', '/') + (lit ? "_lit" : "");
			return ConfiguredModel.builder().modelFile(pvd.models()
					.withExistingParent(model, lit ?
							new ResourceLocation("block/cube_all") :
							new ResourceLocation(L2Magic.MODID, "block/node_small"))
					.texture("all", new ResourceLocation(L2Magic.MODID, "block/" + name))
					.renderType("cutout")).build();
		});
	}

	private static void genFacingModel(DataGenContext<Block, DelegateBlock> ctx, RegistrateBlockstateProvider pvd) {
		pvd.directionalBlock(ctx.getEntry(), bs -> {
			boolean lit = bs.hasProperty(BlockStateProperties.LIT) && bs.getValue(BlockStateProperties.LIT);
			String model = ctx.getName() + (lit ? "_lit" : "");
			String name = ctx.getName().replace('_', '/') + (lit ? "_lit" : "");
			return pvd.models()
					.withExistingParent(model, lit ?
							new ResourceLocation(L2Magic.MODID, "block/node_side_large") :
							new ResourceLocation(L2Magic.MODID, "block/node_side"))
					.texture("all", new ResourceLocation(L2Magic.MODID, "block/" + name))
					.renderType("cutout");
		});
	}

	public static void register() {
	}

}

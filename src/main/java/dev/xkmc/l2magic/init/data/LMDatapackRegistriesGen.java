package dev.xkmc.l2magic.init.data;

import dev.xkmc.l2magic.content.engine.core.ConfiguredEngine;
import dev.xkmc.l2magic.content.engine.core.SpellAction;
import dev.xkmc.l2magic.content.engine.core.SpellCastType;
import dev.xkmc.l2magic.content.engine.core.SpellTriggerType;
import dev.xkmc.l2magic.content.engine.instance.logic.ListInstance;
import dev.xkmc.l2magic.content.engine.instance.logic.PredicateInstance;
import dev.xkmc.l2magic.content.engine.instance.particle.BlockParticleInstance;
import dev.xkmc.l2magic.content.engine.instance.particle.SimpleParticleInstance;
import dev.xkmc.l2magic.content.engine.iterator.DelayedIterator;
import dev.xkmc.l2magic.content.engine.iterator.LinearIterator;
import dev.xkmc.l2magic.content.engine.iterator.LoopIterator;
import dev.xkmc.l2magic.content.engine.iterator.RingRandomIterator;
import dev.xkmc.l2magic.content.engine.modifier.*;
import dev.xkmc.l2magic.content.engine.variable.BooleanVariable;
import dev.xkmc.l2magic.content.engine.variable.DoubleVariable;
import dev.xkmc.l2magic.content.engine.variable.IntVariable;
import dev.xkmc.l2magic.init.L2Magic;
import dev.xkmc.l2magic.init.registrate.EngineRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.data.DatapackBuiltinEntriesProvider;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class LMDatapackRegistriesGen extends DatapackBuiltinEntriesProvider {

	public static final ResourceKey<SpellAction> WINTER = spell("winter_storm");
	public static final ResourceKey<SpellAction> FLAME = spell("flame_burst");
	public static final ResourceKey<SpellAction> QUACK = spell("earth_quack");

	private static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
			.add(EngineRegistry.SPELL, ctx -> {
				ctx.register(WINTER, new SpellAction(
						winterStorm(),
						Items.SNOWBALL,
						SpellCastType.CONTINUOUS,
						SpellTriggerType.SELF_POS
				));
				ctx.register(FLAME, new SpellAction(
						flameBurst(),
						Items.FIRE_CHARGE,
						SpellCastType.INSTANT,
						SpellTriggerType.TARGET_POS
				));
				ctx.register(QUACK, new SpellAction(
						flameBurstCircle(),
						Items.TNT,
						SpellCastType.CHARGE,
						SpellTriggerType.HORIZONTAL_FACING
				));

			});

	private static ConfiguredEngine<?> winterStorm() {
		return new DelayedIterator(
				IntVariable.of("10"),
				IntVariable.of("2"),
				new RingRandomIterator(
						DoubleVariable.of("3"),
						DoubleVariable.of("8"),
						DoubleVariable.of("-180"),
						DoubleVariable.of("180"),
						IntVariable.of("5"),
						new RotationModifier(
								DoubleVariable.of("75"),
								new OffsetModifier(
										DoubleVariable.ZERO,
										DoubleVariable.of("rand(0.5,2.5)"),
										DoubleVariable.ZERO,
										new SimpleParticleInstance(
												ParticleTypes.SNOWFLAKE,
												DoubleVariable.of("0.5")
										)
								)
						), null
				), null
		);
	}

	private static ConfiguredEngine<?> flameBurst() {
		return new ListInstance(List.of(
				new SetDirectionModifier(
						DoubleVariable.of("1"),
						DoubleVariable.ZERO,
						DoubleVariable.ZERO,
						star(4, 0.3)
				),
				new DelayedIterator(
						IntVariable.of("40"),
						IntVariable.of("1"),
						new RingRandomIterator(
								DoubleVariable.of("0"),
								DoubleVariable.of("4"),
								DoubleVariable.of("-180"),
								DoubleVariable.of("180"),
								IntVariable.of("10"),
								new RandomVariableModifier(
										"r", 4,
										new SetDirectionModifier(
												DoubleVariable.of("(r0-0.5)*0.2"),
												DoubleVariable.of("1"),
												DoubleVariable.of("(r1-0.5)*0.2"),
												new PredicateInstance(
														BooleanVariable.of("r2<0.25"),
														new SimpleParticleInstance(
																ParticleTypes.SOUL,
																DoubleVariable.of("0.5+r3*0.2")
														),
														new SimpleParticleInstance(
																ParticleTypes.FLAME,
																DoubleVariable.of("0.5+r3*0.2")
														)
												)
										)
								), "i"
						), null
				)
		));
	}

	private static ConfiguredEngine<?> flameBurstCircle() {
		return new DelayedIterator(
				IntVariable.of("min(TickUsing/10,3)"),
				IntVariable.of("10"),
				new RandomVariableModifier("r", 2,
						new LoopIterator(
								IntVariable.of("3+i*2"),
								new DelayModifier(
										IntVariable.of("abs(2-j)"),
										new RotationModifier(
												DoubleVariable.of("180/(3+i*2)*(j+(r0+r1)/2)-90"),
												new ForwardOffsetModifier(
														DoubleVariable.of("6*i+4"),
														new RandomOffsetModifier(
																RandomOffsetModifier.Type.BALL,
																DoubleVariable.of("0.1"),
																DoubleVariable.ZERO,
																DoubleVariable.of("0.1"),
																new ListInstance(List.of(star(2, 0.2),
																		new RingRandomIterator(
																				DoubleVariable.of("0"),
																				DoubleVariable.of("2"),
																				DoubleVariable.of("-180"),
																				DoubleVariable.of("180"),
																				IntVariable.of("100"),
																				new SetDirectionModifier(
																						DoubleVariable.ZERO,
																						DoubleVariable.of("1"),
																						DoubleVariable.ZERO,
																						new BlockParticleInstance(
																								Blocks.STONE,
																								DoubleVariable.of("0.5+rand(0,0.4)")
																						)
																				), null
																		)
																))
														)
												)
										)
								), "j"
						)
				), "i"
		);
	}

	private static ConfiguredEngine<?> star(double radius, double step) {
		int linestep = (int) Math.round(1.9 * radius / step);
		int circlestep = (int) Math.round(radius * Math.PI * 2 / step);
		return new RotationModifier(
				DoubleVariable.of("rand(0,360)"),
				new ListInstance(List.of(
						new LoopIterator(
								IntVariable.of("5"),
								new RotationModifier(
										DoubleVariable.of("72*ri"),
										new ForwardOffsetModifier(
												DoubleVariable.of(radius + ""),
												new RotationModifier(
														DoubleVariable.of("162"),
														new LinearIterator(
																DoubleVariable.of(radius * 1.9 / linestep + ""),
																Vec3.ZERO,
																DoubleVariable.ZERO,
																IntVariable.of(linestep + 1 + ""),
																true,
																new SimpleParticleInstance(
																		ParticleTypes.FLAME,
																		DoubleVariable.ZERO
																),
																null
														)
												)
										)
								), "ri"
						),
						new LoopIterator(
								IntVariable.of(circlestep + ""),
								new RotationModifier(
										DoubleVariable.of(360d / circlestep + "*ri"),
										new ForwardOffsetModifier(
												DoubleVariable.of(radius + ""),
												new SimpleParticleInstance(
														ParticleTypes.FLAME,
														DoubleVariable.ZERO
												)
										)
								), "ri"
						)
				))
		);
	}

	private static ResourceKey<SpellAction> spell(String id) {
		return ResourceKey.create(EngineRegistry.SPELL, L2Magic.loc(id));
	}

	public LMDatapackRegistriesGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
		super(output, registries, BUILDER, Set.of("minecraft", L2Magic.MODID));
	}

	@NotNull
	public String getName() {
		return "L2Magic Spell Data";
	}

}

package dev.xkmc.l2magic.content.item;

import dev.xkmc.l2library.util.raytrace.IGlowingTarget;
import dev.xkmc.l2library.util.raytrace.RayTraceUtil;
import dev.xkmc.l2magic.content.engine.context.LocationContext;
import dev.xkmc.l2magic.content.engine.context.SpellContext;
import dev.xkmc.l2magic.content.engine.core.SpellAction;
import dev.xkmc.l2magic.content.engine.core.SpellCastType;
import dev.xkmc.l2magic.content.engine.core.SpellTriggerType;
import dev.xkmc.l2magic.init.registrate.EngineRegistry;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class WandItem extends Item implements IGlowingTarget {

	private static final String KEY = "l2magic:spell";


	@Nullable
	public static ResourceLocation getSpellId(Level level, ItemStack stack) {
		var root = stack.getTag();
		if (stack.isEmpty() || root == null) return null;
		if (!root.contains(KEY, Tag.TAG_STRING)) return null;
		String id = root.getString(KEY);
		if (!ResourceLocation.isValidResourceLocation(id))
			return null;
		return new ResourceLocation(id);
	}

	@Nullable
	public static SpellAction getSpell(Level level, ItemStack stack) {
		ResourceLocation id = getSpellId(level, stack);
		if (id == null) return null;
		return level.registryAccess().registryOrThrow(EngineRegistry.SPELL).get(id);
	}

	public static ItemStack setSpell(ItemStack stack, ResourceLocation id) {
		stack.getOrCreateTag().putString(KEY, id.toString());
		return stack;
	}

	public WandItem(Properties prop) {
		super(prop);
	}


	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		SpellAction spell = getSpell(level, stack);
		if (spell != null) {
			if (spell.castType() == SpellCastType.INSTANT) {
				if (castSpell(stack, level, player, spell, 0)) {
					return InteractionResultHolder.consume(stack);
				} else {
					return InteractionResultHolder.fail(stack);
				}
			} else {
				return ItemUtils.startUsingInstantly(level, player, hand);
			}
		}
		return super.use(level, player, hand);
	}

	@Override
	public void onUseTick(Level level, LivingEntity user, ItemStack stack, int remain) {
		super.onUseTick(level, user, stack, remain);
		SpellAction spell = getSpell(level, stack);
		if (spell != null) {
			if (spell.castType() == SpellCastType.CONTINUOUS) {
				castSpell(stack, level, user, spell, getUseDuration(stack) - remain);
			}
		}
	}

	@Override
	public void releaseUsing(ItemStack stack, Level level, LivingEntity user, int remain) {
		SpellAction spell = getSpell(level, stack);
		if (spell != null) {
			if (spell.castType() == SpellCastType.CHARGE) {
				castSpell(stack, level, user, spell, getUseDuration(stack) - remain);
			}
		}
	}

	@Override
	public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
		if (selected && entity instanceof Player player && level.isClientSide()) {
			SpellAction spell = getSpell(level, stack);
			if (spell != null && spell.triggerType() == SpellTriggerType.TARGET_ENTITY) {
				RayTraceUtil.clientUpdateTarget(player, getDistance(stack));
			}
		}
	}

	@Nullable
	private LivingEntity getTarget(LivingEntity le) {
		if (le instanceof Player player) {
			return RayTraceUtil.serverGetTarget(player);
		}
		if (le instanceof Mob mob) {
			return mob.getTarget();
		}
		return null;
	}

	private Vec3 getCenter(LivingEntity le) {
		return le.position().add(0, le.getBbHeight() / 2f, 0);
	}

	private Vec3 getForward(LivingEntity le) {
		if (le instanceof Player player) {
			return RayTraceUtil.getRayTerm(Vec3.ZERO, player.getXRot(), player.getYRot(), 1);
		}
		if (le instanceof Mob mob) {
			var target = mob.getTarget();
			if (target != null) {
				return getCenter(le).subtract(mob.getEyePosition());
			}
		}
		return le.getForward();
	}

	private boolean castSpell(ItemStack stack, Level level, LivingEntity user, SpellAction spell, int useTick) {
		double power = 1;
		switch (spell.triggerType()) {
			case SELF_POS -> spell.execute(new SpellContext(user, user.position(), LocationContext.UP, useTick, power));
			case TARGET_POS -> {
				var start = user.getEyePosition();
				var dir = getForward(user);
				var end = start.add(dir.scale(getDistance(stack)));
				AABB box = (new AABB(start, end)).inflate(1.0);
				var bhit = level.clip(new ClipContext(start, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, user));
				var ehit = ProjectileUtil.getEntityHitResult(level, user, start, end, box, e -> true);
				if ((ehit == null || ehit.getType() == HitResult.Type.MISS) && bhit.getType() == HitResult.Type.MISS) {
					return false;
				}
				var hit = ehit != null && ehit.getLocation().distanceToSqr(start) < bhit.getLocation().distanceToSqr(start) ?
						ehit.getLocation() : bhit.getLocation();
				spell.execute(new SpellContext(user, hit, LocationContext.UP, useTick, power));
			}
			case FACING_BACK -> {
				var dir = getForward(user);
				var pos = user.getEyePosition();
				spell.execute(new SpellContext(user, pos.add(dir), dir, useTick, power));
			}
			case FACING_FRONT -> {
				var dir = getForward(user);
				var pos = user.getEyePosition();
				spell.execute(new SpellContext(user, pos.add(dir.scale(-1)), dir, useTick, power));
			}
			case TARGET_ENTITY -> {
				var target = getTarget(user);
				if (target != null)
					spell.execute(new SpellContext(user, target.position(), LocationContext.UP, useTick, power));
				else return false;
			}
		}
		return true;
	}

	@Override
	public int getUseDuration(ItemStack stack) {
		return 72000;
	}

	@Override
	public int getDistance(ItemStack itemStack) {
		return 64;
	}
}

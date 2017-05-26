package com.github.kkysen.supersmashbros.core;

/**
 * 
 * 
 * @author Khyber Sen
 */
public class Hurtbox extends Box {
    
    private static final float DAMAGE_SCALE = 1.0f; // FIXME
    
    public float damageTakenBy(final Hitbox hitbox) {
        return intersectionArea(hitbox) * hitbox.damage * DAMAGE_SCALE;
    }
    
    public float collide(final Hitbox hitbox) {
        final float damage = damageTakenBy(hitbox);
        hitbox.player.attack(damage);
        return damage;
    }
    
    @Override
    public boolean update() {
        return true; // TODO
    }
    
}

/*
 * This file is part of HPET - Packet Based Pet Plugin
 *
 * TOS (Terms of Service)
 * You are not allowed to decompile, or redestribuite part of this code if not authorized by the original author.
 * You are not allowed to claim this resource as yours.
 */
package it.heron.hpet.api;

import it.heron.hpet.api.events.PetSelectEvent;
import it.heron.hpet.levels.LType;
import it.heron.hpet.levels.LevelEvents;
import it.heron.hpet.messages.Messages;
import it.heron.hpet.pettypes.PetType;
import it.heron.hpet.userpets.MobUserPet;
import it.heron.hpet.userpets.MythicUserPet;
import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import it.heron.hpet.Pet;
import it.heron.hpet.userpets.UserPet;
import it.heron.hpet.groups.HSlot;

import java.util.*;

public class API {

    public boolean hasUserPet(Player p) {
        return getUserPet(p) != null;
    }
    public UserPet getUserPet(Player p) {
        return Pet.getInstance().getPacketUtils().getPets().get(p.getUniqueId());
    }
    public List<HSlot> getEnabledPetTypes() {
        return Pet.getInstance().getPetTypes();
    }
    public Collection<UserPet> getEnabledPets() {return Pet.getInstance().getPacketUtils().getPets().values();}

    public void selectPet(Player player, String petType) {
        selectPet(player, Pet.getPetTypeByName(petType));
    }
    public void selectPet(Player player, PetType petType) {
        if(Pet.getApi().hasUserPet(player)) {
            Pet.getApi().getUserPet(player).remove();
        }

        UserPet pet;
        if(petType.isMob()) {
            pet = new MobUserPet(player, petType, null);
        } else {
            if(petType.isMythicMob()) {
                pet = new MythicUserPet(player, petType, null);
            } else {
                pet = new UserPet(player, petType, null);
            }
        }
        Bukkit.getPluginManager().callEvent(new PetSelectEvent(player, pet));
        player.sendMessage(Messages.getMessage("pet.spawned").replace("[type]", petType.getName()));
    }

    public int getPetLevel(Player p, String type) {
        LevelData d = new LevelData(p.getUniqueId(), type, Pet.getInstance().getDatabase().getPetLevel(p.getUniqueId(), type));
        return d.getLevel();
    }
    public void setPetLevel(Player p, String type, int amount) {
        Pet.getInstance().getDatabase().setPetLevel(p.getUniqueId(), type, amount);
    }

    public void incrementLevel(Player p) {
        UserPet upet = Pet.getApi().getUserPet(p);
        if(upet.getType().getLtype() == LType.NONE) return;
        int l = upet.getLevel()+1;
        setPetLevel(p, upet.getType().getName(), l);
        getUserPet(p).updateLevel();
        p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
        for(String s : Messages.getList("levelup")) {
            p.sendMessage(s.replace("[level]", l+"").replace("[leveltype]", Messages.getMessage("leveltype."+upet.getType().getLtype().name())+" ??7"+ LevelEvents.currentStat(upet)+"/"+LevelEvents.getMaxStat(upet)));
        }
    }

    private @Data class LevelData {
        private UUID uuid;
        private String petType;
        private int level;

        public LevelData(UUID uuid, String petType, int level) {
            this.petType = petType;
            this.uuid = uuid;
            this.level = level;
        }

    }

}

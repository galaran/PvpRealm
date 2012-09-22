package mccity.plugins.pvprealm;

import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownyUniverse;
import org.bukkit.entity.Player;

public class TownyFacade {

    private final TownyUniverse townyUniv;

    public TownyFacade(TownyUniverse townyUniverse) {
        townyUniv = townyUniverse;
    }

    public Resident getTownyResident(Player player) {
        return townyUniv.getResidentMap().get(player.getName().toLowerCase());
    }

    public boolean isTownyFriendly(Player player, Player target) {
        Resident playerResident = getTownyResident(player);
        Resident targetResident = getTownyResident(target);
        if (playerResident == null || targetResident == null) return false;

        if (playerResident.hasFriend(targetResident)) return true;

        try {
            if (playerResident.hasTown() && targetResident.hasTown()) {
                Town playerTown = playerResident.getTown();
                Town targetTown = targetResident.getTown();
                if (playerTown == targetTown) {
                    return true;
                } else if (playerTown.hasNation() && targetTown.hasNation()) {
                    Nation playerNation = playerTown.getNation();
                    Nation targetNation = targetTown.getNation();
                    if (playerNation == targetNation || playerNation.hasAlly(targetNation)) {
                        return true;
                    }
                }
            }
        } catch (Exception ex) {
            // never happens
        }

        return false;
    }
}

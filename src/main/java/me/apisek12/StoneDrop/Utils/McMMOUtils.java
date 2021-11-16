package me.apisek12.StoneDrop.Utils;

import com.gmail.nossr50.datatypes.skills.PrimarySkillType;
import com.gmail.nossr50.datatypes.skills.SubSkillType;
import com.gmail.nossr50.mcMMO;
import com.gmail.nossr50.util.Permissions;
import com.gmail.nossr50.util.player.UserManager;
import com.gmail.nossr50.util.random.RandomChanceUtil;
import com.gmail.nossr50.util.skills.RankUtils;
import org.bukkit.entity.Player;

public class McMMOUtils {

    private static boolean canDoubleDrop(Player player) {
        return RankUtils.hasUnlockedSubskill(player, SubSkillType.MINING_DOUBLE_DROPS) && Permissions.isSubSkillEnabled(player, SubSkillType.MINING_DOUBLE_DROPS);
    }

    public static int increasePlayerDrop(Player player, int amountToDrop){

        if(!mcMMO.p.getSkillTools().doesPlayerHaveSkillPermission(player, PrimarySkillType.MINING)){
            return 0;
        }

        if (!Permissions.isSubSkillEnabled(player, SubSkillType.MINING_DOUBLE_DROPS)) {
            return 0;
        }
        if( !canDoubleDrop(player)){
            return 0;
        }


        int amountToAdd = 0;

        for(int i=0; i<amountToDrop; i++){
            if (RandomChanceUtil.checkRandomChanceExecutionSuccess(player, SubSkillType.MINING_DOUBLE_DROPS, true)) {
                amountToAdd+=1;
                if (UserManager.getPlayer(player).getAbilityMode(mcMMO.p.getSkillTools().getSuperAbility(PrimarySkillType.MINING)) && mcMMO.p.getAdvancedConfig().getAllowMiningTripleDrops()){
                    amountToAdd+=1;
                }
            }
        }

        return amountToAdd;
    }
}

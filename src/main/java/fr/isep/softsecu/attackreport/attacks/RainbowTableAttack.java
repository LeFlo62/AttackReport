package fr.isep.softsecu.attackreport.attacks;

import fr.isep.softsecu.attackreport.Attack;
import fr.isep.softsecu.attackreport.Report;

import java.util.List;

public class RainbowTableAttack implements Attack {

    private String loginUrl;

    @Override
    public List<Class<? extends Attack>> getRequiredPreviousAttacks() {
        return List.of(FindLoginPageAttack.class);
    }

    @Override
    public void setPreviousAttacks(List<Attack> previousAttacks) {
        for(Attack previousAttack : previousAttacks){
            if(previousAttack instanceof FindLoginPageAttack findLoginPageAttack){
                loginUrl = findLoginPageAttack.getLoginUrlStr();
            }
        }
    }

    @Override
    public void run(Report report, String ip, int port) {
        System.out.println("Rainbow table attack on " + loginUrl);
    }

    @Override
    public String getAttackName() {
        return "Rainbow table attack";
    }
}

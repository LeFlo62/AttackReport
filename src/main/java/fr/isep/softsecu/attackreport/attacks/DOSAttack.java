package fr.isep.softsecu.attackreport.attacks;

import fr.isep.softsecu.attackreport.Attack;
import fr.isep.softsecu.attackreport.Report;

public class DOSAttack implements Attack {
    @Override
    public void run(Report report, String ip, int port) {

    }

    @Override
    public String getAttackName() {
        return "DOS attack";
    }
}

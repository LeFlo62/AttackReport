package fr.isep.softsecu.attackreport;

public class DOSAttack implements Attack {
    @Override
    public void run(Report report, String ip, int port) {

    }

    @Override
    public String getAttackName() {
        return "DOS attack";
    }
}

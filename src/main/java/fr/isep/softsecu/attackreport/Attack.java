package fr.isep.softsecu.attackreport;

import java.util.List;

public interface Attack {

    default List<Class<? extends Attack>> getRequiredPreviousAttacks() {
        return null;
    }

    default void setPreviousAttacks(List<Attack> previousAttacks) {}

    void run(Report report, String ip, int port);

    String getAttackName();

}

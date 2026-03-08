package com.examples.dilemma.strategy;

/**
 * The classic Prisoner's Dilemma payoff matrix.
 *
 * <p>When two players each choose {@link Move#COOPERATE} or {@link Move#DEFECT},
 * their individual scores are determined by this matrix:
 *
 * <pre>
 *                  Opponent COOPERATES    Opponent DEFECTS
 *   I COOPERATE      R=3  (Reward)          S=0  (Sucker)
 *   I DEFECT         T=5  (Temptation)      P=1  (Punishment)
 * </pre>
 *
 * <p>The constraints T &gt; R &gt; P &gt; S and 2R &gt; T+S ensure that
 * mutual cooperation is collectively optimal, while individual incentive
 * always favors defection — the core tension of the dilemma.
 */
public final class Payoff {

    /** Reward for mutual cooperation. */
    public static final int REWARD = 3;

    /** Temptation to defect (unilateral defection against a cooperator). */
    public static final int TEMPTATION = 5;

    /** Punishment for mutual defection. */
    public static final int PUNISHMENT = 1;

    /** Sucker's payoff (cooperating against a defector). */
    public static final int SUCKER = 0;

    private Payoff() {}

    /**
     * Returns the score for a player who played {@code mine} against an opponent
     * who played {@code theirs}.
     *
     * @param mine   this player's move
     * @param theirs the opponent's move
     * @return the score for this player this round
     */
    public static int score(Move mine, Move theirs) {
        if (mine == Move.COOPERATE && theirs == Move.COOPERATE) return REWARD;
        if (mine == Move.DEFECT   && theirs == Move.COOPERATE) return TEMPTATION;
        if (mine == Move.COOPERATE && theirs == Move.DEFECT)   return SUCKER;
        return PUNISHMENT; // both defect
    }
}

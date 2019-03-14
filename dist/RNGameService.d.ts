export declare class RNGameService {
    static submitScore(LEADERBOARD_ID: string, score: number): void;
    static openLeadboard(LEADERBOARD_ID: string): void;
    /**Return -1 or current rank*/
    static getCurrentRank(LEADERBOARD_ID: string): Promise<number>;
}

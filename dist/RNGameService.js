import { NativeModules } from "react-native";
export class RNGameService {
    static submitScore(LEADERBOARD_ID, score) {
        NativeModules.RNGameService.submitScore(LEADERBOARD_ID, score);
    }
    static openLeadboard(LEADERBOARD_ID) {
        NativeModules.RNGameService.openLeadboard(LEADERBOARD_ID);
    }
    /**Return -1 or current rank*/
    static async getCurrentRank(LEADERBOARD_ID) {
        return NativeModules.RNGameService.getCurrentRank(LEADERBOARD_ID);
    }
}

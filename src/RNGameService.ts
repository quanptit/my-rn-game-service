import React from 'react';
import {NativeModules} from "react-native";

export class RNGameService {

    static submitScore(LEADERBOARD_ID: string, score: number) {
        NativeModules.RNGameService.submitScore(LEADERBOARD_ID, score);
    }

    static openLeadboard(LEADERBOARD_ID: string) {
        NativeModules.RNGameService.openLeadboard(LEADERBOARD_ID);
    }

    /**Return -1 or current rank*/
    static async getCurrentRank(LEADERBOARD_ID: string): Promise<number>{
        return NativeModules.RNGameService.getCurrentRank(LEADERBOARD_ID);
    }
}

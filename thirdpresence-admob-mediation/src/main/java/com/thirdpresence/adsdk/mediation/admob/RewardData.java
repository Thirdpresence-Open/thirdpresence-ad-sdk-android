package com.thirdpresence.adsdk.mediation.admob;

import com.google.android.gms.ads.reward.RewardItem;

class RewardData implements RewardItem {
    private String title;
    private int amount;

    private RewardData() {}

    public RewardData(String title, int amount) {
        super();
        this.title = title;
        this.amount = amount;
    }

    @Override
    public String getType() {
        return title;
    }

    @Override
    public int getAmount() {
        return amount;
    }

    @Override
    public String toString() {
        return amount + " " + title;
    }
}

package assignments.dsa;

/**
 * LeetCode 121 learning seam; implement the best single buy-then-sell profit.
 */
public final class BestTimeToBuySellStock {
    public int maxProfit(int[] prices)  {

        if (prices == null || prices.length == 0) {
            return 0;
        }

        /**
         * The logic is to find the maximum profit between two days
         * The selling day must be after the buying day. (ain't it obvious?)
         */

        // Brute force approach

        /*
            int maxProfit = 0;
            for (int i = 0; i < prices.length - 1; i++) {
                for (int j = i + 1; j < prices.length; j++) {
                    if (prices[j] < prices[i]) {
                        maxProfit = Math.max(maxProfit, prices[j] - prices[i]);
                    }
                }
            }
            return maxProfit;
         */

        /* Time complexity: O(n^2) | Space complexity: O(1) */

        /**
         * The one-pass approach works in such a way that we keep track of the max profit between two days.
         * But along with that, we also keep track of the minimum price seen so far.
         * Once we find the minimum price, we can calculate the profit between the current day and the minimum price.
         */

        int maxProfit = 0, minPrice = Integer.MAX_VALUE;

        for (int price : prices) {
            if (price < minPrice) {
                minPrice = price;
            } else {
                maxProfit = Math.max(maxProfit, price - minPrice);
            }
        }

        return maxProfit;

        /* Time complexity: O(n) | Space complexity: O(1) */
    }
}

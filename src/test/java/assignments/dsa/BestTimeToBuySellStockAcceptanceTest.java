package assignments.dsa;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Fixed LeetCode 121 contract; expected to fail until the learner implements maxProfit. */
@Tag("learnerAcceptance")
class BestTimeToBuySellStockAcceptanceTest {
    private final BestTimeToBuySellStock solution = new BestTimeToBuySellStock();

    @Test
    void returnsBestLaterSellProfit() {
        assertEquals(5, solution.maxProfit(new int[]{7, 1, 5, 3, 6, 4}));
    }

    @Test
    void returnsZeroForDescendingPricesAndSingleDay() {
        assertEquals(0, solution.maxProfit(new int[]{7, 6, 4, 3, 1}));
        assertEquals(0, solution.maxProfit(new int[]{9}));
    }

    @Test
    void handlesZeroProfitAndInputIsUnchanged() {
        int[] prices = {2, 2, 3, 1, 1};
        int[] before = prices.clone();
        assertEquals(1, solution.maxProfit(prices));
        assertEquals(java.util.Arrays.toString(before), java.util.Arrays.toString(prices));
    }

    @Test
    void rejectsNullAndAcceptsEmptyInputAsNoTransaction() {
        assertThrows(IllegalArgumentException.class, () -> solution.maxProfit(null));
        assertEquals(0, solution.maxProfit(new int[0]));
    }
}

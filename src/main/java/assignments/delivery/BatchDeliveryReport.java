package assignments.delivery;

public record BatchDeliveryReport(int captured, int receiptsCreated, int identicalReplays,
                                  int acknowledged, int pendingAtStop) { }

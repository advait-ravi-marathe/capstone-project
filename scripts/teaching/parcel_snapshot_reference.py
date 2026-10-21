from copy import deepcopy
from decimal import Decimal


def assert_equal(left, right, reason):
    if left != right:
        raise AssertionError(reason)


def projection(row):
    return {
        "id": row["id"], "owner": row["owner"],
        "amount": str(Decimal(row["amount"]).quantize(Decimal("0.01"))),
        "currency": row["currency"], "version": row["version"],
    }


def check(trace):
    expected = {"id": "hold-7", "owner": "member-a", "amount": "12.50",
                "currency": "GBP", "version": 1}
    assert_equal(trace["statuses"], [201, 200, 200], "create/read/replay status")
    assert_equal(trace["requests"][0], trace["requests"][1], "same replay request")
    assert_equal(set(trace["api"]), {"create", "read", "replay"}, "exact observation set")
    for body in trace["api"].values():
        assert_equal(body, expected, "complete approved API projection")
    assert_equal(trace["dom"], "hold-7 | 12.50 GBP | revision 1", "DOM agrees with API")
    before, created, read, replayed = trace["db"]
    assert_equal(len(created["holds"]), len(before["holds"]) + 1, "one new hold")
    assert_equal(created["holds"][:-1], before["holds"], "all prior fields stable")
    assert_equal(projection(created["holds"][-1]), expected, "new DB row agrees")
    assert_equal(created["events"], before["events"] + [{"id": "hold-7", "version": 1}], "one linked event")
    assert_equal(created["keys"], before["keys"] + [{"owner": "member-a", "key": "r-9", "id": "hold-7"}], "one linked receipt")
    assert_equal(created, read, "readback changes nothing")
    assert_equal(read, replayed, "replay changes nothing")


old = {"id": "old-1", "owner": "member-b", "amount": "8.00", "currency": "GBP", "version": 3}
new = {"id": "hold-7", "owner": "member-a", "amount": "12.50", "currency": "GBP", "version": 1}
before = {"holds": [old], "events": [], "keys": []}
created = {"holds": [deepcopy(old), deepcopy(new)],
           "events": [{"id": "hold-7", "version": 1}],
           "keys": [{"owner": "member-a", "key": "r-9", "id": "hold-7"}]}
request = {"parcelId": "parcel-4", "clientRequestId": "r-9"}
good = {"statuses": [201, 200, 200], "requests": [deepcopy(request), deepcopy(request)],
        "api": {action: projection(new) for action in ["create", "read", "replay"]},
        "dom": "hold-7 | 12.50 GBP | revision 1",
        "db": [before, created, deepcopy(created), deepcopy(created)]}
check(good)
bad = deepcopy(good)
bad["db"][2]["holds"][-1]["amount"] = "99.00"
try:
    check(bad)
except AssertionError as error:
    assert_equal(str(error), "readback changes nothing", "mutation killed at the read checkpoint")
else:
    raise AssertionError("Count-preserving intermediate mutation survived")
print("PASS: semantic API/DOM/DB oracle; intermediate count-preserving mutation rejected")

from copy import deepcopy


class Process:
    def __init__(self, name, database):
        self.name, self.database, self.sessions = name, database, {}
        self.next_session = 0

    def select(self, member):
        self.next_session += 1
        cookie = f"{self.name}-s{self.next_session}"
        self.sessions[cookie] = member
        return cookie

    def session_status(self, cookie):
        return 200 if cookie in self.sessions else 401

    def logout(self, cookie):
        self.sessions.pop(cookie, None)
        return 204

    def preview(self, cookie, parcel):
        member = self.sessions.get(cookie)
        if member is None:
            return 401, None
        row = self.database.get(parcel)
        if row is None or row["member"] != member:
            return 404, None
        return 200, {"parcel": parcel, "revision": row["revision"], "note": "Reference only"}


database = {"p-7": {"member": "member-a", "revision": 2}}
before = deepcopy(database)
first = Process("process-1", database)
logout_cookie = first.select("member-a")
assert first.session_status(logout_cookie) == 200
assert first.logout(logout_cookie) == 204
assert first.session_status(logout_cookie) == 401

# Separate restart experiment: session remains active until the process is replaced.
active_cookie = first.select("member-a")
assert first.session_status(active_cookie) == 200
status, expected_preview = first.preview(active_cookie, "p-7")
assert status == 200
second = Process("process-2", database)
assert second.name != first.name
assert second.session_status(active_cookie) == 401
fresh_cookie = second.select("member-a")
assert second.preview(fresh_cookie, "p-7") == (200, expected_preview)
foreign_cookie = second.select("member-b")
assert second.preview(foreign_cookie, "p-7") == (404, None)
assert database == before
try:
    assert first.session_status(logout_cookie) == 200, "already logged out is invalid restart evidence"
except AssertionError as error:
    assert str(error) == "already logged out is invalid restart evidence"
else:
    raise AssertionError("Confounded restart sample was accepted")
print("PASS: separate logout/restart experiments; pre-invalidated sample rejected")

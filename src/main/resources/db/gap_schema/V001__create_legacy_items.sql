CREATE TABLE gap_backfill_item (
    item_id integer PRIMARY KEY,
    legacy_value text NOT NULL
);

INSERT INTO gap_backfill_item (item_id, legacy_value) VALUES
    (1, ' Policy Alpha '), (2, 'Policy Bravo'), (3, ' Policy Charlie'),
    (4, 'Policy Delta '), (5, ' Policy Echo'), (6, 'Policy Foxtrot '),
    (7, ' Policy Golf'), (8, 'Policy Hotel '), (9, ' Policy India'), (10, 'Policy Juliet ');

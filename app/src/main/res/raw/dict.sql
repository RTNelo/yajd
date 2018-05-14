INSERT INTO WordRecord (id, word, description, frequency, summary) VALUES (1, "今日", "「今日」の説明", 5, "今天"), (2, "今", "「今」の説明", 5, "现在"), (3, "打ち上げ花火", "「打ち上げ花火」の説明", 5, "烟花"), (4, "すみません", "「すみません」の説明", 5, "对不起");
INSERT INTO WordRomaji (wordRecordId, romaji) VALUES (1, "kyo"), (1, "konjitsu"), (2, "ima"), (3, "uchiagehanabi"), (4, "sumimasenn");
INSERT INTO WordFeature (wordRecordId, feature) VALUES (1, "今日"), (1, "kyo"), (1, "konjitsu"), (2, "今"), (2, "ima"), (3, "打ち上げ花火"), (3, "uchiagehanabi"), (3, "打上花火"), (4, "すみません"), (4, "sumimasenn");

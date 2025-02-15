package fr.osallek.clausewitzparser.model;

public enum BinaryToken {
    QUOTED_STRING((short) 0x000F),
    NOT_QUOTED_STRING((short) 0x0017),
    EQUALS((short) 0x0001),
    END((short) 0x0004),
    OPEN((short) 0x0003),
    UNSIGNED_INT((short) 0x0014),
    UNSIGNED_LONG((short) 0x029C),
    INT((short) 0x000C),
    BOOL((short) 0x000E),
    FLOAT((short) 0x000D),
    DOUBLE((short) 0x0167),
    COLOR((short) 0x0243);

    public final short token;

    private static final BinaryToken[] VALUES = BinaryToken.values();

    BinaryToken(short token) {
        this.token = token;
    }

    public static BinaryToken ofToken(short token) {
        for (BinaryToken value : VALUES) {
            if (value.token == token) {
                return value;
            }
        }

        return null;
    }

    public short getToken() {
        return token;
    }
}

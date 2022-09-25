package fr.osallek.clausewitzparser.model;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    private static final Map<Short, BinaryToken> TOKEN_MAP = Arrays.stream(BinaryToken.values())
                                                                   .collect(Collectors.toMap(BinaryToken::getToken, Function.identity()));

    BinaryToken(short token) {
        this.token = token;
    }

    public static Optional<BinaryToken> ofToken(short token) {
        return Optional.ofNullable(TOKEN_MAP.get(token));
    }

    public short getToken() {
        return token;
    }
}

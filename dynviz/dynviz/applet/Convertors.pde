public static final byte[] intToByteArray(int value) {
        return new byte[] {
                (byte)value,
                (byte)(value >>> 8),
                (byte)(value >>> 16),
                (byte)(value >>> 24)};
}

public static final int byteArray2ToInt(byte [] b) {
        return 0+((b[1] & 0xFF) << 8)
                + (b[0] & 0xFF);
}

public static final int byteArrayToInt(byte [] b) {
        return (b[3] << 24)
                + ((b[2] & 0xFF) << 16)
                + ((b[1] & 0xFF) << 8)
                + (b[0] & 0xFF);
}

//public static final short byteArrayToShort(byte [] b) {
//        return (b[1] << 8)
//                + (short)(b[0] & 0xFF);
//}

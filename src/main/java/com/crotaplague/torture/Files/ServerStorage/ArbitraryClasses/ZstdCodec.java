package com.crotaplague.torture.Files.ServerStorage.ArbitraryClasses;

import com.github.luben.zstd.Zstd;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

public class ZstdCodec {
    private static byte[] dictionary;
    public static void trainDictionary(List<String> input){
        byte[][] samples = input.stream().map(s -> s.getBytes(StandardCharsets.UTF_8)).toArray(byte[][]::new);
        byte[] dict = new byte[32 * 1024];
        long dictSize = Zstd.trainFromBuffer(samples, dict);
        dictionary = Arrays.copyOf(dict, (int) dictSize);
    }

    public static String compressToBase64(String input) {
        byte[] compressed = Zstd.compress(input.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(compressed);
    }

    public static String decompressFromBase64(String base64) {
        byte[] compressed = Base64.getDecoder().decode(base64);
        long size = Zstd.decompressedSize(compressed);
        byte[] decompressed = Zstd.decompress(compressed, (int) size);
        return new String(decompressed, StandardCharsets.UTF_8);
    }
}

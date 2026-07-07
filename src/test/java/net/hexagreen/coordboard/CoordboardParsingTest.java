package net.hexagreen.coordboard;

import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.junit.Assert;
import org.junit.Test;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CoordboardParsingTest {
    private static final Pattern NAMED_VEC = Pattern.compile("x[:=]?(?<x>[+-]?\\d+)|z[:=]?(?<z>[+-]?\\d+)");
    private static final Pattern VEC3 = Pattern.compile("([+-]?\\d+)[ ,]([+-]?\\d+)[ ,]([+-]?\\d+)");
    private static final Pattern VEC2 = Pattern.compile("([+-]?\\d+)[ ,]([+-]?\\d+)");
    @Test
    public void labeledCoordTest() {
        var a = Component.literal("Delivery destination for %s: X -1542, Y 61, Z -2241");
        var b = Component.literal("Z: -1542, Y: 61, x: -2241");
        var c = Component.literal("X-1542y61z-2241");
        Assert.assertEquals(new Vec3(-1542, 64, -2241), getTargetFromCoordinateBoard(a, 64));
        Assert.assertEquals(new Vec3(-2241, 64, -1542), getTargetFromCoordinateBoard(b, 64));
        Assert.assertEquals(new Vec3(-1542, 64, -2241), getTargetFromCoordinateBoard(c, 64));
    }

    private @Nullable Vec3 getTargetFromCoordinateBoard(Component selected, int y) {
        if(true) {
            String string = coordinateSanitizer(selected.getString());
            try {
                Matcher namedMatcher = NAMED_VEC.matcher(string);
                String rawX = null, rawZ = null;
                while(namedMatcher.find()) {
                    if(namedMatcher.group("x") != null) rawX = namedMatcher.group("x");
                    if(namedMatcher.group("z") != null) rawZ = namedMatcher.group("z");
                }
                if(rawX != null && rawZ != null) {
                    int x = Integer.parseInt(rawX);
                    int z = Integer.parseInt(rawZ);
                    return new Vec3(x, y, z);
                }

                Matcher v3Matcher = VEC3.matcher(string);
                if(v3Matcher.find()) {
                    int x = Integer.parseInt(v3Matcher.group(1));
                    int z = Integer.parseInt(v3Matcher.group(3));
                    return new Vec3(x, y, z);
                }

                Matcher v2Matcher = VEC2.matcher(string);
                if(v2Matcher.find()) {
                    int x = Integer.parseInt(v2Matcher.group(1));
                    int z = Integer.parseInt(v2Matcher.group(2));
                    return new Vec3(x, 64, z);
                }
            } catch(NumberFormatException ignore) {}
        }
        return null;
    }

    private String coordinateSanitizer(String string) {
        return string
            .replaceAll("(?<!\\d) ", "")
            .replaceAll(" (?=,)", "")
            .toLowerCase(Locale.ENGLISH);
    }
}

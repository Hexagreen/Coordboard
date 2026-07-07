package net.hexagreen.coordboard.clipboard;

import com.simibubi.create.content.equipment.clipboard.ClipboardEntry;
import dev.simulated_team.simulated.content.blocks.nav_table.NavTableBlockEntity;
import dev.simulated_team.simulated.content.blocks.nav_table.navigation_target.NavigationTarget;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClipboardNavigationTarget implements NavigationTarget {
    private static final Pattern NAMED_VEC = Pattern.compile("x[:=]?(?<x>[+-]?\\d+)|z[:=]?(?<z>[+-]?\\d+)");
    private static final Pattern VEC3 = Pattern.compile("([+-]?\\d+)[ ,]([+-]?\\d+)[ ,]([+-]?\\d+)");
    private static final Pattern VEC2 = Pattern.compile("([+-]?\\d+)[ ,]([+-]?\\d+)");

    @Override
    public float getMaxRange() {
        return 0.0F;
    }

    @Override
    public @Nullable Vec3 getTarget(NavTableBlockEntity navTableBlockEntity, ItemStack itemStack) {
        double navY = navTableBlockEntity.getProjectedSelfPos().y();
        return getTargetFromCoordinateBoard(itemStack, navY);
    }

    private @Nullable Vec3 getTargetFromCoordinateBoard(ItemStack itemStack, double y) {
        List<ClipboardEntry> currentPage = ClipboardEntry.getLastViewedEntries(itemStack);
        Optional<ClipboardEntry> selected = currentPage.stream()
            .filter(e -> e.checked)
            .filter(e -> !e.text.getString().isBlank())
            .findFirst();
        if(selected.isPresent()) {
            String string = coordinateSanitizer(selected.get().text.getString());
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
